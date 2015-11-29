package be.shouldyou;

import java.util.Date;

import com.google.appengine.api.users.User;

/**
 * JavaBean used for testing Cloud Endpoint API.
 */
public class PingResponse {
	User user;

	public PingResponse(User user){
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getStatus() {
		return "OK";
	}

	public boolean isAuthenticated(){
		return user != null;
	}
	
	public Date getTime(){
		return new Date();
	}
}
