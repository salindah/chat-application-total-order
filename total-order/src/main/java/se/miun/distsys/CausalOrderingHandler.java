package se.miun.distsys;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.ClientUpdateMessage;

public class CausalOrderingHandler {
	
	private UUID userId;
	
	private GroupCommuncation parent;
	
	private List<ChatMessage> holdBackQueue =   Collections.synchronizedList(new ArrayList<ChatMessage>()) ;
	
	private Map<UUID, Integer> vectorClock = new ConcurrentHashMap<UUID, Integer>();
	
	public CausalOrderingHandler(UUID userId, GroupCommuncation parent) {
		this.userId = userId;
		this.parent = parent;
	}
		
	public synchronized void putElement(UUID userId, Integer value) {
		
		Integer existingValue = vectorClock.get(userId);
		if(existingValue != null) {			
			if(value > existingValue) {
				vectorClock.put(userId, value);
			}
		} else {
			vectorClock.put(userId, value);
		}		
	}
	
	public synchronized void updateClock(ClientUpdateMessage updateMessage) {		
		UUID id = updateMessage.getUserId();
		Map<UUID, Integer> clock = updateMessage.getVectorClock();
		putElement(id, clock.get(id));	
		System.out.println(vectorClock);		
	}

	public Map<UUID, Integer> getVectorClock() {
		return vectorClock;
	}

	public void setVectorClock(Map<UUID, Integer> vectorClock) {
		this.vectorClock = vectorClock;
	}	
	
	public synchronized void updateChatMessageWithClock(ChatMessage chatMessage, UUID id) {	
						 
		Integer currentValue = this.vectorClock.get(id);
		this.vectorClock.put(id, (currentValue + 1));
		
		Map<UUID, Integer> clonnedMap = new ConcurrentHashMap<UUID, Integer>(this.vectorClock);
		chatMessage.setVectorClock(clonnedMap);
		chatMessage.printDetails();
	}
	
	private boolean isReadyToDeliver(ChatMessage chatMessage) {
		boolean readyToDeliver = true;
		Map<UUID, Integer> incomingClock = chatMessage.getVectorClock();
		UUID senderId = chatMessage.getUserId();
		for(Entry<UUID, Integer> entry : this.vectorClock.entrySet()) {
			
			UUID key = entry.getKey();
			Integer currentValue = this.vectorClock.get(key);
			Integer incomingValue = incomingClock.get(key);
			if(key.equals(senderId)) {							
				if( incomingValue != (currentValue + 1)) {
					readyToDeliver = false;
					break;
				}
			} else {
				if(incomingValue > currentValue) {
					readyToDeliver = false;
					break;
				}
			}
		}
		return readyToDeliver;
	}
	
	
	public synchronized int getDeliveryCandidateIndex() {
		
		System.out.println("Queue size : " + holdBackQueue.size() );
		int index = -1;
		for(int i = 0; i < holdBackQueue.size(); i++) {			
			if( isReadyToDeliver(holdBackQueue.get(i)) ) {
				index = i;
				break;
			}			
		}
		return index;
	}
		
	public synchronized void deliverMessage(int index) {		
		
		ChatMessage message = holdBackQueue.get(index);
		if(message != null) {			
			parent.chatMessageListener.onIncomingChatMessage(message);				
			Integer currentValue = this.vectorClock.get(message.getUserId());
			this.vectorClock.put(message.getUserId(), (currentValue + 1));						
			holdBackQueue.remove(index);
			System.out.println("Deliver Message : " + message.getChat());
			System.out.println("Own vector : \n" + this.vectorClock.toString());	
		}				
	}
	
	public void processChatMessage(ChatMessage chatMessage) {	
		
		synchronized(this){
		
			this.holdBackQueue.add(chatMessage);
			chatMessage.printDetails();		
			System.out.println("Own vector : \n" + this.vectorClock.toString());
			
			while(true) {
				int index = getDeliveryCandidateIndex();
				System.out.println("Delivery index :" + index);
				if(index >= 0) {
					deliverMessage(index);
				} else {
					break;
				}						
			}		
		}			
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public List<ChatMessage> getHoldBackQueue() {
		return holdBackQueue;
	}

	public void setHoldBackQueue(List<ChatMessage> holdBackQueue) {
		this.holdBackQueue = holdBackQueue;
	}		
}
