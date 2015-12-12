package be.shouldyou;

import java.util.List;

public class InitialState {
    public String channel;
    public List<Incident> incidents;

    public InitialState(String channel, List<Incident> incidents) {
        this.channel = channel;
        this.incidents = incidents;
    }

}
