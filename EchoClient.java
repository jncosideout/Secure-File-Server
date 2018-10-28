package myClient;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.PrivateKey;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.*;

import login.UserLogin;
import rsaEncryptSign.DHCryptoInitiator;
import rsaEncryptSign.DHKeyAlice;
import rsaEncryptSign.DHKeyBob;

import java.io.DataOutputStream;
import java.io.ObjectOutputStream;  // Used to write objects to the server
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
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
	
	private String serverHost;
	private int serverPort;
	private String ksName; //file path of keystore
    private String	storePass;
    private String tsName; //file path of trust store
    private String	trustStorePass;
	private String alias;
	private String keypass;
	private java.security.cert.Certificate myCert;
	private PrivateKey myPrivKey;
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
    	ArrayList<char[]> jksPassWs = assignKeystorePaths(scan);
    	SSLContext sc = initSSLContext(jksPassWs);
    	SSLSocketFactory factory = sc.getSocketFactory();
    	SSLSocket sock = connect(factory);
    	
    	try {
			DHKeyAlice dhka = null; //possible DHKeyAlice
    		Thread.sleep(200);
    		System.out.println("begin DH key exchange with server");
			DHKeyBob dhkb = new DHKeyBob(sock, myCert, myPrivKey, false);
			System.out.println("Communication with server now encrypted");
    		AES userAes = new AES(dhkb.getBobSecret());			
			UserLogin ul = new UserLogin(sock, scan, userAes, alias, ksName, storePass, "");
    		if (!ul.getVerified()) {								
    			System.out.println("Access denied. Exiting program");
    			System.exit(1);
    			} else {System.out.println("Access granted."); }
    		if (ul.getInitiator()) {
	    		System.out.println("Please hold for your correspondent to log in." );
				dhka = new DHKeyAlice(sock, myCert, myPrivKey, true);
				userAes = new AES(dhka.getAliceSecret());
			} else {
	    		System.out.println("You are the second user. Your correspondent has initiated Diffie-Hellman." );
				dhkb = new DHKeyBob(sock, myCert, myPrivKey, true);
				userAes = new AES(dhkb.getBobSecret());
			}
			startChat(sock, scan, ul.getUserName(), userAes);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | 
				NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
    /*produces:
     * ArrayList<char[]> passwords: 
     * '0' = Key Store Pass
     * '1' = Trust Store Pass
     * '2' = alias pass
     */
    private ArrayList<char[]> assignKeystorePaths(Scanner scan) {

		System.out.println("What is the certificate alias?");
		alias =  scan.nextLine();
//"newClientA";//TODO testing
		//
		/*
		 * in production we would not use multiple keystores because
		 * A) one user per client, presumably would not want to share this
		 * app with another user
		 * B) Java JSSE KeyManagerFactory cannot be initialized with 
		 * keystores containing multiple PrivateKey entries
		 */
		System.out.printf("Input key password for %s\n", alias);
		if (alias.equals("newClientA")) {
		keypass = "newClientAPass";
		} else { keypass = "clientBPass";}

		
		if (alias.equals("newClientA")) {
		//System.out.println("What is the keystore file path?");
		ksName = "C:\\temp-openssl-32build\\clientKeystore\\newClientKeystore.jks"; 			
		} else {
			ksName = "C:\\temp-openssl-32build\\clientKeystore\\CLIENTBkeystore.jks"; 			
		}
		
		if (alias.equals("newClientA")) {
		//System.out.println("Input keystore password");
		storePass = "newClientStorePass";
		} else {
			storePass = "CLIENTBstorepass";
		}
    	char[] spass = storePass.toCharArray();  				// password for keystore
    	
		//System.out.println("What is the trust store file path?");
		tsName = "C:\\temp-openssl-32build\\clientKeystore\\newClientTrustStore"; 			
				//scan.nextLine();

		//System.out.println("Input keystore password");
		trustStorePass = "newClientTrustPass";
    	char[] tspass = trustStorePass.toCharArray();  				// password for TrustStore
     				
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
			//get client certificate and private key 
			myCert = ks.getCertificate(alias);
			myPrivKey = (PrivateKey) ks.getKey(alias, jksPassWs.get(2));
			//init factories
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("Sunx509");
			kmf.init(ks, jksPassWs.get(2));
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
			tmf.init(ts);
			
			SecureRandom random = SecureRandom.getInstanceStrong();
			sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), random);
			
			
    	} catch (NoSuchAlgorithmException e1) {
			System.err.println(e1.getMessage());
			e1.printStackTrace();
		} catch (KeyStoreException e2) {
			System.err.println(e2.getMessage());
			e2.printStackTrace();
		} catch (CertificateException | IOException 
					| UnrecoverableKeyException |KeyManagementException e) {
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
    	    System.out.println("\nConnected to " + host + " on port " + EchoServer.SERVER_PORT);
    	    
	    } catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		    e.printStackTrace(System.err);
		}
		return sock;  
    }
    
    public void startChat(SSLSocket sock, Scanner scan, String userName, AES userAes) throws IOException, InterruptedException,
    InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

	    ServerThread serverThread = new ServerThread(sock, userName, userAes);
	    Thread serverAccessThread = new Thread(serverThread);
	    serverAccessThread.start();
	    
	    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
	    
	    while (serverAccessThread.isAlive())
	    {
	    	Message clientMsg = null;
	    	if (scan.hasNextLine() ) {
	    		if (scan.hasNext("goodbye")) {
	    			sock.close();
		    	    System.out.println("Leaving chat");
	    			break;
	    		}
	    		else {
					String nextSend = userName + " > " + scan.nextLine();
					String ciphertext = userAes.encrypt(nextSend);
					byte[] cipherhash = Message.computeHash(ciphertext, userAes);
					clientMsg = new Message(ciphertext, cipherhash);
					oos.writeObject(clientMsg);
					oos.flush();
				
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