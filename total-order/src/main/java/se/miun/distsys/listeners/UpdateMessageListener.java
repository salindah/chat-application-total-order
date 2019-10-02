package se.miun.distsys.listeners;

import se.miun.distsys.messages.ClientUpdateMessage;

public interface UpdateMessageListener {

	public void onIncomingUpdateMessage(ClientUpdateMessage clientUpdateMessage);
	
	public void displayMessage(ClientUpdateMessage clientUpdateMessage);
}
