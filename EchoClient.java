package myClient;

import java.net.Socket;             // Used to connect to the server
import java.io.ObjectInputStream;   // Used to read objects sent from the server
import java.io.ObjectOutputStream;  // Used to write objects to the server
import java.io.BufferedReader;      // Needed to read from the console
import java.io.InputStreamReader;   // Needed to read from the console

import java.io.IOException;
import java.util.Scanner;
/**
 * Simple client class.  This class connects to an EchoServer to send
 * text back and forth.  Java message serialization is used to pass
 * Message objects around.
 *
 *
 */
public class EchoClient
{

    /**
     * Main method.
     *
     * @param args  First argument specifies the server to connect to
     *
     */
	
	public static final String host = "localhost";
	
	private String userName;
	private String serverHost;
	private int serverPort;
	
	
    public static void main(String[] args)
    {
String readName = null;
	 
	 Scanner userInputScanner = new Scanner(System.in);
	 System.out.println("What's your username?");
	 readSomeText(readName);
	 
	 EchoClient client = new EchoClient(readName, EchoServer.SERVER_PORT);
			 
			 
 if(connect())
 {
	 
	 
	 
	try{
	    // Connect to the specified server
		
	    final Socket sock = new Socket("localhost", EchoServer.SERVER_PORT);
	    Thread.sleep(1000);
	    System.out.println("Connected to " + host + " on port " + EchoServer.SERVER_PORT);
	    
	    ServerThread serverThread = new ServerThread(sock, userName);
	    Thread serverAccessThread = new Thread(serverThread);
	    serverAccessThread.start();
	    
	    while (serverAccessThread.isAlive())
	    {
	    	if (userInputScanner.hasNextLine() ) {
	    		serverThread.addNextMessage(userInputScanner.nextLine());
	    	}
	    }
	    // Set up I/O streams with the server
	    final ObjectOutputStream output = new ObjectOutputStream(sock.getOutputStream());
	    final ObjectInputStream input = new ObjectInputStream(sock.getInputStream());

	    // loop to send messages
	    Integer k=0;
	    Message msg = null, resp = null;
	    Message password= null,username = null;
	    do{
		// Read and send message.  Since the Message class
		// implements the Serializable interface, the
		// ObjectOutputStream "output" object automatically
		// encodes the Message object into a format that can
		// be transmitted over the socket to the server.
	

		
		// Get ACK and print.  Since Message implements
		// Serializable, the ObjectInputStream can
		// automatically read this object off of the wire and
		// encode it as a Message.  Note that we need to
		// explicitly cast the return from readObject() to the
		// type Message.
		resp = (Message)input.readObject();
		System.out.println("\nServer says: " + resp.theMessage + "\n");
		if(resp!=null)
		{
		 
			if(resp.theMessage.equals("Enter UserName"))
			{
				username=new Message(Username());
				output.writeObject(username);
			}
			if(resp.theMessage.equals("Enter Password"))
			{
				password=new Message(Password());
				output.writeObject(password);
			}
			if(resp.theMessage.equals("Select 4 to Exit"))
			{
				msg=new Message(Username());
				output.writeObject(msg);
			}
			if(resp.theMessage.equals("Input secret key"))
			{
				msg=new Message(Username());
				output.writeObject(msg);
			}
			if(resp.theMessage.equals("4"))
			{
				k=5;
			}
			else
				continue;
			}
		
		    }while(k<4);
		    if(disconnect())
		    {
		    System.out.println("Exiting");
		    // shut things down
		    sock.close();
		    }
	
		} catch(IOException io) {
			System.out.println("Error: " + io.getMessage());
			io.printStackTrace();
		} catch (InterruptedException intE) {
			System.err.println("Error: " + intE.getMessage());
			intE.printStackTrace();
		} catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		    e.printStackTrace(System.err);
		}   
		
	
	
	    }//end if(connect())
    }//-- end main(String[])


    private EchoClient(String userName, int portNumber)
    {
    this.userName = userName;
    serverPort = portNumber;
    }
    
    /**
     * Simple method to print a prompt and read a line of text.
     *
     * @return A line of text read from the console
     */
    public static boolean connect()
    {
    	String choice = null;
    	System.out.println("Would you like to connect to the server? Type Y/N");
    	readSomeText(choice);
    	if(choice.toUpperCase().equals("Y") )
    	return true;
    	else
		return false;
    }
   public static boolean disconnect()
    {
    	String choice = null;
    	System.out.println("Would you like to disconnect to the server? Type Y/N");
    	readSomeText(choice);
    	if(choice.toUpperCase().equals("Y") )
    	return true;
    	else
		return false; 
    }
   
    private static void readSomeText(String outVar)
    {
    	while (outVar == null || outVar.trim().equals("")) {
			try{
			    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			    outVar = in.readLine();
			    if (outVar.trim().equals(""))
			    	System.out.println("Usernames must not contain whitespace. Please try again");
				}
			catch(Exception e){
			    System.out.println( "error reading from keyboard");
			    e.printStackTrace(System.err);
				}
    	}
    } //-- end readSomeText()
    
} //-- end class EchoClient