package myClient;

import java.net.Socket;             // Used to connect to the server

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.*;

import login.UserLogin;
import rsaEncryptSign.DHCryptoInitiator;
import rsaEncryptSign.DHKeyBob;

import java.io.Console;				//used for system.console.readPassword() which isn't working
import java.io.ObjectInputStream;   // Used to read objects sent from the server
import java.io.ObjectOutputStream;  // Used to write objects to the server
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;      // Needed to read from the console
import java.io.InputStreamReader;   // Needed to read from the console

import java.io.IOException;
import java.util.ArrayList;
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
	public static final String host = "localhost";
	
	private String userName;
	private String serverHost;
	private int serverPort;
	private String ksName; //file path of keystore
    private String	storePass;
    private String tsName; //file path of trust store
    private String	trustStorePass;
	
	/**
     * Main method.
     *
     * @param args  First argument specifies the server to connect to
     *
     */
    public static void main(String[] args) {
     Scanner userInputScanner = new Scanner(System.in);
	 	 
	 EchoClient client = new EchoClient(host, EchoServer.SERVER_PORT);
	 client.startClient(userInputScanner);

	 userInputScanner.close();
    }//-- end main(String[])


    private EchoClient(String host, int portNumber) {
    serverPort = portNumber;
    serverHost = host;
    }
    
    private void startClient(Scanner scan) {
    	ArrayList<char[]> jksPassWs = assignKeystorePaths();
    	SSLContext sc = initSSLContext(jksPassWs);
    	SSLSocketFactory factory = sc.getSocketFactory();
    	SSLSocket sock = connect(factory);
    	
    	try {
    		// handshake
//			sock.startHandshake();
//			System.out.println("handshake complete");
    		Thread.sleep(1000);
    		System.out.println("begin dh key exchange CLIENT");
			DHKeyBob dhkb = new DHKeyBob(sock);
    		AES userAes = new AES(dhkb.getBobSecret());
			UserLogin ul = new UserLogin(sock, scan, userAes);
    		//use an accessor function instead of public mem var
    		if (!ul.getVerified()) {
    			System.out.println("Access denied. Exiting program");
    			System.exit(1);
    			} else {System.out.println("Access granted."); }
			startChat(sock, scan, ul.getUserName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    private ArrayList<char[]> assignKeystorePaths() {
    	
		//System.out.println("What is the keystore file path?");
		ksName = "C:\\temp-openssl-32build\\clientKeystore\\clientkeystore.jks"; 			
				//scan.nextLine();

		//System.out.println("Input keystore password");
		storePass = "keYs4clianTs";
    	char[] spass = storePass.toCharArray();  				// password for keystore
    	
		//System.out.println("What is the trust store file path?");
		tsName = "C:\\temp-openssl-32build\\clientKeystore\\clientTrustStore.jks"; 			
				//scan.nextLine();

		//System.out.println("Input keystore password");
		trustStorePass = "clianTtrUst";
    	char[] tspass = trustStorePass.toCharArray();  				// password for TrustStore
     				
    	
//		System.out.println("What is the alias?");
//		String alias = scan.nextLine();
		
		//System.out.println("Input key password for %s", alias);
		String keypass = "client1";
    	char[] kpass = keypass.toCharArray();  // password for private key
     	ArrayList<char[]> passwords = new ArrayList<>();
     	passwords.add(spass);
     	passwords.add(tspass);
     	passwords.add(kpass);
     	
 		return passwords;  
     }
    
    public SSLContext initSSLContext(ArrayList<char[]> jksPassWs) {
    	
    	SSLContext sc =  null;
    	
    	try {
			sc = SSLContext.getInstance("TLSv1.2");
			
			//initialize KeyStore
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream ksfis = new FileInputStream(ksName);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			
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
		return sc;
    }
    
    public SSLSocket connect(SSLSocketFactory factory) {
    	SSLSocket sock = null;
    	try{
    	    // Connect to the specified server
    		
    	    sock = (SSLSocket) factory.createSocket(serverHost, serverPort);
//			String[] suites = {"TLS_RSA_WITH_AES_128_CBC_SHA256"};
//			sock.setEnabledCipherSuites(suites);
			
    	    Thread.sleep(1000);
    	    System.out.println("Connected to " + host + " on port " + EchoServer.SERVER_PORT);
    	    
	    } catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		    e.printStackTrace(System.err);
		}
		return sock;  
    }
    
    public void startChat(SSLSocket sock, Scanner scan, String userName) throws IOException, InterruptedException {

	    ServerThread serverThread = new ServerThread(sock, userName);
	    Thread serverAccessThread = new Thread(serverThread);
	    serverAccessThread.start();
	    
        PrintWriter serverOut = new PrintWriter(sock.getOutputStream(), false);

	    while (serverAccessThread.isAlive())
	    {
	    	if (scan.hasNextLine() ) {
	    		if (scan.hasNext("goodbye")) {
	    			sock.close();
		    	    System.out.println("Leaving chat");
	    			break;
	    		}
	    		else {
					String nextSend = scan.nextLine();
					serverOut.println(userName + " > " + nextSend);
					serverOut.flush();
				
					/*make sure 
					 * there were 
					 * no surprises
					 */
					if  (serverOut.checkError()) {
						System.err.println("ServerThread: java.io.PrintWriter error");
					}
				}
	    	}//end if
	    }//end while
	    
	    serverAccessThread.join();
	    System.err.println("after serverThread.join(); in EchoClient");
	    
    }

    public void printParams(SSLContext sc) {
		SSLParameters params = sc.getSupportedSSLParameters();
		String[] ciphers = params.getCipherSuites();
		String[] protocols = params.getProtocols();
		System.out.println("supported cipher suites are: \n");
		for (String c : ciphers) {
			System.out.println(c + "\n");
		}
		System.out.println("Supported protocols are: \n");
		for (String p : protocols) {
			System.out.println(p +"\n");
		}
		
		System.out.println("client session : " + sc.getClientSessionContext());
	
    }
   
} //-- end class EchoClient