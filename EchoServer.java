package myClient;


import java.net.ServerSocket;  // The server uses this to bind to a port
import java.net.Socket;        // Incoming connections are represented as sockets
import java.security.AlgorithmConstraints;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

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
    	
    	
    	    // This basically just listens for new client connections
    		String ksName; //file path of keystore
    		//System.out.println("What is the keystore file path?");
    		ksName = "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore"; 			
    				//scan.nextLine();

    		//System.out.println("Input keystore password");
    		String	storePass = "serVerstoRepasS";
        	char[] spass = storePass.toCharArray();  				// password for keystore
        	
        	String tsName; //file path of trust store
    		//System.out.println("What is the trust store file path?");
    		tsName = "C:\\temp-openssl-32build\\serverkeystore\\serverTrustStore"; 			
    				//scan.nextLine();

    		//System.out.println("Input keystore password");
    		String	trustStorePass = "serVertrUst";
        	char[] tspass = trustStorePass.toCharArray();  				// password for TrustStore
         				
        	
//    		System.out.println("What is the alias?");
//    		String alias = scan.nextLine();
    		
    		//System.out.println("Input key password for %s", alias);
    		String keypass = "serVerkeYpasS";
        	char[] kpass = keypass.toCharArray();  // password for private key
        	
        	try{
    			SSLContext sc = SSLContext.getInstance("TLSv1.2");
    			
    			//initialize KeyStore
    			KeyStore ks = KeyStore.getInstance("JKS");
    			FileInputStream ksfis = new FileInputStream(ksName);
    			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
    			
    			ks.load(ksbufin, spass);
    			ksfis.close();
    			
    			//initialize TrustStore
    			KeyStore ts = KeyStore.getInstance("JKS");
    			FileInputStream tsfis = new FileInputStream(tsName);
    			BufferedInputStream tsbufin = new BufferedInputStream(tsfis);
    			
    			ts.load(tsbufin, tspass);
    			tsfis.close();

    			//init factories
    			KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
    			kmf.init(ks, kpass);
    			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
    			tmf.init(ts);
    			
    			SecureRandom random = SecureRandom.getInstanceStrong();
    			sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
    			
//    			SSLParameters params = sc.getSupportedSSLParameters();
//    			
//    			AlgorithmConstraints algoConsts = params.getAlgorithmConstraints();
//    			List<SNIServerName> SNIs = params.getServerNames();
//    			String[] ciphers = params.getCipherSuites();
//    			String[] protocols = params.getProtocols();
//    			
//    			System.out.println("supported cipher suites are: \n");
//    			for (String c : ciphers) {
//    				System.out.println(c + "\n");
//    			}
//    			System.out.println("Supported protocols are: \n");
//    			for (String p : protocols) {
//    				System.out.println(p +"\n");
//    			}
//    			System.out.println("SNIs are: \n");				//all null now
//    			for (SNIServerName s : SNIs) {
//    				System.out.println(s.toString() +"\n");
//    			}
    			
    			SSLServerSocketFactory sslServSockFact = sc.getServerSocketFactory();
    			SSLServerSocket serverSock = (SSLServerSocket) sslServSockFact.createServerSocket(serverPort);
    			
//    			String[] suites = {"TLS_RSA_WITH_AES_128_CBC_SHA256"};
//    			serverSock.setEnabledCipherSuites(suites);
    			
    	    acceptClients(serverSock);
    	    
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
 		} catch (UnrecoverableKeyException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		} catch (KeyManagementException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
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
    
    
    private void acceptClients(SSLServerSocket serverSocket) {
    	// A simple infinite loop to accept connections
	    SSLSocket sock = null;
	    //Thread thread = null;
	    System.out.println("Server starts port  " + serverSocket.getLocalSocketAddress());

    	
			 while(true){// Loop to work on new connections while this
			        // the accept()ed connection is handled
				    	try {
				    		sock = (SSLSocket) serverSocket.accept();     // Accept an incoming connection from CLIENT
				    		System.out.println("Accepts: " + sock.getRemoteSocketAddress());
				    		EchoThread client = new EchoThread(this, sock);
				    		client.start();                 // Fork the thread
				    		clients.add(client);
				    		
				    	} catch (IOException io) {
				    		System.out.println("Accept failed on " + SERVER_PORT);
				    		io.printStackTrace();
				    	}
				    }
    	
	   

    }
    
//-- end main(String[])

} //-- End class EchoServer	
