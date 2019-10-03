package se.miun.distsys.messages;

import java.util.Map;
import java.util.UUID;

public class ChatMessage extends Message {

	private static final long serialVersionUID = -8618972042183561181L;

	private String chat = "";
	
	private String userName = "";
			
	private Map<UUID, Integer> vectorClock;
	
	private Integer delay;
	
	public ChatMessage(Long userId, String userName, String chat) {
		this.userId = userId;
		this.userName = userName;
		this.chat = chat;
	}
	
	public ChatMessage(Long userId, String userName, int index) {
		this.userId = userId;
		this.userName = userName;
		this.chat = "Test message " + index;
	}
	
	public void updateMessage(int index ) {
		this.chat = "Test message " + index;
	}
	
	public String getChatMessage() {		
		return  "[ " + this.userName + " ] : " + chat;
	}

	public String getChat() {
		return chat;
	}

	public void setChat(String chat) {
		this.chat = chat;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Map<UUID, Integer> getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(Map<UUID, Integer> vectorClock) {
		this.vectorClock = vectorClock;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}
	
	public void printDetails() {
		System.out.println("===========================================");
		System.out.println("Incoming Message : " + this.getChat() );
		System.out.println("Incoming vector : \n" + this.vectorClock.toString());						
	}
	
	
}
