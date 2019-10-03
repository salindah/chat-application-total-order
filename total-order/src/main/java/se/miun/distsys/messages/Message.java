package se.miun.distsys.messages;

import java.io.Serializable;

public class Message implements Serializable{

	protected Long userId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}	
}
