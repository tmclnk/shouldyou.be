package be.shouldyou;

import static be.shouldyou.Constants.SERVICE_CLIENT_ID;
import static be.shouldyou.OfyService.ofy;
import static com.google.api.server.spi.Constant.API_EMAIL_SCOPE;
import static com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.shouldyou.geocode.AddressExtractor;
import be.shouldyou.geocode.Geocoder;
import be.shouldyou.geocode.json.Location;

import com.google.api.client.util.DateTime;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/** An endpoint class we are exposing */
@Api(name = "sibw", version = "v2", namespace = @ApiNamespace(ownerDomain = "sibw", ownerName = "sibw", packagePath = ""))
public class IncidentService {
	private Logger log = LoggerFactory.getLogger(getClass());
	private List<String> channelKeys = new ArrayList<>();
	private final Gson gson;

	public IncidentService() {
		// TODO the toString here passes the info along as zulu w/o saying
		// it's zulu!
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc,
					JsonSerializationContext context) {
				return new JsonPrimitive(new DateTime(src, Calendar
						.getInstance().getTimeZone()).toString());
			}
		});

		this.gson = gsonBuilder.create();
	}
	
	@ApiMethod(name = "ping", scopes = { API_EMAIL_SCOPE }, clientIds = {
			API_EXPLORER_CLIENT_ID, SERVICE_CLIENT_ID })
	public PingResponse ping(User user, HttpServletRequest request) {
		final PingResponse resp = new PingResponse(user);
		if (user != null) {
			log.debug("Ping from {} {}", user.getEmail(), request.getRemoteAddr());
			resp.setGeocoderApiStatus(Geocoder.test());
		} else {
			log.debug("Anonymous ping from {}", request.getRemoteAddr());
		}
		return resp;
	}


	// @ApiMethod(name = "clearAll")
	// public void clearAll() {
	// for (Incident i : ofy().load().type(Incident.class).list()) {
	// ofy().delete().type(Incident.class).id(i.id);
	// }
	// }

	@ApiMethod(name = "getIncidents")
	public List<Incident> getIncidents() throws IOException {
		return ofy().load().type(Incident.class).limit(100).list();
	}

	@ApiMethod(name = "removeTweet", scopes = { API_EMAIL_SCOPE }, clientIds = {
			API_EXPLORER_CLIENT_ID, SERVICE_CLIENT_ID })
	public void removeTweet(@Named("tweetId") Long tweetId, User user)
			throws OAuthRequestException {
		checkUser(user);

		ofy().delete().type(Incident.class).id(tweetId).now();
	}

	/**
	 * Extracts geolocations from the incident's text, saves the
	 * {@link Incident} using {@link OfyService}, and publishes the Incident to
	 * the {@link ChannelService} (thereby pushing the update to all subscribed
	 * clients).
	 * 
	 * @param incident
	 * @throws IOException
	 * @return a (geocoded) copy of the incident if it was accepted, or null if
	 *         it was rejected for some reason
	 * @throws OAuthRequestException
	 */
	@ApiMethod(name = "insertIncident", scopes = { API_EMAIL_SCOPE }, clientIds = { API_EXPLORER_CLIENT_ID, SERVICE_CLIENT_ID })
	public Incident insertIncident(Incident incident, User user)
			throws IOException, OAuthRequestException {
		checkUser(user);

		// DO SOME QUICK VALIDATION
		if (incident.id != null) {
			log.debug("Rejecting incident - incident.id={}", incident.id);
			return null;
		}

		if (ofy().load().type(Incident.class)
				.filter("tweetId", incident.tweetId).list().size() > 0) {
			log.debug("Rejecting incident - duplicate tweetId={}",
					incident.tweetId);
			return null;
		}

		// geocode based on the text and save the result
		if (incident.getLocations() == null
				|| incident.getLocations().size() == 0) {
			List<String> addresses = AddressExtractor
					.extractAddresses(incident.text);
			incident.setAddresses(addresses);

			// = Geocoder.getLocations(incident.text);
			if (addresses.size() > 0) {
				List<Location> locations = new ArrayList<>();
				for (String addr : addresses) {
					Location location = Geocoder
							.getLocationFromStreetAddress(addr);
					if (location != null) {
						locations.add(location);
					}
				}

				incident.setLocations(locations);
			} else {
				log.debug("Rejecting incident - no address found {tweetId:{}}",
						incident.tweetId);
				return null;
			}
		}
		// I guess trust the incoming stream
		ofy().save().entities(incident);
		log.debug("Inserted incident - {}", incident.toString());

		// hope that this is asynchronous??
		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		log.debug("There are {} channelKeys", channelKeys.size());
		for (String channelKey : channelKeys) {
			log.debug("Sending message to {}", channelKey);

			// make sure to use gson and not just toString - don't want to lose
			// time zone info in dates
			String json = gson.toJson(incident);
			channelService.sendMessage(new ChannelMessage(channelKey, json));
		}

		return incident;
	}

//	@ApiMethod(name = "primeStatuses", scopes = { API_EMAIL_SCOPE }, clientIds = { API_EXPLORER_CLIENT_ID, SERVICE_CLIENT_ID })
//	public void primeStatuses(@Named("screenNames") String[] screenNames,
//			@Named("count") int count, User user) throws TwitterException,
//			IOException, OAuthRequestException {
//		checkUser(user);
//
//		if (count > 500) {
//			throw new IllegalArgumentException("No more than 500 allowed");
//		}
//
//		for (String screenName : screenNames) {
//			for (Status status : TwitterFactory.getSingleton().getUserTimeline(
//					screenName, new Paging(1, count))) {
//				log.debug("Priming tweet {}", status.getId());
//				Incident incident = new Incident(status);
//				this.insertIncident(incident, user);
//			}
//		}
//	}

	/**
	 * Throw an exception if not a valid user. We will consider
	 * example@example.com to be a valid user.
	 * 
	 * @param user
	 * @throws OAuthRequestException
	 */
	private void checkUser(User user) throws OAuthRequestException {
		if (user == null) {
			throw new OAuthRequestException(
					"Missing OAuth2 credentials.");
		} else if (!user.getEmail().equals("example@example.com")) {
			throw new OAuthRequestException(String.format(
					"OAuth credentials for %s are not valid.", user.getEmail()));
		}
	}

	/**
	 * This method is responsible for setting up the {@link ChannelService}
	 * which will push updates to users as new tweets come in. Clients call this
	 * method when they start. When new incidents come in via
	 * {@link #insertIncident(Incident)}, they are also published on this
	 * channel.
	 * 
	 * @param req
	 * @return
	 * @throws IOException
	 */
	@ApiMethod(name = "init")
	public InitialState init(HttpServletRequest req) throws IOException {
		// set up a Channel
		ChannelService channelService = ChannelServiceFactory
				.getChannelService();
		String uuid = UUID.randomUUID().toString();
		String token = channelService.createChannel(uuid);
		this.channelKeys.add(token);
		log.debug("Created token {}", token);

		// include the last 18 hours results that we have stored away
		int hours = 18;
		List<Incident> incidents = ofy()
				.load()
				.type(Incident.class)
				.filter("createdAt > ",
						new Date(System.currentTimeMillis() - hours * 3600000))
				.limit(128).list();

		// not much going on? go however far back as necessary
		if (incidents.size() < 12) {
			incidents = ofy().load().type(Incident.class).order("-createdAt")
					.limit(12).list();
		}

		log.debug("Returning {} incidents", incidents.size());

		return new InitialState(token, incidents);
	}
}
