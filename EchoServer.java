package myClient;


import java.net.ServerSocket;  // The server uses this to bind to a port
import java.net.Socket;        // Incoming connections are represented as sockets
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
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
    

public static String[][] usernames;
public static boolean exiting = false;
   
    /**
     * Main routine.  Just a dumb loop that keeps accepting new
     * client connections.
     *
     */
    public static void main(String[] args)
    {
    	usernames=new String[2][2];
    	usernames[0][0]="aj";
    	usernames[0][1]="jackie";
    	usernames[1][0]="sand";
    	usernames[1][1]="love";
    
	try{
	    // This is basically just listens for new client connections
	    final ServerSocket serverSock = new ServerSocket(SERVER_PORT);
	    
	    // A simple infinite loop to accept connections
	    Socket sock = null;
	    EchoThread thread = null;
	    while(true){
		sock = serverSock.accept();     // Accept an incoming connection
		thread = new EchoThread(sock);  // Create a thread to handle this connection
		thread.start();                 // Fork the thread
	    }                                   // Loop to work on new connections while this
                                                // the accept()ed connection is handled

	}
	catch(Exception e){
	    System.err.println("Error: " + e.getMessage());
	    e.printStackTrace(System.err);
	}

    }
    


    
    
//-- end main(String[])

} //-- End class EchoServer	
