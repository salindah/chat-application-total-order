import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

import se.miun.distsys.GroupCommuncation;
import se.miun.distsys.listeners.ChatMessageListener;
import se.miun.distsys.listeners.JoinMessageListener;
import se.miun.distsys.listeners.LeaveMessageListener;
import se.miun.distsys.listeners.UpdateMessageListener;
import se.miun.distsys.messages.ChatMessage;
import se.miun.distsys.messages.ClientUpdateMessage;
import se.miun.distsys.messages.JoinMessage;
import se.miun.distsys.messages.LeaveMessage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.border.Border;

import com.github.javafaker.Faker;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.UUID;

import javax.swing.JScrollPane;
import javax.swing.JTextField;

//Skeleton code for Distributed systems 9hp, DT050A

public class WindowProgram implements ChatMessageListener, JoinMessageListener, LeaveMessageListener, UpdateMessageListener, ActionListener {

	JFrame frame;
	JTextPane txtpnChat = new JTextPane();
	JTextPane txtpnMessage = new JTextPane();
	JTextField txtAmount = new JTextField();
	Long userId;
	String userName = "";
	
	GroupCommuncation gc = null;	

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WindowProgram window = new WindowProgram();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public WindowProgram() {
		initializeFrame();

		userName = getUserName();
		userId = getUserId();
		gc = new GroupCommuncation(userId, userName);		
		gc.setChatMessageListener(this);
		gc.setJoinMessageListener(this);
		gc.setLeaveMessageListener(this);
		gc.setUpdateMessageListener(this);						
		frame.setTitle(userName);
		gc.sendJoinMessage();
		System.out.println("Group Communcation Started");
	}

	private void initializeFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	gc.sendLeaveMessage();	
	        	gc.shutdown();
	        }
	        public void windowOpened( WindowEvent e ){
	        	txtpnMessage.requestFocus();
	        }
	    });
		
		//Set up the content pane.
        addComponentsToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
	}
	
	public void addComponentsToPane(Container pane) {
	
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		Border blackline = BorderFactory.createLineBorder(Color.black);
		c.fill = GridBagConstraints.BOTH;
		
		JScrollPane scrollPane = new JScrollPane();		
		scrollPane.setViewportView(txtpnChat);
		txtpnChat.setEditable(false);	
		txtpnChat.setText("--== Group Chat ==--");
		txtpnChat.setPreferredSize(new Dimension(600, 200));		
		c.weightx = 1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.insets = new Insets(5, 5, 5, 5);
		pane.add(scrollPane, c);
		
		txtpnMessage.setText("");
		txtpnMessage.setPreferredSize(new Dimension(600, 100));
		
		txtpnMessage.setBorder(blackline);
		c.weightx = 1;
		c.weighty = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 4;
		pane.add(txtpnMessage, c);
		
		JButton btnSendWithDelay = new JButton("Send Bulk");
		btnSendWithDelay.setPreferredSize(new Dimension(150, 50));
		btnSendWithDelay.addActionListener(this);
		btnSendWithDelay.setActionCommand("send-bulk");
		
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		pane.add(btnSendWithDelay, c);
		
		txtAmount = new JTextField();
		txtAmount.setText("10");
		btnSendWithDelay.setPreferredSize(new Dimension(100, 50));
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		pane.add(txtAmount, c);
		
		JButton btnSendChatMessage = new JButton("Send");
		btnSendChatMessage.setPreferredSize(new Dimension(100, 50));
		btnSendChatMessage.addActionListener(this);
		btnSendChatMessage.setActionCommand("send");
		
		c.weightx = 1;
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		pane.add(btnSendChatMessage, c);
		
		JButton btnClear = new JButton("Print Vector");
		btnClear.setPreferredSize(new Dimension(100, 50));
		btnClear.addActionListener(this);
		btnClear.setActionCommand("clear");
		
		c.weightx = 1;
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		pane.add(btnClear, c);
		
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equalsIgnoreCase("send")) {
			gc.sendChatMessage(txtpnMessage.getText());
			txtpnMessage.setText("");
			txtpnMessage.requestFocus();
		} 		
		
		if (event.getActionCommand().equalsIgnoreCase("clear")) {
			txtpnChat.setText("");			
		}
		
		if (event.getActionCommand().equalsIgnoreCase("send-bulk")) {			
			String value = txtAmount.getText();
			Integer amount = Integer.parseInt(value);			
			gc.sendChatMessage(txtpnMessage.getText(), amount);
		}
	}
	
	public void onIncomingChatMessage(ChatMessage chatMessage) {	
		txtpnChat.setText(chatMessage.getChatMessage() + "\n" + txtpnChat.getText());				
	}

	public void onIncomingJoinMessage(JoinMessage joinMessage) {
		txtpnChat.setText(joinMessage.getMessage() + "\n" + txtpnChat.getText());
		gc.sendClientUpdateMessage();
	}

	public void onIncomingLeaveMessage(LeaveMessage leaveMessage) {
		txtpnChat.setText(leaveMessage.getMessage() + "\n" + txtpnChat.getText());	
		gc.sendClientUpdateMessage();
	}
	
	private String getUserName() {
		Faker faker = new Faker();
		return faker.name().fullName();		
	}
	
	private Long getUserId() {	
		
		return System.currentTimeMillis();		
	}

	public void onIncomingUpdateMessage(ClientUpdateMessage updateMessage) {
		txtpnChat.setText(updateMessage.getMessage() + "\n" + txtpnChat.getText());			
	}

	public void displayMessage(ClientUpdateMessage updateMessage) {
		txtpnChat.setText( "[" + updateMessage.getUserId() + "]" + updateMessage.getVectorClock().toString() + "\n" + txtpnChat.getText());	
		
	}
}
