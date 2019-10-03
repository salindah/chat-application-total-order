package se.miun.distsys;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;
import java.util.UUID;

import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.LeaveMessageListener;
import se.miun.distsys.listeners.UpdateMessageListener;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.ClientUpdateMessage;
import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;
import se.miun.distsys.messages.Message;
import se.miun.distsys.messages.MessageSerializer;

public class GroupCommuncation {
	
	private int datagramSocketPort = 8091; //You need to change this!		
	DatagramSocket datagramSocket = null;	
	boolean runGroupCommuncation = true;
	boolean clientListUpdated = false;
	MessageSerializer messageSerializer = new MessageSerializer();
	
	//Listeners
	ChatMessageListener chatMessageListener = null;
	JoinMessageListener joinMessageListener = null;
	LeaveMessageListener leaveMessageListener = null;
	UpdateMessageListener updateMessageListerner = null;
	ActiveUserHandler activeUserHandler = null;
	
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
																
				}
			} else if (message instanceof JoinMessage) {
				JoinMessage joinMessage = (JoinMessage) message;
				if(joinMessageListener != null) {					
					activeUserHandler.addClient(joinMessage.getUserName());
					
					joinMessageListener.onIncomingJoinMessage(joinMessage);							
				}
				
			} else if (message instanceof LeaveMessage) {
				LeaveMessage leaveMessage = (LeaveMessage) message;
				if(leaveMessageListener != null) {
					activeUserHandler.removeClient(leaveMessage.getUserName());					
					leaveMessageListener.onIncomingLeaveMessage(leaveMessage);
				}				
			} else if (message instanceof ClientUpdateMessage) {
				ClientUpdateMessage clientUpdateMessage = (ClientUpdateMessage) message;				
				if(!clientListUpdated) {
					activeUserHandler.setActiveUserList(clientUpdateMessage.getClientList());
					updateMessageListerner.onIncomingUpdateMessage(clientUpdateMessage);
					clientListUpdated = true;
				}
											
			} else {				
				System.out.println("Unknown message type");
			}			
		}		
	}	
	
	class ChatBotThread extends Thread {
		
		private ChatMessage chatMessage;
		
		private Long userId;
		
		public ChatBotThread(ChatMessage chatMessage, Long userId) {		
			this.chatMessage = chatMessage;
			this.userId = userId;
		}
		
		@Override
		public void run() {
			
			try {
										
				byte[] sendData = messageSerializer.serializeMessage(chatMessage);
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), datagramSocketPort);
							
				Thread.sleep(1000 * getRandom(1, 5));				
				datagramSocket.send(sendPacket);
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
			for(int i = 1; i <= amount; i++) {				
				ChatMessage chatMessage = new ChatMessage(this.userId, this.userName, i);
				//Immediately deliver to his own node.
				chatMessageListener.onIncomingChatMessage(chatMessage);	
				
				ChatBotThread thread = new ChatBotThread(chatMessage, this.userId);
				thread.start();				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public void sendChatMessage(String chat) {
		try {
			ChatMessage chatMessage = new ChatMessage(this.userId, this.userName, chat);
			//Immediately deliver to his own node.
			chatMessageListener.onIncomingChatMessage(chatMessage);	
			
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
			ClientUpdateMessage updateMessage = new ClientUpdateMessage(activeUserHandler.getActiveUserList());
			updateMessage.setUserId(userId);						
			byte[] sendData = messageSerializer.serializeMessage(updateMessage);
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}	
}
