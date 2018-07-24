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
    


    private PrintWriter getClientOut() {
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
		
		//get session after connection is established
		SSLSession session = socket.getSession();
		
		System.out.println("Session details: ");
		System.out.println("\tProtocol: " + session.getProtocol());
		System.out.println("\tCipher suite: " + session.getCipherSuite());
		
	  //setup i/o
		this.clientOut = new PrintWriter(socket.getOutputStream(), false);
		Scanner in = new Scanner(socket.getInputStream());
		
		while(!socket.isClosed()) {
			 
			if (in.hasNextLine()) {
				String input = in.nextLine();
                // NOTE: if you want to check server can read input, 
				//uncomment next line and check server file console.
				System.out.println(input);  
				for (EchoThread thatClient : server.getClients()) {
					PrintWriter thatClientOut = thatClient.getClientOut();
					if (thatClientOut != null) {
						thatClientOut.write(input + "\r\n");
						thatClientOut.flush();
						/*make sure  there were 
						 * no surprises */
						if  (thatClientOut.checkError()) {
							System.err.println("EchoThread: java.io.PrintWriter error");
						}
					}
				}
			}
			
		}
		socket.close();
		in.close();
		join();
		/*
		// Print incoming message
	    System.out.println("** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + " **");

	    
	    
	    // set up I/O streams with the client
	    final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
	    final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
	   
	  
	    // Loop to read messages
	    Message msg = null, password=null, username=null; 
	    Message	keyMsg = null;
	   // String mSelect=null;
	    int count = 0;
	    do{
	    	
		// reads and print message
		
		
	    	int attempt = 3;
	        do{

	        	output.writeObject(new Message("Enter UserName"));
	        	username=(Message)input.readObject();
	        	output.writeObject(new Message("Enter Password"));
	        	password=(Message)input.readObject();
	        	mPassword=password.theMessage;
	        	mUsername=username.theMessage;
			
			if(validate()) // Authenticate users
			{
					output.writeObject(new Message("Permission Granted"));
						break;
			}
							attempt--;
							output.writeObject(new Message("** Invalid Username/Password combination."
						     		+ "Remaining attempts: " + attempt + " **"));
						    		   
		    }while (attempt > 0);
	        
	        if  (attempt < 1) // three strike rule
	        {	output.writeObject(new Message("Access Denied"));
	        // Close and cleanup
	        output.writeObject(new Message("** Closing connection with " + socket.getInetAddress() 
	        + ":" + socket.getPort() + " **"));
		    socket.close();
	        }
	        
	        mfiles();
			if(mUsername.equals("aj"))
			{
				scanFile(path1,mList);// writes to file
				scanFile(path2,mList);//
			}
			else
			{
				scanFile(path3,mList2);// 
				scanFile(path4,mList2);//
			}
	do
	{
			//user menu
			output.writeObject(new Message("Select 1 for Files"));
			output.writeObject(new Message("Select 2 to Encrypt"));
			output.writeObject(new Message("Select 3 to Decrypt"));
			output.writeObject(new Message("Select 4 to Exit"));

			msg = (Message)input.readObject();//user response
			
			try
			{
				if(msg.theMessage.equals("1"))
				{
					Selection=1;
				}
				if(msg.theMessage.equals("2"))
				{
					Selection=2;
				}
				if(msg.theMessage.equals("3"))
					
				{
					Selection=3;
				}
				if(msg.theMessage.equals("4"))
				{
					Selection=4;
				}
			}
			catch (NumberFormatException e)
			{
				Selection=5;
			}
			
			switch(Selection)
			{
			//view files
			case 1:
				if(mUsername.equals("aj"))
				{
			for(String i:mList)
				{
				output.writeObject(new Message(i.toString()));
				}
			}
				else //jackie
				{
				for(String i:mList2)
					{
						output.writeObject(new Message(i.toString()));
					}
				}
					
						break;
						
						//Encrypt files
			case 2:
					output.writeObject(new Message("Input secret key"));
					keyMsg = (Message)input.readObject();
					UserENCRYPTkey = keyMsg.theMessage; 
					if(mUsername.equals("aj"))
					{ 
						if(UserENCRYPTkey.equals(AES.keyAJ))
						{	for(String i:mList)
							{
							String line=AES.encrypt(i.toString(),AES.keyAJ);
							encrypt.add(line);
							}
						}
						else
						{	output.writeObject(new Message("Invalid secret key, bye bye"));
						output.writeObject(new Message("4"));
						break;
						}
					}
						
						
					 if(mUsername.equals("jackie"))
					{ 
						if(UserENCRYPTkey.equals(AES.keyJK))
						{	for(String i:mList2)
							{
							String line=AES.encrypt(i.toString(),AES.keyJK);
							encrypt.add(line);
							}
						}
						else
						{	output.writeObject(new Message("Invalid secret key, so long"));
						output.writeObject(new Message("4"));
						break;
						}
					}	
					
									

						
				int firsttime=0; // used to clear users list
				
				if(mUsername.equals("aj"))
				{
						for(String i:encrypt)
							{
						
								if(firsttime==0)
								{
									mList.clear();
								}
								mList.add(i);
								firsttime++;
							}
			}
				else //jackie
				{
					for(String i:encrypt)
					{
			
						if(firsttime==0)
						{
							mList2.clear();
						}
						mList2.add(i);
						firsttime++;
					}
					
					
				}
			
				output.writeObject(new Message("Files Encrypted"));
				encrypt.clear();
				break;
				//decrypt files
			case 3:
					output.writeObject(new Message("Input secret key"));
					keyMsg = (Message)input.readObject();
					UserENCRYPTkey = keyMsg.theMessage; 
					if(mUsername.equals("aj"))
					{ 
						if(UserENCRYPTkey.equals(AES.keyAJ))
						{	for(String i:mList)
							{
							String line=AES.decrypt(i.toString(),AES.keyAJ);
							encrypt.add(line);
							}
						}
						else
						{	output.writeObject(new Message("Invalid secret key, get out of here"));
						output.writeObject(new Message("4"));

						break;}
						}
						
						
					if(mUsername.equals("jackie"))
					{ 
						if(UserENCRYPTkey.equals(AES.keyJK))
						{	for(String i:mList2)
							{
							String line=AES.decrypt(i.toString(),AES.keyJK);
							encrypt.add(line);
							}
						}
						else
						{	output.writeObject(new Message("Invalid secret key, scram"));
						output.writeObject(new Message("4"));
						break;
						 }
					}	
						
					
				int secondtime=0;// repeat of first time
				
				if(mUsername.equals("aj"))
				{
					for(String i:encrypt)
					{
						if(secondtime==0)
						{
							mList.clear();
						}
						mList.add(i);
						secondtime++;
					}
				}
				else
				{
					for(String i:encrypt)
					{
						if(secondtime==0)
						{
							mList2.clear();
						}
						mList2.add(i);
						secondtime++;
					}
				}
				
				output.writeObject(new Message("Files Decrypted"));
				encrypt.clear();
				break;
				// exit function for user
			case 4: output.writeObject(new Message("4"));
			String userfile="";
			String userfile1="";
			String userfile2="";
			String userfile3="";
			boolean check=true;
			if(mUsername.equals("aj"))
			{
				for(String i: mList)
				{
					if(check)
					{
						userfile=i;
						check=false;
					}
					else
						{userfile1=i;
						check=true;	}	
				}
			}
				else
				{
					for(String i: mList2)
					{
						if(check)
						{
							userfile2=i;
							check=false;
						}
						else
						{	userfile3=i;
							check=true;		
						}
				
				}
				}
			if(mUsername.equals("aj"))
				{
					overwrite(path1,userfile );
					overwrite(path2,userfile1);
				}
				else
				{
					overwrite(path3,userfile2 );
					overwrite(path4,userfile3);
					
				}
				Clear();
			
			
			
		}	
	
	}while(Selection>0 && Selection<=4);
			
			

	output.writeObject(new Message("Access Denied"));
		// Write an ACK back to the sender
		count++;
		output.writeObject(new Message("Recieved message #" + count));

	    }while(Selection==4);
		System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] " + msg.theMessage);

	    // Close and cleanup
	    System.out.println("** Closing connection with " + socket.getInetAddress() + ":" + socket.getPort() + " **");
	    socket.close();

	*/
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

