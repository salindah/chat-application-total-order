package se.miun.distsys;

import java.util.HashMap;
import java.util.Map;

import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;

public class ActiveUserHandler {
	 		
	private Map<Long, String> activeUserMap = new HashMap<Long, String>();
	

	public void addClient(Long userId, String userName) {
		this.activeUserMap.put(userId, userName);
	}
	
	public void removeClient(Long userId) {
		
		if(this.activeUserMap.containsKey(userId)) {			
			this.activeUserMap.remove(userId);
		}
	}

	
	public void addClientDescription(JoinMessage joinMessage) {	
		joinMessage.setMessage(joinMessage.getMessage() + getDescription());
	}
	
	public void addClientDescription(LeaveMessage leaveMessage) {	
		leaveMessage.setMessage(leaveMessage.getMessage() + getDescription());
	}	
	
	public Map<Long, String> getActiveUserMap() {
		return activeUserMap;
	}

	public void setActiveUserMap(Map<Long, String> activeUserMap) {
		this.activeUserMap = activeUserMap;
	}

	public String getDescription() {
		StringBuffer buffer = new StringBuffer("\n******** Active users at the moment *************\n");
		for(String client : activeUserMap.values()) {
			buffer.append( client + "\n");
		}
		buffer.append("*****************************************************\n");
		return buffer.toString();		
	}	
}
