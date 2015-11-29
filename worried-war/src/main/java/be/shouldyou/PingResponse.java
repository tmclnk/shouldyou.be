package be.shouldyou;

import java.util.Date;

import com.google.appengine.api.users.User;

/**
 * JavaBean used for testing Cloud Endpoint API.
 */
public class PingResponse {
	private final User user;
	private String geocoderApiStatus;
	private String status;

	public PingResponse(User user){
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}

	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status){
		this.status = status;
	}

	public boolean isAuthenticated(){
		return user != null;
	}
	
	public Date getTime(){
		return new Date();
	}

	public String getGeocoderApiStatus() {
		return geocoderApiStatus;
	}

	public void setGeocoderApiStatus(String geocoderApiStatus) {
		this.geocoderApiStatus = geocoderApiStatus;
	}
}
