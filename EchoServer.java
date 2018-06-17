package myClient;


import java.net.ServerSocket;  // The server uses this to bind to a port
import java.net.Socket;        // Incoming connections are represented as sockets

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * A simple server class.  Accepts client connections and forks
 * EchoThreads to handle the bulk of the work.
 *
 * 
 */
public class EchoServer
{
    /** The server will listen on this port for client connections */
    public static final int SERVER_PORT = 7756;
    private int serverPort;
    
    private List<EchoThread> clients;
    
public static String[][] usernames;
public static boolean exiting = false;
   
    /**
     * Main routine.  Just a dumb loop that keeps accepting new
     * client connections.
     *
     */

	public EchoServer(int portNumber ) {
		serverPort = portNumber;
	}
	
    public static void main(String[] args)
    {
	    EchoServer server = new EchoServer(SERVER_PORT);
    	server.startServer();
    	/*
    	usernames=new String[2][2];
    	usernames[0][0]="aj";
    	usernames[0][1]="jackie";
    	usernames[1][0]="sand";
    	usernames[1][1]="love";*/
    
	

    }

    private void startServer(){
    	clients = new ArrayList<EchoThread>();
    	
    	try{
    	    // This basically just listens for new client connections
    	    final ServerSocket serverSock = new ServerSocket(serverPort);
    	    acceptClients(serverSock);
    	    
    	} catch (IOException io) {
    		System.err.println("Could not listen on port " + serverPort);
    		System.exit(1);
    	} catch(Exception e){
    	    System.err.println("Error: " + e.getMessage());
    	    e.printStackTrace(System.err);
    	}
    }
    
	public List<EchoThread> getClients() {
		return clients;
	}
    
    
    private void acceptClients(ServerSocket serverSocket) {
    	// A simple infinite loop to accept connections
	    Socket sock = null;
	    Thread thread = null;
	    System.out.println("Server starts port  " + serverSocket.getLocalSocketAddress());
	    
	    while(true){
	    	try {
	    		sock = serverSocket.accept();     // Accept an incoming connection from CLIENT
	    		System.out.println("Accepts: " + sock.getRemoteSocketAddress());
	    		EchoThread client = new EchoThread(this, sock);
	    		thread = new Thread(client);  // Create a thread to handle this connection
	    		thread.start();                 // Fork the thread
	    		clients.add(client);
	    	} catch (IOException io) {
	    		System.out.println("Accept failed on " + SERVER_PORT);
	    		io.printStackTrace();
	    	}
	    }// Loop to work on new connections while this
        // the accept()ed connection is handled

    }
    
//-- end main(String[])

} //-- End class EchoServer	
