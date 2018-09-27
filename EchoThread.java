package myClient;




import java.lang.Thread;            // We will extend Java's base Thread class
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;   // For reading Java objects off of the wire
import java.io.ObjectOutputStream;  // For writing Java objects to the wire
import java.io.OutputStreamWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
	
	public String mPassword; // to store the user's input for password verification
	public String mUsername;// to store the user's input for password verification
	public String UserENCRYPTkey; // to compare the user's input for encryption key verification
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
    


    private PrintWriter getClientOut() {
		return clientOut;
	}
    
   
    public void run()
    {
		//socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

	try{
		//start handshake
		socket.startHandshake();
		
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
    
      
    void overwrite(String path, String userfile)// overwrite text files
    {
    File fold=new File(path);
    fold.delete();
    File fnew=new File(path);
    

    try {
        FileWriter f2 = new FileWriter(fnew, false);
        f2.write(userfile);
        f2.close();
    } catch (IOException e) {
        e.printStackTrace();
    }     
    }

	public void scanFile(String path,ArrayList<String> list)// reads text files into array
	{
	    try(BufferedReader br = new BufferedReader(new FileReader(path)))
	    {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        String everything = sb.toString();
	        list.add(everything) ;
	      //  System.out.print(everything);
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} //-- end class EchoThread

