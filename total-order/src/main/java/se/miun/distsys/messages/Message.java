package se.miun.distsys.messages;

import java.io.Serializable;

public class Message implements Serializable{

	private static final long serialVersionUID = -2228581240726421907L;
	
	protected Long userId;
	
	protected String userName;
	
	protected String message;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}	
}
