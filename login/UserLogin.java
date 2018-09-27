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
	
	public UserLogin(SSLSocket sock) throws IOException {
		Scanner userInputScanner = new Scanner(System.in);

		 System.out.println("New user or returning user?");
		 String choice = "return";
		 while (choice == null || choice.trim().equals("")) {
				try{
					choice = userInputScanner.nextLine();
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
						userName = "'beatya4'";
						//userInputScanner.nextLine();
				    	System.out.println("Please type your email and press enter");
						email = "'asbeaty@uh.edu'";
						//userInputScanner.nextLine();
				    	System.out.println("Please type your password and press enter");
				    	givenPassword = "apple3456";
				    			//userInputScanner.nextLine();
					} else {
				    	System.out.println("Usernames/password must not be blank. Please try again");

					}
				}catch(Exception e){
				    System.out.println( "error reading from keyboard");
				    e.printStackTrace(System.err);
					}
	 	}
		 
		 
		userInputScanner.close();
		socket = sock;
		pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));

		
	}
	
	protected void registerNewUser() {
		SaltHashPassW hashP = new SaltHashPassW(givenPassword, 40000); 
		try {
			String [] itSaHa = hashP.generatePasswordHash(64, "PBKDF2WithHmacSHA256");
			this.givenPassword = itSaHa[2];
					
			pw.write("NEW_USER");
			pw.flush();
			
			sendCredentials();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	protected boolean returningUser() {
		boolean granted = false;		
		try {			
			pw.write("RETURNING_USER");
			pw.flush();
			
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
					
			pw.write(userName);
			pw.flush();
			pw.write(email);
			pw.flush();
			pw.write(givenPassword);
			pw.flush();
			
	}
	
	protected boolean receiveValidation() throws IOException {
		boolean granted = false;
		
		Scanner in = new Scanner(socket.getInputStream());
			
		if (in.hasNextBoolean()) {
			granted = in.nextBoolean();
		}
		in.close();
		return granted;
	}
}
