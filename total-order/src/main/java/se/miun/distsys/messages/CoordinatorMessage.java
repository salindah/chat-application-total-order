package se.miun.distsys.messages;

public class CoordinatorMessage extends Message{

	private static final long serialVersionUID = -3034203357675213856L;
	
	public CoordinatorMessage(Long userId) {
		this.userId = userId;
	}
	
	public String getMessage() {
		return "Coordinator message from : " + this.userId;
	}
}
