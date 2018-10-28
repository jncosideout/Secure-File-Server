package login;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.*;

import ExecuteBat.ProcessBuilderExample;
import myClient.AES;
import rsaEncryptSign.DHCryptoInitiator;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class UserLogin {

	private String userName;
	private String email;
	private String givenPassword;
	private SSLSocket socket;
	private PrintWriter pw;
	private boolean verified = false;
	private boolean initiator;
	private AES userAes;
	private String alias, keyPass, keystore, storePass, csrFile;
	private String fingerprints = "";
	
	public UserLogin(SSLSocket sock, Scanner userInputScanner, AES userAes, String newAlias,
				String keystore, String storePass, String csrFile) throws IOException {
		socket = sock;
		this.userAes = userAes;
		alias = newAlias;
		this.keystore = keystore;
		this.storePass = storePass;
		this.csrFile = csrFile;
		
		pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
		
		 System.out.println("New user or returning user?");
		 String choice = null;
		 while (choice == null || choice.trim().equals("")) {
				try{
					choice = "returning";
							//userInputScanner.nextLine();
				    if (choice.trim().contains("new")) {
				    	System.out.println("Please type new username and press enter");
				    	userName = userInputScanner.nextLine();
				    	System.out.println("Please type your email and press enter");
				    	email = userInputScanner.nextLine();
				    	System.out.println("Please type a strong password and press enter");
				    	givenPassword = userInputScanner.nextLine();
						System.out.printf("Input new alias for %s new certificate", userName);
						alias = "ClientC";
						System.out.printf("Input new key password for %s new certificate", alias);
						keyPass = alias + "pass";
				    	registerNewUser(userInputScanner);
				    	verified = false;
					} else if (choice.trim().contains("return")) {
						System.out.println("Welcome back.\nPlease enter your username and press enter");
						userName = alias;
						//userInputScanner.nextLine();
				    	System.out.println("Please type your email and press enter");
						email = alias + "@email.com";
						//userInputScanner.nextLine();
				    	System.out.println("Please type your password and press enter");
				    	//TODO HARD CODED LOGIN FOR TESTING PURPOSES ONLY 
				    	if (alias.equals("newClientA")) {
				    		givenPassword = "newClientA-3456password";
				    	} else {
				    	givenPassword = "newClientB-65478password";
				    	}
				    			//userInputScanner.nextLine();
				    	
						//We need to ask the user ahead of time to take responsibility for initiating 
						//Diffie-Hellman with the next client who logs in. For each pair of users 
						//the first one to log in MUST choose 'YES'
				    	System.out.println("Please type YES to initiate Diffie-Hellman \n NO to receive request");
				    	String choice2 = userInputScanner.nextLine();
				    	if (choice2.toUpperCase().contains("YES")) {initiator = true;} else {initiator = false;}
				    	pw.println(choice2);
				    	pw.flush();
				    	verified = returningUser();
					} else {
				    	System.out.println("Usernames/password must not be blank. Please try again");

					}
				}catch(Exception e){
				    System.out.println( "error reading from keyboard");
				    e.printStackTrace(System.err);
					}
	 	}
	}
	
	protected void registerNewUser(Scanner userInput) {
		SaltHashPassW hashP = new SaltHashPassW(givenPassword, 40000); 
		try {
			String [] itSaHa = hashP.createNewHash();
			this.givenPassword = itSaHa[2];
			createCertificate(userInput);
			pw.write("NEW_USER");
			pw.flush();		
			pw.write(userAes.encrypt(itSaHa[1]));//send salt
			pw.flush();
			sendCredentials();
			
		} catch (IOException |InvalidKeyException |NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException | IllegalBlockSizeException |BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	protected boolean returningUser() {
		boolean granted = false;		
		try {			
			pw.println("RETURNING_USER");
			pw.flush();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendCredentials();
			granted = receiveValidation();
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return granted;
	}
	
	protected void createCertificate(Scanner userInput) {
		keystore = alias + "Keystore.jks";
		storePass = alias + "StorePass";
		try {																//
			ProcessBuilderExample pbe = new ProcessBuilderExample("genkey", alias, keyPass, keystore, storePass, null, userInput);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void sendCredentials() throws IOException {
	try {		
			pw.println(userAes.encrypt(userName));
			pw.flush();
			Thread.sleep(100);
			pw.println(userAes.encrypt(email));
			pw.flush();
			Thread.sleep(100);
			pw.println(userAes.encrypt(givenPassword));
			pw.flush();
			Thread.sleep(100);
			getFingerprints();
			pw.println(userAes.encrypt(fingerprints));
			pw.flush();
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			
	}
	
	//true if access granted
	protected boolean receiveValidation() throws IOException {
		boolean access = false;
		
		Scanner in = new Scanner(socket.getInputStream());
			
		if (in.hasNextLine()) {
			String reply = in.nextLine();
			if (reply.equals("denied")) { access = false;}
			else if (reply.equals("granted")) {access = true;}
		}
		return access;
	}
	
	
	private void getFingerprints() {
	    
		try {								//non-applicable args			//keypass                  csr filename
	    	ProcessBuilderExample pbe = new ProcessBuilderExample("list", alias, "", keystore, storePass, "", null);
	    	fingerprints = pbe.getfPrints();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getUserName(){ return userName;}
	public boolean getVerified(){ return verified;}
	public boolean getInitiator(){ return initiator;}

}//eoc
