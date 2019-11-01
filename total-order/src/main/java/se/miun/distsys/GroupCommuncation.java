package se.miun.distsys;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.ElectionListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.LeaveMessageListener;
import se.miun.distsys.listeners.UpdateMessageListener;
import se.miun.distsys.messages.AnswerMessage;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.ClientUpdateMessage;
import se.miun.distsys.messages.CoordinatorMessage;
import se.miun.distsys.messages.ElectionMessage;
import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;
import se.miun.distsys.messages.Message;
import se.miun.distsys.messages.MessageSerializer;
import se.miun.distsys.messages.SequenceMessage;

public class GroupCommuncation {
	
	private int datagramSocketPort = 8091; 
	private int sequenceServerPort = 8092;
	DatagramSocket datagramSocket = null;	
	boolean runGroupCommuncation = true;
	boolean clientListUpdated = false;
	MessageSerializer messageSerializer = new MessageSerializer();
	
	//Listeners
	ChatMessageListener chatMessageListener = null;
	JoinMessageListener joinMessageListener = null;
	LeaveMessageListener leaveMessageListener = null;
	UpdateMessageListener updateMessageListerner = null;
	ElectionListener electionListener = null;
	SequenceServer sequenceServer = null;
	//Handlers which responsible for different tasks.
	ActiveUserHandler activeUserHandler = null;
	ElectionHandler electionHandler = null;
	TotalOrderingHandler totalOrderingHandler = null;
	private Long sequenceNumber = 0L;
	
	
	Long userId;
	String userName = "";
	
	public GroupCommuncation() {		
	}	
	
	public GroupCommuncation(Long userId, String userName) {			
		try {
			this.userId = userId;
			this.userName = userName;
			runGroupCommuncation = true;	
			activeUserHandler = new ActiveUserHandler();
			sequenceServer = new SequenceServer();
			electionHandler = new ElectionHandler(this.userId, this);	
			totalOrderingHandler = new TotalOrderingHandler(this);
			datagramSocket = new MulticastSocket(datagramSocketPort);						
			RecieveThread rt = new RecieveThread();
			rt.start();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		runGroupCommuncation = false;		
	}
	
	public void startSequenceServer() {
		
		this.sequenceServer.setSequenceNumber(this.sequenceNumber);
		this.sequenceServer.start(this.sequenceServerPort, sequenceNumber);
	}
	
	public void stopSequenceServer() {
		this.sequenceServer.stop();
	}
	

	class RecieveThread extends Thread{
		
		@Override
		public void run() {
			byte[] buffer = new byte[65536];		
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			
			while(runGroupCommuncation) {
				try {
					datagramSocket.receive(datagramPacket);										
					byte[] packetData = datagramPacket.getData();					
					Message recievedMessage = messageSerializer.deserializeMessage(packetData);					
					handleMessage(recievedMessage);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
				
		private void handleMessage (Message message) {
			
			if(message instanceof ChatMessage) {				
				ChatMessage chatMessage = (ChatMessage) message;				
				if(chatMessageListener != null){											
					//chatMessageListener.onIncomingChatMessage(chatMessage);	
					totalOrderingHandler.onIncomingChatMessage(chatMessage, electionHandler.isCoordinator());
				}
			} else if (message instanceof JoinMessage) {
				JoinMessage joinMessage = (JoinMessage) message;
				if(joinMessageListener != null) {					
					activeUserHandler.addClient(joinMessage.getUserId(), joinMessage.getUserName());						
					joinMessageListener.onIncomingJoinMessage(joinMessage);							
				}
				
			} else if (message instanceof LeaveMessage) {
				LeaveMessage leaveMessage = (LeaveMessage) message;				
				if(leaveMessageListener != null && !leaveMessage.getUserId().equals(userId)) {
					activeUserHandler.removeClient(leaveMessage.getUserId());					
					leaveMessageListener.onIncomingLeaveMessage(leaveMessage);
					electionHandler.setActiveUserMap(activeUserHandler.getActiveUserMap());
					electionHandler.startElection();
				}				
			} else if (message instanceof ClientUpdateMessage) {
				ClientUpdateMessage clientUpdateMessage = (ClientUpdateMessage) message;				
				if(!clientListUpdated) {
					activeUserHandler.setActiveUserMap(clientUpdateMessage.getActiveUserMap());	
					totalOrderingHandler.setSequenceNumber(clientUpdateMessage.getSequenceNumber());
					updateMessageListerner.onIncomingUpdateMessage(clientUpdateMessage);
					clientListUpdated = true;
					electionHandler.setActiveUserMap(clientUpdateMessage.getActiveUserMap());
					electionHandler.startElection();					
				}											
			} else if (message instanceof ElectionMessage) {
				
				ElectionMessage electionMessage = (ElectionMessage) message;
				electionHandler.onElectionMessageReceived(electionMessage);
				
			} else if(message instanceof AnswerMessage) {
				
				AnswerMessage answerMessage = (AnswerMessage) message;
				electionHandler.onAnswerMessageReceived(answerMessage);	
				
			} else if(message instanceof CoordinatorMessage) {	
				
				CoordinatorMessage coordinatorMessage = (CoordinatorMessage) message;
				electionHandler.onCoordinatorMessageReceived(coordinatorMessage);
				
			} else if(message instanceof SequenceMessage) {	
				
				SequenceMessage sequenceMessage = (SequenceMessage) message;
				totalOrderingHandler.onSequenceMessageReceived(sequenceMessage, electionHandler.isCoordinator());				
			} 					
			else {				
				System.out.println("Unknown message type");
			}			
		}		
	}	
	
	class ChatBotThread extends Thread {
		
		private Long userId;
		
		private String userName;
		
		private int amount;
				
		public ChatBotThread(Long userId, String userName, int amount) {		
			this.userId = userId;	
			this.userName = userName;
			this.amount = amount;
		}
		
		@Override
		public void run() {
			
			try {
				for(int i = 1; i <= amount; i++) {	
					
					ChatMessage chatMessage = new ChatMessage(this.userId, this.userName, i);
					
					byte[] sendData = messageSerializer.serializeMessage(chatMessage);
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
								
					Thread.sleep(1000);				
					datagramSocket.send(sendPacket);
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		private int getRandom(int min, int max) {			
			Random rand = new Random();
			int randomNum = rand.nextInt((max - min) + 1) + min;
			return randomNum;
		}
	}
	
	public void sendChatMessage(String chat, Integer amount) {
		try {								
				ChatBotThread thread = new ChatBotThread(this.userId, this.userName, amount);
				thread.start();				
						
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void sendChatMessage(String chat) {
		try {
			ChatMessage chatMessage = new ChatMessage(this.userId, this.userName, chat);
			
			byte[] sendData = messageSerializer.serializeMessage(chatMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void sendJoinMessage() {
		try {
			JoinMessage joinMessage = new JoinMessage(this.userId, this.userName);
			byte[] sendData = messageSerializer.serializeMessage(joinMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendLeaveMessage() {
		try {
			LeaveMessage chatMessage = new LeaveMessage(this.userId, this.userName);
			byte[] sendData = messageSerializer.serializeMessage(chatMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendClientUpdateMessage() {
		try {
			ClientUpdateMessage updateMessage = new ClientUpdateMessage(this.userId, activeUserHandler.getActiveUserMap(), this.totalOrderingHandler.getSequenceNumber());									
			byte[] sendData = messageSerializer.serializeMessage(updateMessage);
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendElectionMessage() {
		try {
			ElectionMessage electionMessage = new ElectionMessage(this.userId);				
			byte[] sendData = messageSerializer.serializeMessage(electionMessage);
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendAnswerMessage() {
		try {
			AnswerMessage answerMessage = new AnswerMessage(this.userId);				
			byte[] sendData = messageSerializer.serializeMessage(answerMessage);
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendCoordinatorMessage() {
		try {
			CoordinatorMessage coordinatorMessage = new CoordinatorMessage(this.userId);				
			byte[] sendData = messageSerializer.serializeMessage(coordinatorMessage);
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendSequenceMessage(SequenceMessage sequenceMessage) {
		try {						
			byte[] sendData = messageSerializer.serializeMessage(sequenceMessage);
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
			datagramSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void setChatMessageListener(ChatMessageListener listener) {
		this.chatMessageListener = listener;		
	}
	
	public void setJoinMessageListener(JoinMessageListener joinMessageListener) {
		this.joinMessageListener = joinMessageListener;
	}
	
	public void setLeaveMessageListener(LeaveMessageListener leaveMessageListener) {
		this.leaveMessageListener = leaveMessageListener;
	}
	
	public void setUpdateMessageListener(UpdateMessageListener updateMessageListerner) {
		this.updateMessageListerner = updateMessageListerner;
	}
	
	public ElectionListener getElectionListener() {
		return electionListener;
	}

	public void setElectionListener(ElectionListener electionListener) {
		this.electionListener = electionListener;
	}	

	public TotalOrderingHandler getTotalOrderingHandler() {
		return totalOrderingHandler;
	}

	public void setTotalOrderingHandler(TotalOrderingHandler totalOrderingHandler) {
		this.totalOrderingHandler = totalOrderingHandler;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public ElectionHandler getElectionHandler() {
		return electionHandler;
	}

	public void setElectionHandler(ElectionHandler electionHandler) {
		this.electionHandler = electionHandler;
	}

	public SequenceServer getSequenceServer() {
		return sequenceServer;
	}

	public void setSequenceServer(SequenceServer sequenceServer) {
		this.sequenceServer = sequenceServer;
	}		
}
