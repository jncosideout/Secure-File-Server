package myClient;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class ServerThread implements Runnable {

		private Socket socket;
		private String userName;
		private final LinkedList<String> messagesToSend;
		private boolean hasMessages = false;
		
		public ServerThread(Socket socket, String userName) {
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
			System.out.println("Welcome " + userName);
			
			System.out.println("Local port: " + socket.getLocalPort());
			System.out.println("Server = " + socket.getRemoteSocketAddress() + ":" + socket.getPort());
			
			try {
				PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
				InputStream serverInStream = socket.getInputStream();
				Scanner serverIn = new Scanner(serverInStream);
				
				while(!socket.isClosed()) {
					if (serverInStream.available() > 0) {
						if (serverIn.hasNextLine()) {
							System.out.println(serverIn.nextLine());
						}
					}
					if (hasMessages) {
						String nextSend = "";
						synchronized (messagesToSend) {
							nextSend = messagesToSend.pop();
							hasMessages = !messagesToSend.isEmpty();
						}
					serverOut.println(userName + " > " + nextSend);
					serverOut.flush();
					}
				}//end while
				
				serverIn.close();
			} catch (IOException io) {
				System.err.println(io.getMessage());
			}
		}//end run
		
}//end class
