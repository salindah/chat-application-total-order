package se.miun.distsys.messages;

public class LeaveMessage extends Message{

	private static final long serialVersionUID = 2811362043854037709L;

	private String message = "";
	
	private String userName = "";
	
	public LeaveMessage(Long userId, String userName) {
		this.userId = userId;
		this.userName = userName;
		this.message = "Hay!, " + this.userName + " left the chat";
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
