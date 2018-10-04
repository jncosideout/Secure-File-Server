package login;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.SQLException;
import javax.net.ssl.SSLSocket;

public class LoginHandler {

	SSLSocket socket;
	PrintWriter pw;
	Connection myConnection;
	MyJDBChandler handler; 
	String table = "user_account";
	private boolean verified = false;
	
	public LoginHandler(SSLSocket socket) throws IOException {
		this.socket = socket;
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		
//		System.setProperty("javax.net.ssl.keyStore", "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore");
//		System.setProperty("javax.net.ssl.keyStorePassword", "serVerstoRepasS");
		
		handler = new MyJDBChandler("mysql", "secure_chat_db", "root", "comodo25PAnda", "localhost", 3306);
		
		try {
			handler.loadDriver();
			myConnection = handler.getConnection();
			receiveRequest();
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
		Scanner in = new Scanner(socket.getInputStream());
		String request = null;
		if (in.hasNextLine()) {
			request = in.nextLine();
			System.out.println("request received");
		}
		if (request.equals("NEW_USER")) {
			insertNewUser();
		} else if (request.equals("RETURNING_USER")) {
			String userName = in.nextLine();
			String email = in.nextLine();
			String password = in.nextLine();
			String[] results = handler.searchTable(myConnection, table, userName, email, true, true);
			  //output[0] = id; [1] = salt; [2] = hash; [3] = iterations; [4] = hash_algo;
			sendAnswer(verify(results, password));
		}
	}
	
	protected boolean verify(String[] results, String givenPass) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = Integer.parseInt(results[3]);
		ValidateHashedPassW vhpw = new ValidateHashedPassW(givenPass, results[2], iterations, results[1]); 
		//given stored iter salt
		return vhpw.validate();
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
}//eoc
