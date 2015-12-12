package sibw.stream.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sibw.sibw.Sibw;
import sibw.sibw.model.Incident;
import twitter4j.FilterQuery;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;

import com.google.api.client.util.DateTime;

public class ScreenNameTwitterConsumer {
    private static final Logger log = LoggerFactory.getLogger(ScreenNameTwitterConsumer.class);

    public static void main(String[] args) throws IOException, TwitterException {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }

        if (args.length == 0) {
            System.out.println("Enter twitter @ScreenNames to follow and root url to use.");
            System.out.println("Example:");
            System.out.println("java this.jar @MeanStreetsOMA @SoAndSo @SoAndSo2 http://localhost:8080/_ah/api");
            return;
        }

        String rootUrl = null;
        String p12path = null;
        List<String> screenNames = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("@")) {
                screenNames.add(args[i].substring(1));
            } else if (args[i].startsWith("http")) {
                rootUrl = args[i];
            } else if (args[i].endsWith(".p12")) {
                p12path = args[i];
            }
        }

        if (rootUrl == null) {
            System.out.println("No root url specified.");
            return;
        }

        // look up the user ids for the given screen names
        ResponseList<User> users = TwitterFactory.getSingleton().lookupUsers(screenNames.toArray(new String[screenNames.size()]));
        long[] userIdsToFollow = new long[users.size()];
        for (int i = 0; i < userIdsToFollow.length; i++) {
            userIdsToFollow[i] = users.get(i).getId();
        }

        final Sibw service = ServiceFactory.getInstance(rootUrl, p12path);

        // build a stream handler for twitter
        StatusListener listener = new AbstractStatusListener() {
            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                try {
                    service.removeTweet(statusDeletionNotice.getStatusId()).execute();
                    log.debug("DELETED tweet {} ", statusDeletionNotice.getStatusId());
                } catch (IOException e) {
                    log.warn("Delete failure", e);
                }
            }

            public void onStatus(Status status) {
                if (status.isRetweet()) {
                    log.debug("Ignoring retweet:", status.getText());
                } else {
                    try {
                        // when we get a twitter status, convert it to an
                        // incident
                        // and invoke our service
                        Incident incident = new Incident();
                        incident.setName(status.getUser().getName());
                        incident.setScreenName(status.getUser().getScreenName());
                        incident.setCreatedAt(new DateTime(status.getCreatedAt()));
                        incident.setText(status.getText());
                        incident.setTweetId(status.getId());

                        Incident savedIncident = service.insertIncident(incident).execute();
                        if (savedIncident == null) {
                            log.debug("REJECTED by sibw {}", incident.toString());
                        } else {
                            log.debug("ACCEPTED [locations={}] {}", savedIncident.getLocations().size(), incident.toString());
                        }

                    } catch (IOException e) {
                        log.warn("Status failure", e);
                    }
                }
            }
        };

        // start streaming
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        FilterQuery filter = new FilterQuery(userIdsToFollow);
        twitterStream.filter(filter);
    }
}
