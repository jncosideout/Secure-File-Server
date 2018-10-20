package myClient;

import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class ServerThread implements Runnable {

		private SSLSocket socket;
		private String userName;
		private AES userAes;
		
		public ServerThread(SSLSocket socket, String userName, AES userAes) {
			super();
			this.socket = socket;
			this.userName = userName;
			this.userAes = userAes;
		}
		
		
		public void run(){
			
			//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
			System.out.println("Welcome " + userName);
			System.out.println("Local port: " + socket.getLocalPort());
			System.out.println("Server = " + socket.getRemoteSocketAddress() + ":" + socket.getPort());
			
			try {
				
				//get session after connection is established
				SSLSession session = socket.getSession();
				
				System.out.println("Session details: ");
				System.out.println("\tProtocol: " + session.getProtocol());
				System.out.println("\tCipher suite: " + session.getCipherSuite());
				System.out.println("Begin chatting.");
				
				//this thread is now INPUT only
				InputStream serverInStream = socket.getInputStream();
	        	final ObjectInputStream objInput = new ObjectInputStream(serverInStream);
	        	Message response = null;
	        	
				while(!socket.isClosed()) {
                	response = (Message)objInput.readObject();
                	
                	if (!response.compareHash(response.theMessage, userAes)){
                		System.out.println("message was tampered with in transit!");
                	}
                	System.out.println(userAes.decrypt(response.theMessage));
						
				}//end while
				
				objInput.close();
				System.out.println("after ServerThread serverIn.close");
				
			} catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
					NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | ClassNotFoundException io) {
				System.err.println(io.getMessage());
			}
		}//end run
		
}//end class
