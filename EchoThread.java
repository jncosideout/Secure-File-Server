package myClient;




import java.lang.Thread;            // We will extend Java's base Thread class

import java.io.ObjectInputStream;   // For reading Java objects off of the wire
import java.io.ObjectOutputStream;  // For writing Java objects to the wire
import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;



import javax.net.ssl.SSLSocket;

/**
 * A simple server thread.  This class just echoes the messages sent
 * over the socket until the socket is closed.
 *
 */
public class EchoThread extends Thread
{

	private ObjectOutputStream clientOut;
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
    


    private ObjectOutputStream getClientOut() {return clientOut;}
    
   
    public void run()
    {
		//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

	try{
	  //setup i/o
		this.clientOut = new ObjectOutputStream(socket.getOutputStream());
    	final ObjectInputStream objInput = new ObjectInputStream(socket.getInputStream());
    	Message input = null;
    	
		while(!socket.isClosed()) {

			try {
			
				input = (Message) objInput.readObject();
                // NOTE: if you want to check server can read input, 
				//uncomment next line and check server file console.
				//System.out.println(input); 
				for (EchoThread thatClient : server.getClients()) {
					ObjectOutputStream thatClientOut = thatClient.getClientOut();
					
					if (thatClientOut != null) {
                        thatClientOut.writeObject(input);
						thatClientOut.flush();
					}
				}
			
				} catch (IOException eof){
					System.err.println(eof.getMessage());
					System.out.println("after ping");
					socket.close();
					System.out.println("after socket.close");
					objInput.close();			
					System.out.println("after objInput.close");
					clientOut.close();
					System.out.println("after clientOut.close");
					
					break;
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			} //end if/else
		
		
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

