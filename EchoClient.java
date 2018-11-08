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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import login.UserLogin;
import rsaEncryptSign.DHCryptoInitiator;
import rsaEncryptSign.DHKeyAlice;
import rsaEncryptSign.DHKeyBob;

import java.io.DataOutputStream;
import java.io.ObjectOutputStream;  // Used to write objects to the server
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
/**
 * This class connects to an EchoServer to sends encrypted messages 
 *  back and forth.  Java message serialization is used to pass
 * Message objects around.
 *
 *
 */
public class EchoClient implements ActionListener, Runnable
{							
	public static final String host = "localhost";
	
	private String serverHost;
	private int serverPort;
	private String ksName; //file path of keystore
    private String	storePass;
    private String tsName; //file path of trust store
    private String	trustStorePass;
	private String alias;
	private char[] keyPass;
	private String userName;
	private String email;
	private char[] password;
	private java.security.cert.Certificate myCert;
	private PrivateKey myPrivKey;
	private ObjectOutputStream oos;
	private SSLSocket sock;
	private AES userAes;
	private int new_return;//'0'=new user, '1'=returning user
	private int initiate_DH;//'0' if chose to initiate DH with next logged in user
							//'1' if chose to be the next logged in user
	
    private final JFrame f = new JFrame();
    private final JTextField tf = new JTextField(25);
    protected final JTextPane tp = new JTextPane();
    private final JButton send = new JButton("Send");
    private Thread thread;


    public static void main(String[] args) {
	 	 
    	 new LoginWindow(host, EchoServer.SERVER_PORT);
    }//-- end main(String[])


    public EchoClient(String userName, char[] password, String alias, char[] keyPass, String email,
    		String host, int portNumber, int new_return, int initiate_DH) {
    this.userName = userName;
    this.password = password;
    this.alias = alias;
    this.keyPass = keyPass; 
    this.email = email;
    serverPort = portNumber;
    serverHost = host;
    this.new_return = new_return;
    this.initiate_DH = initiate_DH; 
    //setup GUI
    f.setTitle("Secure Chat Client");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getRootPane().setDefaultButton(send);
    f.setPreferredSize(new Dimension(400, 600));
    f.add(tf, BorderLayout.NORTH);
    f.add(new JScrollPane(tp), BorderLayout.CENTER);
    f.add(send, BorderLayout.SOUTH);
    f.pack();
    send.addActionListener(this);
    tp.setEditable(false);
    DefaultCaret caret = (DefaultCaret) tp.getCaret();
    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    display("Trying " + host + " on port " + portNumber);
    thread = new Thread(this);
    
    }
    
    public void start() {
        f.setVisible(true);
        thread.start();
    }
    
    public void run() {
    	ArrayList<char[]> jksPassWs = assignKeystorePaths();
    	SSLContext sc = initSSLContext(jksPassWs);
    	SSLSocketFactory factory = sc.getSocketFactory();
    	sock = connect(factory);
    	
    	try {
			DHKeyAlice dhka = null; //may use this later if client chose to initiate DH exchange 
    		Thread.sleep(200);
    		display("begin DH key exchange with server");
			DHKeyBob dhkb = new DHKeyBob(sock, myCert, myPrivKey, false);
			display("Communication with server now encrypted");
			//initialize an AES obj with new shared DH key
    		userAes = new AES(dhkb.getBobSecret());
    		display("Begin secure login");
    		/*If returning user, send credentials and receive authentication response
    		 * If new user, send new credentials, create new certificate
    		 * 		server stores new data and access is denied, program closes.
    		 * 		a new user will not be able to chat immediately because his certificate
    		 * 		has not been signed yet. This must happen offline.
    		 */
			UserLogin ul = new UserLogin(sock, userAes, alias, ksName, storePass, "", userName, email,
					password, keyPass, new_return, initiate_DH);
    		if (!ul.getVerified()) {								
    			display("Access denied. Exiting program");
    			System.exit(1);
    			} else {display("Access granted."); }
    		if (ul.getInitiator()) {
	    		display("Please hold for your correspondent to log in." );
	    		/* DH key exchange will begin after 2nd client joins.
	    		 * This client connection was handed off to her DHKeyEchoThread2
	    		 * which gets put in waiting state.
	    		 */
				dhka = new DHKeyAlice(sock, myCert, myPrivKey, true);
				userAes = new AES(dhka.getAliceSecret());
			} else {
	    		display("You are the second user. Your correspondent has initiated Diffie-Hellman." );
				/*DH key exchange between two clients may now begin.
				 * 2nd client's connection was handed off to his DHKeyEchoThread2
				 * which notifies the waiting thread
				 */
	    		dhkb = new DHKeyBob(sock, myCert, myPrivKey, true);
				userAes = new AES(dhkb.getBobSecret());
			}
    		/*newly created DH key is passed along with socket.
    		 * The server has passed its socket to an EchoThread
    		 * which will distribute chatroom messages
    		 */
			startChat();
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
    
    /*This sets up file descriptors and passwords for keystores and truststores
     * used in the TLS connection. This information is also used for RSA signatures
     * during DH key exchanges.
     * Method produces:
     * ArrayList<char[]> passwords: 
     * '0' = Key Store Pass
     * '1' = Trust Store Pass
     * '2' = alias pass
     */
    private ArrayList<char[]> assignKeystorePaths() {

		/*
		 * in production we would not use multiple keystores because
		 * A) one user per client, presumably would not want to share
		 * sccess to this program with another user
		 * B) Java JSSE KeyManagerFactory cannot be initialized with 
		 * keystores containing multiple PrivateKey entries
		 */
		if (alias.equals("newClientA")) {
		keyPass = "newClientAPass".toCharArray();
		} else { keyPass = "clientBPass".toCharArray();}

		//again, hard-coded for testing purposes
		if (alias.equals("newClientA")) {
		ksName = "C:\\temp-openssl-32build\\clientKeystore\\newClientKeystore.jks"; 			
		} else {
			ksName = "C:\\temp-openssl-32build\\clientKeystore\\CLIENTBkeystore.jks"; 			
		}
		//also hard-coded for testing purposes
		if (alias.equals("newClientA")) {
		storePass = "newClientStorePass";
		} else {
			storePass = "CLIENTBstorepass";
		}
    	char[] spass = storePass.toCharArray();// password for keystore
    	
		tsName = "C:\\temp-openssl-32build\\clientKeystore\\newClientTrustStore"; 			

		trustStorePass = "newClientTrustPass";
    	char[] tspass = trustStorePass.toCharArray();// password for TrustStore
     				
    	char[] kpass = keyPass;// password for alias' RSA private key
     	ArrayList<char[]> passwords = new ArrayList<>();
     	passwords.add(spass);
     	passwords.add(tspass);
     	passwords.add(kpass);
     	
 		return passwords;  
     }
    
    /*initialize an SSLContext for the client.
     * Set for TLS1.2 and register the client's
     *  keystore and truststore 
     */
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
    /*Return the socket for this connection
     * This is where TLS ciphersuites should be restricted
     */
    public SSLSocket connect(SSLSocketFactory factory) {
    	SSLSocket sock = null;
    	try{
    	    // Connect to the specified server
    		
    	    sock = (SSLSocket) factory.createSocket(serverHost, serverPort);
    	    String[] suites = sock.getSupportedCipherSuites();
//{"TLS_RSA_WITH_AES_128_CBC_SHA256"};
//			sock.setEnabledCipherSuites(suites);
			
    	    Thread.sleep(1000);
    	    display("\nConnected to " + host + " on port " + EchoServer.SERVER_PORT);
    	    
	    } catch(Exception e) {
			System.err.println("Error: " + e.getMessage());
		    e.printStackTrace(System.err);
		}
		return sock;  
    }
    /*Start a ServerThread to receive chat messages
     * Initialize our ObjectOutputStream
     * Print our TLS session details
     */
    public void startChat() throws IOException, InterruptedException,
    InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

	    ServerThread serverThread = new ServerThread(sock, userName, userAes, tp);
	    serverThread.execute();
	    
	    oos = new ObjectOutputStream(sock.getOutputStream());
	    //get session after connection is established
		SSLSession session = sock.getSession();
		
		display("Session details: ");
		display("\tProtocol: " + session.getProtocol());
		display("\tCipher suite: " + session.getCipherSuite());
		display("Begin chatting.");

    }
    /*Pushing 'send' button
     * chat messages are encrypted here using DH session key shared with 
     * one correspondent, then create an HMAC of the encrypted message,
     * then send them together to the correspondent.
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    //@Override
    public void actionPerformed(ActionEvent ae) {
        String s = tf.getText();
    	Message clientMsg = null;

        try {
			if (oos != null) {
					if (s.toUpperCase().contains("goodbye")) {
						sock.close();
			    	    display("Leaving chat");
					}
					else {
						String nextSend = userName + " > " + s;
						String ciphertext = userAes.encrypt(nextSend);
						byte[] cipherhash = Message.computeHash(ciphertext, userAes);
						clientMsg = new Message(ciphertext, cipherhash);
						oos.writeObject(clientMsg);
						oos.flush();
					
					}
				}//end if
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | IOException e) {
			e.printStackTrace();
		}
        
        tf.setText("");
    }
    /*Used to find out supported cipher suites and 
     * supported TLS versions
     */
    public void printParams(SSLContext sc) {
		SSLParameters params = sc.getSupportedSSLParameters();
		String[] ciphers = params.getCipherSuites();
		String[] protocols = params.getProtocols();
		display("supported cipher suites are: \n");
		for (String c : ciphers) {
			display(c + "\n");
		}
		display("Supported protocols are: \n");
		for (String p : protocols) {
			display(p +"\n");
		}
		
		display("client session : " + sc.getClientSessionContext());
	
    }
   /*Print stylized messages to the GUI
    * Each new line is added to the document model of 
    * the JTextPane
    */
    private void display(final String s) {
    	SwingUtilities.invokeLater(new Runnable() {
            //@Override
            public void run() {
            	StyledDocument doc = tp.getStyledDocument();

            //  Define a keyword attribute
            SimpleAttributeSet keyWord = new SimpleAttributeSet();
            StyleConstants.setForeground(keyWord, Color.RED);
            StyleConstants.setBackground(keyWord, Color.YELLOW);
            StyleConstants.setBold(keyWord, true);
        
            //  Add some text
            try
            {
                doc.insertString(doc.getLength(), s + "\n", keyWord );
            }
            catch(Exception e) { System.out.println(e); }
            }
        });
    }
    
    
    
} //-- end class EchoClient