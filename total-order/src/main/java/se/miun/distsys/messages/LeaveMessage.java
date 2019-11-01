package se.miun.distsys.messages;

public class LeaveMessage extends Message{

	private static final long serialVersionUID = 2811362043854037709L;
	
	public LeaveMessage(Long userId, String userName) {
		this.userId = userId;
		this.userName = userName;
		this.message = "Hay!, " + this.userName + " left the chat";
	}
}
