package se.miun.distsys.messages;

public class ElectionMessage extends Message{

	private static final long serialVersionUID = 4792732057536689905L;
		
	public ElectionMessage(Long userId) {	
		this.userId = userId;
	}
	
	public String getMessage() {
		return "Election message from : " + this.userId;
	}
}
