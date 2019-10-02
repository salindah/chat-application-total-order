package se.miun.distsys.messages;

import java.util.UUID;

public class JoinMessage extends Message{

	private static final long serialVersionUID = -8884788798663885277L;

	private String message = "";
	
	private String userName = "";
	
	private UUID userId;
	
	public JoinMessage(UUID userId, String userName) {	
		this.userId = userId;
		this.userName = userName;
		this.message = "Hay!, " + this.userName + " joined to the chat";
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
	
	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public void appendMessage(String message) {
		this.message = this.message.concat(" : " + message);
	}
}
