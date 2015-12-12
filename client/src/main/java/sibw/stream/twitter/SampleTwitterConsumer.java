package sibw.stream.twitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sibw.sibw.Sibw;
import sibw.sibw.model.Incident;
import sibw.sibw.model.Location;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * Standalone program that subscribes to the sample twitter stream, makes an
 * {@link Incident} out of each message, assigns an arbitrary lat/long value
 * within a box approxmately bounded around Omaha, and calls the (remote)
 * {@link Sibw#insertIncident(Incident)} service. Note that this is kind of a
 * firehose of tweets, so it will bombard the service pretty quickly and should
 * only be run in short bursts.
 */
public class SampleTwitterConsumer {
    private static Random rand = new Random();
    private static final Logger log = LoggerFactory.getLogger(SampleTwitterConsumer.class);

    private static int LIMIT = 1;

    public static void main(String[] args) throws IOException {
        // SET UP THE SERVICE
        final Sibw service = ServiceFactory.getInstance("http://localhost:8080/_ah/api", "worry.p12");

        // SET UP TWITTER STREAMING
        final TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        StatusListener listener = new AbstractStatusListener() {
            int count = 0;

            public void onStatus(Status status) {

                if (++count > LIMIT) {
                    twitterStream.shutdown();
                    return;
                }

                try {

                    Incident incident = StatusUtil.statusToIncident(status);
                    List<Location> locs = new ArrayList<>();

                    for (int i = 0; i < 3; i++) {
                        Location loc = new Location();
                        loc.setLat(41.25 + 0.05 * (rand.nextDouble()) * (rand.nextBoolean() ? 1.0 : -1.0)); // 41.25
                        loc.setLng(-96.0 + 0.10 * (rand.nextDouble()) * (rand.nextBoolean() ? 1.0 : -1.0)); // 96
                                                                                                            // W
                        locs.add(loc);
                    }
                    incident.setLocations(locs);

                    Incident savedIncident = service.insertIncident(incident).execute();
                    if (savedIncident != null) {
                        int locations = savedIncident.getLocations().size();
                        System.out.println("ACCEPTED [locations=" + locations + "]: " + status.toString());
                    } else {
                        System.err.println("REJECTED: " + status.toString());
                    }
                } catch (IOException e) {
                    log.warn("Status failure", e);
                }

            }

        };

        // GO!
        twitterStream.addListener(listener);
        twitterStream.sample();
    }
}
