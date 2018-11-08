package myClient;

import java.awt.Color;
import java.io.*;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
/*Used to receive chat messages from another client. 
 * Extend SwingWorker so we can update the GUI and print
 * new messages to it automatically
 */
public class ServerThread extends SwingWorker<Void, String> {

		private SSLSocket socket;
		private String userName;
		private AES userAes;
	    protected JTextPane tp; 
		
		public ServerThread(SSLSocket socket, String userName, AES userAes, JTextPane tp) {
			super();
			this.socket = socket;
			this.userName = userName;
			this.userAes = userAes;
	        this.tp = tp;
		}
		
		/*Every Message object received contains an encrypted string
		 * and an HMAC of that string. When an object is read, first
		 * generate a new HMAC of the received message and compare it
		 * to the original HMAC we received. HMACs are calculated using
		 * a session key shared with our correspondent. Only these two 
		 * clients can create these HMAC hashes.
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			try {

				//this thread is now INPUT only
				InputStream serverInStream = socket.getInputStream();
	        	final ObjectInputStream objInput = new ObjectInputStream(serverInStream);
	        	Message response = null;
	        	
				while(!socket.isClosed()) {
                	response = (Message)objInput.readObject();
                	
                	if (!response.compareHash(response.theMessage, userAes)){
                		publish("message was tampered with in transit!");
                	}
                	publish(userAes.decrypt(response.theMessage));
						
				}//end while
				
				objInput.close();
				System.err.println("after ServerThread serverIn.close");
				
			} catch (IOException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException |
					NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | ClassNotFoundException io) {
				System.err.println(io.getMessage());
			}
			
			return null;
		}//end doInBackground
		/*Post published messages to the GUI. If message was sent by this
		 * client, post it in red. If it came from someone else, post it in blue.
		 * (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<String> chunks) {
	        for (String response : chunks) {
	          	StyledDocument doc = tp.getStyledDocument();
	          	
	            //  Define a keyword attribute
	            SimpleAttributeSet keyWord = new SimpleAttributeSet();
	          	if (response.contains(userName)){
	          		StyleConstants.setForeground(keyWord, Color.RED);
	          	} else {
	          		StyleConstants.setForeground(keyWord, Color.BLUE);
	          	}
	            StyleConstants.setBackground(keyWord, Color.WHITE);
	           // StyleConstants.setBold(keyWord, true);
	        
	         //  Add some text
	         try
	            { 
	        	 doc.insertString(doc.getLength(), response + "\n", keyWord );
	            } catch(Exception e) { 
	            	System.out.println(e); }
	            }
	        }
		
		
}//end class
