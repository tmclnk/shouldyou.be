package be.shouldyou.geocode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressExtractor {
    private static final String[] SUFFIXES = { "ct", "court", "st", "street", "str","ave", "avenue", "blvd", "boulevard", "rd", "hwy", "highway", "plz","cir","plaza","circle" };
    private static final Pattern SUFFIX_PATTERN = Pattern.compile(String.format("(%s)", StringUtils.join(SUFFIXES, "|")), Pattern.CASE_INSENSITIVE);

    private static Logger log = LoggerFactory.getLogger(AddressExtractor.class);

    public static void main(String[] args) throws IOException {
        log.debug("Extracting (ctrl-D to finish)...");
        if (args.length > 0) {
            log.error("Nope.");
            throw new IOException("Nope.  Pass input through stdin.");
        }

        /* read data one line at a time from stdin */
        final Scanner console = new Scanner(System.in);

        while (console.hasNextLine()) {
            String line = console.nextLine();
            List<String> addresses = extractAddresses(line);

            for (final String addr : addresses) {
                System.out.println(addr);
            }

        }
        console.close();

        log.debug("Done.");
    }

    /**
     * Return a list of addresses from the given text. For simplicity, assumes
     * that the text all occurs on a single line.
     * 
     * @formatter:off <pre>
     * Fontenelle Blvd and Ames Ave
     * 11502 Burt Street
     * 8262 Iowa St
     * 8262 Iowa Street
     * 96th and West
     * 33rd and Lake Street
     * </pre>
     * @formatter:on
     * 
     * @param text
     * @return a list of addressy looking things in the text
     */
    public static List<String> extractAddresses(String text) {
        List<String> addr = new ArrayList<>();

        Pattern pattern = Pattern.compile("[-]?([0-9]++\\w?\\w?(\\s[&\\w]+)+)", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            final String match = matcher.group(1); //don't grab the leading "-" (if there is one)
            final String[] words = match.split("\\s+");
            boolean reject = false;
            
            String address = match; //just default it to the original and change as necessary
            
            if (words.length > 4 && !isStreetType(words[words.length - 1])) {
                address = StringUtils.join(ArrayUtils.subarray(words, 0, 3), " ");
                log.debug("First part reduction because of:" + words[words.length - 1]);
            } 
            
            if (isInteger(words[0]) && !isStreetType(words[words.length - 1])) {
                //don't allow things like "3 intoxicated males"
                log.debug("<number> <adjective> <noun> exclusion at " + words[0] + " ... " + words[words.length - 1]);
                reject = true;
            }
            
            if(!reject){
                addr.add(address);
            }
        }

        addr.addAll(extractSuffixStreetNames(text));
        return addr;
    }

    private static boolean isStreetType(String s){
        return SUFFIX_PATTERN.matcher(s).find();
    }
    
    private static boolean isInteger(String s) {
        return s.matches("[0-9]+");
    }

    /**
     * Try to catch addresses like "Fontenelle Blvd and Ames Ave"
     */
    private static List<String> extractSuffixStreetNames(String text) {
        List<String> addr = new ArrayList<>();

        Pattern pattern = Pattern.compile("[-]?(\\w+\\s+" + SUFFIX_PATTERN + "\\s+and\\s+\\w+\\s+" + SUFFIX_PATTERN + ")", Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String match = matcher.group(1);//ignore any leading "-"
            addr.add(match);
        }

        return addr;
    }

}
