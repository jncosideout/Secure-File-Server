package login;

import javax.net.ssl.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class UserLogin {

	private String userName;
	private String email;
	private String givenPassword;
	private SSLSocket socket;
	PrintWriter pw;
	private boolean verified = false;
	
	public UserLogin(SSLSocket sock, Scanner userInputScanner) throws IOException {
		socket = sock;
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
				    	registerNewUser();
					} else if (choice.trim().contains("return")) {
						System.out.println("Welcome back. Please enter your username");
						userName = "beatya4";
						//userInputScanner.nextLine();
				    	System.out.println("Please type your email and press enter");
						email = "asbeaty@uh.edu";
						//userInputScanner.nextLine();
				    	System.out.println("Please type your password and press enter");
				    	givenPassword = "apple3456";
				    			//userInputScanner.nextLine();
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
	
	protected void registerNewUser() {
		SaltHashPassW hashP = new SaltHashPassW(givenPassword, 40000); 
		try {
			String [] itSaHa = hashP.createNewHash();
			this.givenPassword = itSaHa[2];
					
			pw.write("NEW_USER");
			pw.flush();
			
			sendCredentials();
		} catch (IOException e) {
			System.err.println(e.getMessage());
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
	
	protected String createCertificate() {
		return new String();
	}
	
	protected void sendCredentials() throws IOException {
	try {		
			pw.println(userName);
			pw.flush();
			Thread.sleep(200);
			pw.println(email);
			pw.flush();
			Thread.sleep(200);
			pw.println(givenPassword);
			pw.flush();
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
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
	
	public String getUserName(){ return userName;}
	public boolean getVerified(){ return verified;}
}//eoc
