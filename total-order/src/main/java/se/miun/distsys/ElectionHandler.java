package se.miun.distsys;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import se.miun.distsys.messages.AnswerMessage;
import se.miun.distsys.messages.CoordinatorMessage;
import se.miun.distsys.messages.ElectionMessage;

public class ElectionHandler {

	
	private static Long COORDINATOR_MSG_TIMEOUT = 5000L;
	
	private static Long ANSWER_MSG_TIMEOUT = 3000L;
	
	private Long userId;
	
	private Long coordinatorId = null;
	
	private LocalTime electionStartTime;
	
	private List<AnswerMessage> answerMessageList = Collections.synchronizedList(new ArrayList<AnswerMessage>());
	
	private Map<Long, String> activeUserMap;
	
	private GroupCommuncation parent;
	
	//private boolean electionSessionStarted = false;
	
	private boolean electionStarted = false;
	
	
		
	public ElectionHandler(Long userId, GroupCommuncation parent) {
		this.parent = parent;
		this.userId = userId;		
	}
		
	public void onElectionMessageReceived(ElectionMessage electionMessage) {		
		//Ensure election message is received only to processes which higher ID		
		if(isValidElectionMessage(electionMessage)) {				
			System.out.println(electionMessage.getMessage());
			parent.getElectionListener().onElectionMessage(electionMessage.getMessage());
			
			coordinatorId = null;
			parent.sendAnswerMessage();
			if(!electionStarted) {
				startElection();	
				
				System.out.println("Election started for the req from : " + electionMessage.getUserId());
				parent.getElectionListener().onElectionMessage("Election started for the req from : " + electionMessage.getUserId());
			}
		}else {
			System.out.println("Election message not valid");
		}
	}
	
	public void onAnswerMessageReceived(AnswerMessage answerMessage) {	
		
		synchronized(this) {
			
			if(!answerMessage.getUserId().equals(this.userId)) {
				answerMessageList.add(answerMessage);

				System.out.println(answerMessage.getMessage());
				parent.getElectionListener().onElectionMessage(answerMessage.getMessage());
			}			
		}		
	}
	
	public void onCoordinatorMessageReceived(CoordinatorMessage coordinatorMessage) {		
			
		synchronized(this) {
			
			if(!coordinatorMessage.getUserId().equals(this.userId)) {
				this.coordinatorId = coordinatorMessage.getUserId();			
				electionStarted = false;				
				System.out.println(coordinatorMessage.getMessage());
				parent.getElectionListener().onElectionMessage(coordinatorMessage.getMessage());
			}			
		}		
	}
	
	private boolean isValidElectionMessage(ElectionMessage electionMessage) {
		
		if(this.userId > electionMessage.getUserId()) {
			return true;
		}
		return false;
	}
	
	
	class AnswerMessageChecker extends Thread{
		
		@Override
		public void run() {
			
			synchronized(this) {
				while(answerMessageList.size() == 0) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {			
						e.printStackTrace();
					}
					Long duration = Duration.between(electionStartTime, LocalTime.now()).toMillis();
					if(duration > ANSWER_MSG_TIMEOUT) {
						System.out.println("Yeees");
						sendCoordinatorMessage();
						break;
					} else {
						System.out.println("Nooo");
					}
					parent.getElectionListener().onElectionMessage("Answer count : " + answerMessageList.size());
				}
				
				
				if(answerMessageList.size() > 0) {
					
					for(AnswerMessage message : answerMessageList) {
						parent.getElectionListener().onElectionMessage("Answer msg from : " + message.getUserId());
					}
					
					LocalTime start = LocalTime.now();
					while(coordinatorId == null) {
						
						try {
							Thread.sleep(300);
						} catch (InterruptedException e) {			
							e.printStackTrace();
						}
						Long duration = Duration.between(start, LocalTime.now()).toMillis();
						if(duration > COORDINATOR_MSG_TIMEOUT) {
							parent.getElectionListener().onElectionMessage("Coordinate time out");
							startElection();
							break;
						}
					}
				}	
			}	
		}
	}
	
	public void startElection() {
		
		if( hasHighestId()) {
			sendCoordinatorMessage();
			parent.getElectionListener().onElectionMessage("I am the coordinator");
		} else {
			electionStartTime = LocalTime.now();
			answerMessageList.clear();
			parent.sendElectionMessage();			
			electionStarted = true;
			coordinatorId = null;
			AnswerMessageChecker checker = new AnswerMessageChecker();
			checker.start();	
			parent.getElectionListener().onElectionMessage("Started an election");
		}		
	}
	
	public void sendCoordinatorMessage() {
		this.coordinatorId = this.userId;		
		parent.sendCoordinatorMessage();	
	}
	
	public boolean isCoordinator() {
		if(this.coordinatorId.equals(this.userId)) {
			return true;
		}
		return false;
	}
	
	public void sendAnswerMessage() {
		parent.sendAnswerMessage();
	}

	private boolean hasHighestId() {
		Long highest = this.userId;
		if( this.activeUserMap != null && !this.activeUserMap.isEmpty()) {			
			for(Long id :this.activeUserMap.keySet()) {
				if(id > highest) {
					highest = id;
				}
			}
		}
		
		if( highest.equals(this.userId)) {
			return true;
		}
		return false;
	}
	
	
	public Long getCoordinatorId() {
		return coordinatorId;
	}

	public void setCoordinatorId(Long coordinatorId) {
		this.coordinatorId = coordinatorId;
	}

	public Map<Long, String> getActiveUserMap() {
		return activeUserMap;
	}

	public void setActiveUserMap(Map<Long, String> activeUserMap) {
		this.activeUserMap = activeUserMap;
	}		
}
