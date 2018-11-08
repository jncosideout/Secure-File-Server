package login;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

	private PrintWriter pw;
	private Connection myConnection;
	private MyJDBChandler handler; 
	private String table = "user_account";
	private boolean verified = false;
	public boolean initiator;
	private Scanner in;
	private AES serverAes;
	private String userName, email, givenPassword, fingerprints;
	
	public LoginHandler(SSLSocket socket, AES serverAes) throws IOException {

		this.serverAes = serverAes;
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		in = new Scanner(socket.getInputStream());
		String choice = in.nextLine();
		//We asked the user ahead of time to take responsibility for initiating 
		//Diffie-Hellman with the next client who logs in. We need to tell the 
		//server in advance what role this client will play in DH agreement
    	if (choice.equals("yes")) {initiator = true;} else {initiator = false;}
	
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
		String salt = null;
		boolean newUser = false;
		
		if (in.hasNextLine()) {
			request = in.nextLine();
			System.out.println("request received");
		}
		try {
			if (request.equals("NEW_USER")) {
				System.out.println("Creating new user entry");
				newUser = true;
				salt = serverAes.decrypt(in.nextLine());
			} else if (request.equals("RETURNING_USER")) {
				System.out.println("Validating returning user credentials");
				newUser = false;
			}			
			decryptCredentials();
			if (newUser) {
				insertNewUser(salt);
			} else {
				//perform a query on the database for the password hash and salt of this user
				String[] results = handler.searchTable(myConnection, table, userName, email, true, true);
				  //output format = results[0] = id; [1] = salt; [2] = hash; [3] = fingerprints [4] = iterations; [5] = hash_algo;
				sendAnswer(verify(results));
			}
		} catch (InvalidKeyException | NoSuchPaddingException | InvalidAlgorithmParameterException
			| IllegalBlockSizeException | BadPaddingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	/*Generate a new hash using password given to us and the salt retrieved from database 
	 * which corresponds to the profile that this user claims to be. Then compare the generated hash
	 * to the hash retrieved from the database. 
	 */
	protected boolean verify(String[] results) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = Integer.parseInt(results[4]);   //           stored hash           stored fingerprints			stored salt
		ValidateHashedPassW vhpw = new ValidateHashedPassW(givenPassword, results[2], fingerprints, results[3], iterations, results[1]); 
		return vhpw.validate() && vhpw.compareFingerPrints();//true if given pass matches stored pass
	}
	
	//respond to client with validation 
	protected void sendAnswer(boolean answer) {
			verified = answer;
			String access = (answer) ? "granted":"denied";
			pw.println(access);
			pw.flush();
			if (pw.checkError()) {
				System.err.println("error sending verification response");
			}
	}
	
	//new and returning users send these encrypted credentials, so 
	//receive them and decrypt them here
	private void decryptCredentials() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException{
			userName = serverAes.decrypt(in.nextLine());
			email = serverAes.decrypt(in.nextLine());
			givenPassword = serverAes.decrypt(in.nextLine());
			fingerprints = serverAes.decrypt(in.nextLine());
	}
	
	private void insertNewUser(String saltVal) throws SQLException {
		handler.insertRow(myConnection, table, userName, email, saltVal, givenPassword, fingerprints, 40000, "PBKDF2WithHmacSHA256");
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
