package myClient;

import java.net.Socket;             // Used to connect to the server
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.*;
import java.io.ObjectInputStream;   // Used to read objects sent from the server
import java.io.ObjectOutputStream;  // Used to write objects to the server
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
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
			    	System.out.println("Usernames must not be blank. Please try again");
				}
			catch(Exception e){
			    System.out.println( "error reading from keyboard");
			    e.printStackTrace(System.err);
				}
 	}
	 
	 EchoClient client = new EchoClient(readName, host, EchoServer.SERVER_PORT);
	 client.startClient(userInputScanner);

	 userInputScanner.close();
	 

    }//-- end main(String[])


    private EchoClient(String userName, String host, int portNumber)
    {
    this.userName = userName;
    serverPort = portNumber;
    serverHost = host;
    }
    
    private void startClient(Scanner scan) {
    	
    	String ksName; //file path of keystore
			System.out.println("What is the keystore file path?");
			ksName = scan.nextLine();
    	char[] spass;  // password for keystore
    		spass = System.console().readPassword("Input keystore password");
    	String alias;
    		System.out.println("What is the alias?");
    		alias = scan.nextLine();
    	char[] kpass;  // password for private key
    		kpass = System.console().readPassword("Input key password for %s", alias);
    	
    	 try {
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream ksfis = new FileInputStream(ksName);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			
			ks.load(ksbufin, spass);

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
			kmf.init(ks, spass);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(ks);
			
			SecureRandom random = SecureRandom.getInstanceStrong();
			sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
    	 } catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		} catch (KeyStoreException e2) {
			System.err.println(e2.getMessage());
			e2.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (CertificateException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 
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
    	    		if (scan.hasNext("goodbye")) {
    	    			break;
    	    		}
    	    		serverThread.addNextMessage(scan.nextLine());
    	    	}
    	    }
    	    System.out.println("Leaving");
    	    sock.close();
    	    
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