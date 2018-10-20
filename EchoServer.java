package myClient;


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

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;// The server uses this to bind to a port
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;// Incoming connections are represented as sockets
import javax.net.ssl.TrustManagerFactory;

import java.util.ArrayList;
import login.LoginHandler;
import rsaEncryptSign.DHCryptoReceiver;
import rsaEncryptSign.DHKeyAlice;
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
    
    List<EchoThread> clients;
    private List<DHKeyEchoThread2> DHClients;
    
	public static String[][] usernames;
	public static boolean exiting = false;
	
	private String ksName; //file path of keystore
	private String	storePass;// password for keystore
	private String tsName; //file path of trust store
	private String	trustStorePass;// password for TrustStore
	private String keypass;// password for private key
   
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
	    ArrayList<char[]> jksPassWs = server.assignKeystorePaths();
    	SSLContext sc = server.initSSLContext(jksPassWs);
	    SSLServerSocket serverSock = server.startServer(sc);
    	try {
			server.acceptClients(serverSock);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }

    private SSLServerSocket startServer(SSLContext sc){
    	clients = new ArrayList<EchoThread>();
    	DHClients = new ArrayList<DHKeyEchoThread2>();

    	//create a new ServerSocket using SSL/TLS SecureContext	
    	SSLServerSocketFactory sslServSockFact = sc.getServerSocketFactory();
		SSLServerSocket serverSock = null;
		try {
			serverSock = (SSLServerSocket) sslServSockFact.createServerSocket(serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverSock.setNeedClientAuth(true);
//			String[] suites = {"TLS_RSA_WITH_AES_128_CBC_SHA256"};
//			serverSock.setEnabledCipherSuites(suites);
		return serverSock;
	            	
    }
	
    //sets up the file paths and passwords for the keystore, trust store 
    //and alias associated with the certificate of this server
    private ArrayList<char[]> assignKeystorePaths() {
		//System.out.println("What is the keystore file path?");
		ksName = "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore"; 			
		//scan.nextLine();

		//System.out.println("Input keystore password");
		storePass = "serVerstoRepasS";
    	char[] spass = storePass.toCharArray();  				
    	
    	//System.out.println("What is the trust store file path?");
		tsName = "C:\\temp-openssl-32build\\serverkeystore\\serverTrustStore"; 			
		//scan.nextLine();

		//System.out.println("Input keystore password");
		trustStorePass = "serVertrUst";
    	char[] tspass = trustStorePass.toCharArray();  				
     		
//		System.out.println("What is the alias?");
//		String alias = scan.nextLine();
		
		//System.out.println("Input key password for %s", alias);
		keypass = "serVerkeYpasS";
    	char[] kpass = keypass.toCharArray();
    	ArrayList<char[]> passwords = new ArrayList<>();
    	passwords.add(spass);
    	passwords.add(tspass);
    	passwords.add(kpass);
    	
		return passwords;  
    }
    
    public List<DHKeyEchoThread2> getDHClients(){return DHClients;}
	public List<EchoThread> getClients() {return clients;}
	
	public void removeClient(EchoThread et) {
		clients.remove(et);
	}
    
	//Creates an SSLContext that holds the keystore and trust store for the server
	//and specifies the version of the SSL protocol to use
    public SSLContext initSSLContext(ArrayList<char[]> jksPassWs) {
    	SSLContext sc = null;
    	try{
			sc = SSLContext.getInstance("TLSv1.2");
			
			//initialize KeyStore
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream ksfis = new FileInputStream(ksName);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			
			try{
				ks.load(ksbufin, jksPassWs.get(0));
				ksfis.close();
				//initialize TrustStore
				KeyStore ts = KeyStore.getInstance("JKS");
				FileInputStream tsfis = new FileInputStream(tsName);
				BufferedInputStream tsbufin = new BufferedInputStream(tsfis);
				ts.load(tsbufin, jksPassWs.get(1));
				tsfis.close();
				//init factories
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
				kmf.init(ks, jksPassWs.get(2));
				TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
				tmf.init(ts);
				SecureRandom random = SecureRandom.getInstanceStrong();
				sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
			} catch (IOException io) {
				System.err.println("Could not listen on port " + serverPort);
				System.exit(1);
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
		} catch (UnrecoverableKeyException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
    	return sc;
    }
	
    //starts a loop to accept and handle clients
    private void acceptClients(SSLServerSocket serverSocket) throws InterruptedException {
    	// A simple infinite loop to accept connections
	    SSLSocket sock = null;
	    System.out.println("Server starts port  " + serverSocket.getLocalSocketAddress());
 	
			 while(true){// Loop to work on new connections while this
			        // the accept()ed connection is handled
				    	try {
				    		// Accept an incoming connection from CLIENT
				    		sock = (SSLSocket) serverSocket.accept(); 
				    		System.out.println("Accepts: " + sock.getRemoteSocketAddress());
				    		System.out.println("begin dh key exchange with client");
				    		//The server acts as "Alice" because it is initiating
				    		//the first Diffie-Hellman key to encrypt login data
							DHKeyAlice dhka = new DHKeyAlice(sock);
							System.out.println("Communication with client now encrypted");
							//creates a class with a new AES key and access
							//to encryption and decryption functions
							AES serverAes = new AES(dhka.getAliceSecret());
							//Establishes a connection to the Database
							LoginHandler lh = new LoginHandler(sock, serverAes); 
				    		if (!lh.getVerified()) { continue;}	
				    		DHKeyEchoThread2 dhThread = new DHKeyEchoThread2(this, sock, lh.initiator);
				    		dhThread.start();
				    		DHClients.add(dhThread);
				    	} catch (IOException io) {
				    		System.out.println("Accept failed on " + SERVER_PORT);
				    		io.printStackTrace();
				    	} 
				    }
    }
    
    
    
//-- end main(String[])

} //-- End class EchoServer	
