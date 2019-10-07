package se.miun.distsys.messages;

import java.util.Map;

public class ClientUpdateMessage extends Message {
	
	private static final long serialVersionUID = 6255823412000246032L;

	private Map<Long, String> activeUserMap;
	
	
	public ClientUpdateMessage(Long userId, Map<Long, String> map) {
		this.userId = userId;
		this.activeUserMap = map;
	}
		
	public Map<Long, String> getActiveUserMap() {
		return activeUserMap;
	}


	public void setActiveUserMap(Map<Long, String> activeUserMap) {
		this.activeUserMap = activeUserMap;
	}

	public String getMessage() {
		
		StringBuffer buffer = new StringBuffer("\n****** Active users at the moment *********\n");
		for(String client : activeUserMap.values()) {
			buffer.append( client + "\n");
		}
		buffer.append("*********************************************\n");
		return buffer.toString();			
	}	
}
