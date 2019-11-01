package se.miun.distsys.messages;


public class ChatMessage extends Message {

	private static final long serialVersionUID = -8618972042183561181L;
	
	private int index; // used for testing purposes.
	
	private Long uniqueId;
	
	public ChatMessage(Long userId, String userName, String message) {
		this.userId = userId;
		this.userName = userName;
		this.message = message;
		this.uniqueId = System.currentTimeMillis();	
	}
	
	public ChatMessage(Long userId, String userName, int index) {
		this.userId = userId;
		this.userName = userName;
		this.index = index;
		this.message = "Test message : " + index;
		this.uniqueId = System.currentTimeMillis();	
	}
	
	public String getMessage() {		
		return  "[ " + this.userName + " ] : " + this.message;
	}
		
	public void printDetails() {
							
	}

	public Long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(Long uniqueId) {
		this.uniqueId = uniqueId;
	}		
}
