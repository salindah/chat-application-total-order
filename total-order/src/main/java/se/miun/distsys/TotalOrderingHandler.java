package se.miun.distsys;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.SequenceMessage;

public class TotalOrderingHandler {

	private GroupCommuncation parent;
	
	private Long sequenceNumber = 0L;	
	
	private Map<Long, ChatMessage> holdBackQueue = new ConcurrentHashMap<Long, ChatMessage>();
	
	public TotalOrderingHandler(GroupCommuncation parent) {
		this.parent = parent;
	}
	
	public void onIncomingChatMessage(ChatMessage chatMessage, boolean isCoordinator) {
				
		if(isCoordinator){	
			//Deliver message
			parent.chatMessageListener.onIncomingChatMessage(chatMessage);
			
			//Multicast the sequence message.
			SequenceMessage sequenceMessage = new SequenceMessage();
			sequenceMessage.setSequenceNumber(this.sequenceNumber);
			sequenceMessage.setUniqueMessageId(chatMessage.getUniqueId());
			parent.sendSequenceMessage(sequenceMessage);			
			sequenceNumber++;						
		} else {
			holdBackQueue.put(chatMessage.getUniqueId(), chatMessage);
		} 
		
	}
	
	
	public void onSequenceMessageReceived(SequenceMessage sequenceMessage, boolean isCoordinator) {
		
		if(!isCoordinator) {
			if(sequenceMessage.getSequenceNumber().equals(this.sequenceNumber)) {		
				ChatMessage chatMessage = this.holdBackQueue.get(sequenceMessage.getUniqueMessageId());
				if(chatMessage != null) {
					deleiverMessage(chatMessage, sequenceMessage.getUniqueMessageId());
				}
			}
		}		
	}
	
	
	private void deleiverMessage(ChatMessage chatMessage, Long uniqueId) {
		
		parent.chatMessageListener.onIncomingChatMessage(chatMessage);
		this.holdBackQueue.remove(uniqueId);
		this.sequenceNumber++;
	}

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}	
}
