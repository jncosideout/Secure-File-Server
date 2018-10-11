package login;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocket;

import myClient.AES;
import rsaEncryptSign.DHCryptoReceiver;

public class LoginHandler {

	private SSLSocket socket;
	private PrintWriter pw;
	private Connection myConnection;
	private MyJDBChandler handler; 
	private String table = "user_account";
	private boolean verified = false;
	public boolean initiator;
	private Scanner in;
	private AES serverAes;
	
	public LoginHandler(SSLSocket socket, AES serverAes) throws IOException {
		this.socket = socket;
		this.serverAes = serverAes;
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		in = new Scanner(socket.getInputStream());
		String choice = in.nextLine();
		//We need to ask the user ahead of time to take responsibility for initiating 
		//Diffie-Hellman with the next client who logs in. For each pair of users 
		//the first one to log in MUST choose 'YES'
    	if (choice.toUpperCase().contains("YES")) {initiator = true;} else {initiator = false;}
		
//		System.setProperty("javax.net.ssl.keyStore", "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore");
//		System.setProperty("javax.net.ssl.keyStorePassword", "serVerstoRepasS");
		
    	//create a new MyJDBChandler to perform all db queries
		handler = new MyJDBChandler("mysql", "secure_chat_db", "root", "comodo25PAnda", "localhost", 3306);
		
		try {
			handler.loadDriver();//JDBC driver
			myConnection = handler.getConnection(); //connection to database
			receiveRequest();  //accept username/email/password that user has sent
			if (myConnection == null) {
				System.err.println("connection not made");
			}
		} catch (SQLException s) {
			handler.printSQLException(s);
		} catch (IOException io) {
			io.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.closeConnection(myConnection);
		}

	}

	protected void receiveRequest() throws IOException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		String request = null;
		String fingerprints = "";
		if (in.hasNextLine()) {
			request = in.nextLine();
			System.out.println("request received");
		}
		if (request.equals("NEW_USER")) {
			System.out.println("Creating new user entry");
			insertNewUser();
		} else if (request.equals("RETURNING_USER")) {
			System.out.println("Validating returning user credentials");
			String userName = null, email = null, password = null;
			try {
				userName = serverAes.decrypt(in.nextLine());
				email = serverAes.decrypt(in.nextLine());
				password = serverAes.decrypt(in.nextLine());
				fingerprints = serverAes.decrypt(in.nextLine());
			} catch (InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException
					| IllegalBlockSizeException | BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//perform a query on the database for the password hash and salt of this user
			String[] results = handler.searchTable(myConnection, table, userName, email, true, true);
			  //output format = results[0] = id; [1] = salt; [2] = hash; [3] = fingerprints [4] = iterations; [5] = hash_algo;
			sendAnswer(verify(results, password, fingerprints));
		}
	}
	
	protected boolean verify(String[] results, String givenPass, String fingerprints) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = Integer.parseInt(results[4]);   //           stored hash           stored fingerprints			stored salt
		ValidateHashedPassW vhpw = new ValidateHashedPassW(givenPass, results[2], fingerprints, results[3], iterations, results[1]); 
		return vhpw.validate() && vhpw.compareFingerPrints();//true if given pass matches stored pass
	}
	
	protected void sendAnswer(boolean answer) {
			verified = answer;
			String access = (answer) ? "granted":"denied";
			pw.println(access);
			pw.flush();
			if (pw.checkError()) {
				System.err.println("error sending verification response");
			}
	}
	
	private boolean insertNewUser() {
		return false;
	}
	
	private boolean deleteUser() {
		return false;
	}
	
	private boolean updateUser() {
		return false;
	}
	
	public boolean getVerified(){ return verified;}
	
	public boolean getInitiator(){ return initiator;}

}//eoc
