package se.miun.distsys;

import java.net.DatagramPacket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import se.miun.distsys.messages.AnswerMessage;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.CoordinatorMessage;
import se.miun.distsys.messages.ElectionMessage;
import se.miun.distsys.messages.Message;

public class ElectionHandler {

	
	private static Long COORDINATOR_MSG_TIMEOUT = 50000L;
	
	private static Long ANSWER_MSG_TIMEOUT = 3000L;
	
	private Long userId;
	
	private Long coordinatorId = null;
	
	private LocalTime electionStartTime;
	
	private List<AnswerMessage> answerMessageList = new ArrayList<AnswerMessage>();
	
	private GroupCommuncation parent;
	
	//private boolean electionSessionStarted = false;
	
	private boolean electionStarted = false;
		
	public ElectionHandler(Long userId, GroupCommuncation parent) {
		this.parent = parent;
		this.userId = userId;
	}
		
	public void onElectionMessageReceived(ElectionMessage electionMessage) {		
		//Ensure election message is received only to processes which higher ID
		System.out.println(electionMessage.getMessage());
		parent.getElectionListener().onElectionMessage(electionMessage.getMessage());
		if(isValidElectionMessage(electionMessage)) {			
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
		answerMessageList.add(answerMessage);
		System.out.println(answerMessage.getMessage());
		parent.getElectionListener().onElectionMessage(answerMessage.getMessage());
	}
	
	public void onCoordinatorMessageReceived(CoordinatorMessage coordinatorMessage) {		
		
		if(this.coordinatorId == null) {
			this.coordinatorId = coordinatorMessage.getUserId();			
			electionStarted = false;
			System.out.println(coordinatorMessage.getMessage());
			parent.getElectionListener().onElectionMessage(coordinatorMessage.getMessage());
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
			}
			
			if(answerMessageList.size() > 0) {
				LocalTime start = LocalTime.now();
				while(coordinatorId == null) {
					
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {			
						e.printStackTrace();
					}
					Long duration = Duration.between(start, LocalTime.now()).toMillis();
					if(duration > COORDINATOR_MSG_TIMEOUT) {
						startElection();
						break;
					}
				}
			}		
		}
	}
	
	public void startElection() {
		electionStartTime = LocalTime.now();
		answerMessageList.clear();
		parent.sendElectionMessage();			
		electionStarted = true;
		AnswerMessageChecker checker = new AnswerMessageChecker();
		checker.start();	
	}
	
	public void sendCoordinatorMessage() {
		parent.sendCoordinatorMessage();	
	}
	
	public void sendAnswerMessage() {
		parent.sendAnswerMessage();
	}
}
