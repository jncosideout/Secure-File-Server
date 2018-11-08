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
	private String alias, keystore, storePass, csrFile;
	private String keyPass;
	private String fingerprints = "";
	
	public UserLogin(SSLSocket sock, AES userAes, String newAlias, String keystore,	String storePass,
			String csrFile, String userName, String email, char[] givenPassword, char[] keyPass,
			int new_return, int dh_initiator) throws IOException {
		socket = sock;
		this.userAes = userAes;
		alias = newAlias;
		this.keystore = keystore;
		this.storePass = storePass;
		this.csrFile = csrFile;
		this.userName = userName;
		this.email = email;
		this.givenPassword = givenPassword.toString();
		this.keyPass = keyPass.toString(); 
		
		pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
				 
		 try{
			 if (new_return == 0) {
	
			    	registerNewUser();
			    	verified = false;
				} else if (new_return == 1) {
			    	//TODO HARD CODED LOGIN CREDENTIALS FOR TESTING PURPOSES ONLY 
					this.userName = alias;
					this.email = alias + "@email.com";
			    	//TODO HARD CODED PASSWORD FOR TESTING PURPOSES ONLY 
			    	if (alias.equals("newClientA")) {
			    		this.givenPassword = "newClientA-3456password";
			    	} else {
			    	this.givenPassword = "newClientB-65478password";
			    	}
			    		String dh_choice;
				    	if (dh_initiator == 0) {
				    		initiator = true;
				    		dh_choice = "yes";
				    	} else {
				    		initiator = false;
				    		dh_choice = "no";
				    	}
				    	pw.println(dh_choice);
				    	pw.flush();
				    	verified = returningUser();
				}	
			}catch(Exception e){
			    e.printStackTrace(System.err);
			}
	 	}
	
	
	protected void registerNewUser() {
		//create a new hash from password
		SaltHashPassW hashP = new SaltHashPassW(givenPassword, 40000); 
		try {
			String [] itSaHa = hashP.createNewHash();
			this.givenPassword = itSaHa[2];
			createCertificate();
			pw.write("NEW_USER");
			pw.flush();		
			pw.write(userAes.encrypt(itSaHa[1]));//send salt that was made
			pw.flush();
			sendCredentials();
			
		} catch (IOException |InvalidKeyException |NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException | IllegalBlockSizeException |BadPaddingException e) {
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
	
	protected void createCertificate() {
		keystore = alias + "Keystore.jks";
		storePass = alias + "StorePass";
		try {		//call Java keytool -genkey from the JRE to make a new keystore and certificate 														//
			ProcessBuilderExample pbe = new ProcessBuilderExample("genkey", alias, keyPass, keystore, storePass, null);
		} catch (IOException | InterruptedException e) {
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
			e.printStackTrace();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
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
	    //call Java keytool -list from JRE to retrieve SHA256 fingerprints of certificate
		try {								//non-applicable args			//keypass            csr blank for returning user
	    	ProcessBuilderExample pbe = new ProcessBuilderExample("list", alias, "", keystore, storePass, "");
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
