package se.miun.distsys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SequenceServer {

	private ServerSocket serverSocket;
	 	
	private Long sequenceNumber;
	
    public void start(int port, Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    	try {
			serverSocket = new ServerSocket(port);
			System.out.println("Sequence server started");
			while(true)
		            new SequenceClientHandler(serverSocket.accept()).start();
		} catch (IOException e) {			
			e.printStackTrace();
		}       
    }
 
    public void stop() {
        try {
			serverSocket.close();
			System.out.println("Sequence server started");
		} catch (IOException e) {			
			e.printStackTrace();
		}
    }
    
    class SequenceClientHandler extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;        
 
        public SequenceClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
 
        public void run() {
            try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(
			              new InputStreamReader(clientSocket.getInputStream()));
			             
			            String inputLine;
			            while ((inputLine = in.readLine()) != null) {
			                if ("next-sequence".equals(inputLine)) {
			                	sequenceNumber ++;
			                	out.println("b");
			                    break;
			                }
			                out.println(inputLine);
			            }
			} catch (IOException e) {				
				e.printStackTrace();
			}finally{
				try {
					in.close();
					out.close();
		            clientSocket.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}	            
			}            
        }
    }

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}    
}
