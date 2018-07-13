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

import java.io.Console;				//used for system.console.readPassword() which isn't working
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
		//System.out.println("What is the keystore file path?");
		ksName = ""; 			
				//scan.nextLine();

		//System.out.println("Input keystore password");
		String	storePass = "keYs4clianTs";
    	char[] spass = storePass.toCharArray();  				// password for keystore
    	
    	String tsName; //file path of trust store
		//System.out.println("What is the trust store file path?");
		tsName = ""; 			
				//scan.nextLine();

		//System.out.println("Input keystore password");
		String	trustStorePass = "clianTtrUst";
    	char[] tspass = trustStorePass.toCharArray();  				// password for TrustStore
     				
    	
		System.out.println("What is the alias?");
		String alias = scan.nextLine();
		
		//System.out.println("Input key password for %s", alias);
		String keypass = "client1";
    	char[] kpass = keypass.toCharArray();  // password for private key
    	
    	
    	 try {
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			
			//initialize KeyStore
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream ksfis = new FileInputStream(ksName);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			
			ks.load(ksbufin, spass);
			
			//initialize TrustStore
			KeyStore ts = KeyStore.getInstance("JKS");
			FileInputStream tsfis = new FileInputStream(tsName);
			BufferedInputStream tsbufin = new BufferedInputStream(tsfis);
			
			ts.load(tsbufin, tspass);

			//init factories
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
			kmf.init(ks, kpass);			//uses KEY pass not STORE pass
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(ts);
			
			SecureRandom random = SecureRandom.getInstanceStrong();
			sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
			
//			SSLParameters params = sc.getSupportedSSLParameters();
//			String[] ciphers = params.getCipherSuites();
//			String[] protocols = params.getProtocols();
//			System.out.println("supported cipher suites are: \n");
//			for (String c : ciphers) {
//				System.out.println(c + "\n");
//			}
//			System.out.println("Supported protocols are: \n");
//			for (String p : protocols) {
//				System.out.println(p +"\n");
//			}
			
//			System.out.println(x);
//			System.out.println("client session : " + sc.getClientSessionContext());
			
			
			SSLSocketFactory factory = sc.getSocketFactory();
			
			try{
	    	    // Connect to the specified server
	    		
	    	    SSLSocket sock = (SSLSocket) factory.createSocket(serverHost, serverPort);
    			String[] suites = {"TLS_RSA_WITH_AES_128_CBC_SHA256"};
    			sock.setEnabledCipherSuites(suites);
    			
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
