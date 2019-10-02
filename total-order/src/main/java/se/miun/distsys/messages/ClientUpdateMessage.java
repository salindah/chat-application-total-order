package se.miun.distsys.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientUpdateMessage extends Message {
	
	private static final long serialVersionUID = 6255823412000246032L;

	public ClientUpdateMessage(List<String> list) {
		this.clientList = list;
	}
	
	private UUID userId;
	
	private Map<UUID, Integer> vectorClock = new HashMap<UUID, Integer>();
	
	private List<String> clientList = new ArrayList<String>();

	public List<String> getClientList() {
		return clientList;
	}

	public void setClientList(List<String> clientList) {
		this.clientList = clientList;
	}	

	public Map<UUID, Integer> getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(Map<UUID, Integer> vectorClock) {
		this.vectorClock = vectorClock;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getMessage() {
		
		StringBuffer buffer = new StringBuffer("\n****** Active users at the moment *********\n");
		for(String client : clientList) {
			buffer.append( client + "\n");
		}
		buffer.append("*********************************************\n");
		return buffer.toString();			
	}
	
}
