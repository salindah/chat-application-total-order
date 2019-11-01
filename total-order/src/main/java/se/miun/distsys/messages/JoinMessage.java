package se.miun.distsys.messages;

public class JoinMessage extends Message{

	private static final long serialVersionUID = -8884788798663885277L;
		
	public JoinMessage(Long userId, String userName) {	
		this.userId = userId;
		this.userName = userName;
		this.message = "Hay!, " + this.userName + " joined to the chat";
	}
}
