package myClient;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class ServerThread implements Runnable {

		private SSLSocket socket;
		private String userName;
		private final LinkedList<String> messagesToSend;
		private boolean hasMessages = false;
		
		public ServerThread(SSLSocket socket, String userName) {
			super();
			this.socket = socket;
			this.userName = userName;
			messagesToSend = new LinkedList<String>();
		}
		
		public void addNextMessage(String message) {
			synchronized (messagesToSend) {
				hasMessages = true;
				messagesToSend.push(message);
			}
		}
		
		public void run(){
			
			//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
			System.out.println("Welcome " + userName);
			System.out.println("Local port: " + socket.getLocalPort());
			System.out.println("Server = " + socket.getRemoteSocketAddress() + ":" + socket.getPort());
			
			try {
				// handshake
				socket.startHandshake();
				
				//get session after connection is established
				SSLSession session = socket.getSession();
				
				System.out.println("Session details: ");
				System.out.println("\tProtocol: " + session.getProtocol());
				System.out.println("\tCipher suite: " + session.getCipherSuite());
				
				//this thread is now INPUT only
				InputStream serverInStream = socket.getInputStream();
				Scanner serverIn = new Scanner(serverInStream);
				
				while(!socket.isClosed()) {
						if (serverIn.hasNextLine()) {
							System.out.println(serverIn.nextLine());
						}
				}//end while
				
				serverIn.close();
				System.out.println("after ServerThread serverIn.close");
				
			} catch (IOException io) {
				System.err.println(io.getMessage());
			}
		}//end run
		
}//end class
