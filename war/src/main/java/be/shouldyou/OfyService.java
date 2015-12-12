package be.shouldyou;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Gives us our custom version rather than the standard Objectify one. Also
 * responsible for setting up the static OfyFactory instead of the standard
 * ObjectifyFactory.
 * 
 * @author Jeff Schnitzer
 */
public class OfyService {
    static {
        factory().register(Incident.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}