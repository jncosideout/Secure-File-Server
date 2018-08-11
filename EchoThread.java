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

	private BufferedWriter clientOut;
	private EchoServer server;
	private SSLSocket socket; // The socket that we'll be talking over
	
	public String mPassword; // to store the user's input for password verification
	public String mUsername;// to store the user's input for password verification
	public String UserENCRYPTkey; // to compare the user's input for encryption key verification
	public Integer Selection=0; //user input for menu selection
	public String path1, path2, path3,path4; //to store the file pathnames for each file belonging to a user
	final static Charset ENCODING = StandardCharsets.UTF_8;
	public ArrayList<String> encrypt=new ArrayList<String>(); //array to temporarily store encrypted or decrypted output

	public ArrayList<String> mList=new ArrayList<String>(); // array to  store user's files while application is running only.
	public ArrayList<String> mList2=new ArrayList<String>(); // array to  store user's files while application is running only.

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
    


    private BufferedWriter getClientOut() {
		return clientOut;
	}



	public void mfiles()
    {
    	if(mUsername.equals("aj"))
		{
			path1 = "C:\\Users\\Alex\\Documents\\java projects\\client file server\\ajlist1.txt";
			path2 = "C:\\Users\\Alex\\Documents\\java projects\\client file server\\ajlist2.txt";
			
		}
		if(mUsername.equals("jackie"))
		{
			path3 = "C:\\Users\\Alex\\Documents\\java projects\\client file server\\jackielist1.txt";
	    	path4 = "C:\\Users\\Alex\\Documents\\java projects\\client file server\\jackielist2.txt";
		}
    	
    }
    
    /**
     * run() is basically the main method of a thread.  This thread
     * simply reads Message objects off of the socket.
     *
     */
    public void run()
    {
		socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

	try{
		//start handshake
		socket.startHandshake();
		/*
		//get session after connection is established
		SSLSession session = socket.getSession();
		
		System.out.println("Session details: ");
		System.out.println("\tProtocol: " + session.getProtocol());
		System.out.println("\tCipher suite: " + session.getCipherSuite());
		*/
	  //setup i/o
		this.clientOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		Scanner in = new Scanner(socket.getInputStream());
		
		while(!socket.isClosed()) {
			 
			//if (socket.getInputStream().available() <= 0) {

			if (in.hasNextLine()) {
				String input = in.nextLine();
                // NOTE: if you want to check server can read input, 
				//uncomment next line and check server file console.
				System.out.println(input); 
				for (EchoThread thatClient : server.getClients()) {
					BufferedWriter thatClientOut = thatClient.getClientOut();
					
					if (thatClientOut != null) {
						
						try {
							thatClientOut.write(input + "\r\n");
							thatClientOut.flush();
							/*make sure  there were 
							 * no surprises */
							//						if  (thatClientOut()) {
							//							System.err.println("EchoThread: java.io.BufferedWriter error");
							//						}
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
   
    
  
    public boolean validate()
    {
    	
    		for(int i=0;i<EchoServer.usernames.length;i++)
    		{
    			if(EchoServer.usernames[0][i].equals(mUsername)&& EchoServer.usernames[1][i].equals(mPassword))
    			{
    				return true;
    			
    			}
    		}
    	
    	
    	return false;
    }
    public void Clear()
    {
    	if(mUsername.equals("aj"))
		{
			mList.clear();
		}
		else
		{
			mList2.clear();
		}
    	
    }
      
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

