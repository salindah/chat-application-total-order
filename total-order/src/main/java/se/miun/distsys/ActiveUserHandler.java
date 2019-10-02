package se.miun.distsys;

import java.util.ArrayList;
import java.util.List;

import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;

public class ActiveUserHandler {
	 	
	
	private List<String> activeUserList = new ArrayList<String>();
	
	
	public List<String> getActiveUserList() {
		return activeUserList;
	}

	public void setActiveUserList(List<String> activeUserList) {
		this.activeUserList = activeUserList;
	}

	public void addClient(String userId) {
		this.activeUserList.add(userId);
	}
	
	public void removeClient(String userId) {
		int index = this.activeUserList.indexOf(userId);
		if(index >= 0) {
			this.activeUserList.remove(index);
		}
	}
	
	public int getIndex(String userId) {
		return this.activeUserList.indexOf(userId);
	}
	
	public void addClientDescription(JoinMessage joinMessage) {	
		joinMessage.setMessage(joinMessage.getMessage() + getDescription());
	}
	
	public void addClientDescription(LeaveMessage leaveMessage) {	
		leaveMessage.setMessage(leaveMessage.getMessage() + getDescription());
	}
	
	public String getDescription() {
		StringBuffer buffer = new StringBuffer("\n******** Active users at the moment *************\n");
		for(String client : activeUserList) {
			buffer.append( client + "\n");
		}
		buffer.append("*****************************************************\n");
		return buffer.toString();		
	}	
}
