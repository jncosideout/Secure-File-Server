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
	 while (readName == null || readName.trim().equals("")) {
			try{
			    
			    readName = userInputScanner.nextLine();
			    if (readName.trim().equals(""))
			    	System.out.println("Usernames must not contain whitespace. Please try again");
				}
			catch(Exception e){
			    System.out.println( "error reading from keyboard");
			    e.printStackTrace(System.err);
				}
 	}
	 
	 EchoClient client = new EchoClient(readName, host, EchoServer.SERVER_PORT);
	 client.startClient(userInputScanner);


    }//-- end main(String[])


    private EchoClient(String userName, String host, int portNumber)
    {
    this.userName = userName;
    serverPort = portNumber;
    serverHost = host;
    }
    
    private void startClient(Scanner scan) {
    	
    	 
   	 
    	try{
    	    // Connect to the specified server
    		
    	    final Socket sock = new Socket(serverHost, serverPort);
    	    Thread.sleep(1000);
    	    System.out.println("Connected to " + host + " on port " + EchoServer.SERVER_PORT);
    	    
    	    ServerThread serverThread = new ServerThread(sock, userName);
    	    Thread serverAccessThread = new Thread(serverThread);
    	    serverAccessThread.start();
    	    
    	    while (serverAccessThread.isAlive())
    	    {
    	    	if (scan.hasNextLine() ) {
    	    		serverThread.addNextMessage(scan.nextLine());
    	    	}
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
    }
    
    /**
     * Simple method to print a prompt and read a line of text.
     *
     * @return A line of text read from the console
     */
    /*public static boolean connect()
    {
    	String choice = null;
    	System.out.println("Would you like to connect to the server? Type Y/N");
    	readSomeText(choice);
    	if(choice.toUpperCase().equals("Y") )
    	return true;
    	else
		return false;
    }*/
   
   
 
    
} //-- end class EchoClient