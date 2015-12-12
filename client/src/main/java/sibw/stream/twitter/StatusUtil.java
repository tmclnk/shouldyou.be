package sibw.stream.twitter;
import java.util.Calendar;

import sibw.sibw.model.Incident;
import twitter4j.Status;

import com.google.api.client.util.DateTime;
public class StatusUtil {
    /**
     * Handle the mundane copying of status fields to incident fields, esp. 
     * accounting for timezone information.
     */
    public static Incident statusToIncident(Status status){
        Incident incident = new Incident();
        incident.setName(status.getUser().getName());
        incident.setScreenName(status.getUser().getScreenName());
        
        //twitter's json response is a text string like {"created_at": "Sun Aug 31 04:51:41 +0000 2014"}
        //so when you do toString on the value from status.getCreateDate() (a java.util.Date)
        //it will print "Sat Aug 30 23:51:41 CDT 2014" which is to say, -5 hours from zulu (since
        //I'm in central time); remember that a java.util.Date has no timezone information
        //and what is printed in a toString is just the system's timezone; this is all ok *as long as you
        //pass that timezone along* (otherwise that information is lost in just sending java.util.Date)
        incident.setCreatedAt(new DateTime(status.getCreatedAt(), Calendar.getInstance().getTimeZone()));
        incident.setText(status.getText());
        incident.setTweetId(status.getId());
        
        return incident;
    }
}
