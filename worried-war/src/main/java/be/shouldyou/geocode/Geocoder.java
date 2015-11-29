package be.shouldyou.geocode;

import static java.net.URLEncoder.encode;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.shouldyou.geocode.json.Location;
import be.shouldyou.geocode.json.Result;

import com.google.gson.Gson;

public class Geocoder {
    private static final Logger log = LoggerFactory.getLogger(Geocoder.class);
    private static final String API_KEY = System.getProperty("geocodingapikey");
    private static final Map<String, String> cache = new HashMap<>();
    
    static {
    	if (API_KEY == null){
    		log.error("No geocodingapikey found in System Properties.  "
    				+ "Failing to set geocodingapikey will result in Incident "
    				+ "insertion failures. ");
//    		"Please set this in appengine-web.xml:\n\n"
//    				+ "<system-properties>\n"
//    				+ "\t<property name=\"geocodingapikey\" value=\"YOUR_KEY_HERE\" />\n"
//    				+ "</system-properties>\n");
    	} else {
    		log.info("Using geocodingapikey={}", API_KEY);
    	}
    }

    private static String geocode(String streetAddress) throws IOException {
        if (cache.get(streetAddress) == null) {
            /* REST-ish call the google geocode API */
            String baseURL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
            
            String address = encode(streetAddress, "UTF-8");
            String components = encode("locality:Omaha|administrative_area:NE|country:US", "UTF-8");

            URL url = new URL(baseURL + String.format("%s&components=%s&key=%s", address, components, API_KEY));
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            /* get the response body using the stupid scanner trick */
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
            try {
                if (!scanner.hasNext()) {
                    // TODO figure out what went wrong
                    throw new RuntimeException("Possibly there was no response body.");
                } else {
                    String json = scanner.next();
                    cache.put(streetAddress, json);
                }
            } finally {
                scanner.close();
            }
        }
        return cache.get(streetAddress);

    }

    public static Location getLocationFromStreetAddress(String streetAddress) throws IOException {
        //String json = geocode(streetAddress + ",Omaha,NE");
        String json = geocode(streetAddress);
        System.err.println(json);
        Gson gson = new Gson();
        Result result = gson.fromJson(json, Result.class);
        
        
        Location loc = result.getResults().get(0).getGeometry().getLocation();
        if (loc.getLat() == null || loc.getLng() == null) {
            return null;
        } else {
            return loc;
        }
    }

    public static void main(String[] args) throws IOException {
        log.debug("Extracting (ctrl-D to finish)...");
        if (args.length > 0) {
            log.error("Nope.");
            throw new IOException("Nope.  Pass input through stdin.");
        }

        /* read data one line at a time from stdin */
        final Scanner console = new Scanner(System.in);

        while (console.hasNextLine()) {
            String addressInput = console.nextLine();
            String json = geocode(addressInput);
            System.out.println(json);
        }
        console.close();

        log.debug("Done.");
    }
}

