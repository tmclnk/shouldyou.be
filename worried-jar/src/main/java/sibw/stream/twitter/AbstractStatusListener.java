package sibw.stream.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.StallWarning;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public abstract class AbstractStatusListener implements StatusListener {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public void onException(Exception ex) {
        log.error("onException fired", ex);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        log.warn("onDeletionNotice fired {userId:{}, statusId:{}}", statusDeletionNotice.getUserId(), statusDeletionNotice.getStatusId());

    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        log.warn("onTrackLimitationNotice ({})", numberOfLimitedStatuses);

    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        //TODO what is this?
        log.warn("onScrubGeo");

    }

    @Override
    public void onStallWarning(StallWarning warning) {
        log.error("onStallWarning [{}] {}", warning.getPercentFull(), warning.getMessage());
    }

}
