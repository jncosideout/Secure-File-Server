package myClient;




import java.lang.Thread;            // We will extend Java's base Thread class
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;   // For reading Java objects off of the wire
import java.io.ObjectOutputStream;  // For writing Java objects to the wire
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

/**
 * A simple server thread.  This class just echoes the messages sent
 * over the socket until the socket is closed.
 *
 */
public class EchoThread extends Thread
{

	private PrintWriter clientOut;
	private EchoServer server;
	private SSLSocket socket; // The socket that we'll be talking over
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
    /**
     * Constructor that sets up the socket we'll chat over
     *
     * @param _socket The socket passed in from the server
     *
     */
    public EchoThread(EchoServer server, SSLSocket _socket)
    {
    	this.server = server;
    	socket = _socket;
    }
    


    private PrintWriter getClientOut() {return clientOut;}
    
   
    public void run()
    {
		//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

	try{
	  //setup i/o
		this.clientOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		Scanner in = new Scanner(socket.getInputStream());
		
		while(!socket.isClosed()) {
			 
			if (in.hasNextLine()) {
				String input = in.nextLine();
                // NOTE: if you want to check server can read input, 
				//uncomment next line and check server file console.
				//System.out.println(input); 
				for (EchoThread thatClient : server.getClients()) {
					PrintWriter thatClientOut = thatClient.getClientOut();
					
					if (thatClientOut != null) {
						
						try {
							thatClientOut.write(input + "\r\n");
							thatClientOut.flush();
							/*make sure  there were 
							 * no surprises */
							if  (thatClientOut.checkError()) {
								System.err.println("EchoThread: PrintWriter error");
							}
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			} else {
				try {
				clientOut.write("ping");
				} catch (Exception eof){
					System.out.println("after ping");
					socket.close();
					System.out.println("after socket.close");
					in.close();			
					System.out.println("after in.close");
//					clientOut.close();
//					System.out.println("after clientOut.close");
					
					break;
				}
			} //end if/else
		}//end while
		
			try {
				server.removeClient(this);
				join();
				System.out.println("after join");
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		
		} catch (IOException io) {
		    System.err.println("Error: " + io.getMessage());
				io.printStackTrace();
		} catch(Exception e) {
		    System.err.println("Error: " + e.getMessage());
		    e.printStackTrace(System.err);
		} 

    }  //-- end run()
    
} //-- end class EchoThread

