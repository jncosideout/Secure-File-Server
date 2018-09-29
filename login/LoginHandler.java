package login;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import login.MyJDBChandler;

import javax.net.ssl.SSLSocket;

public class LoginHandler {

	SSLSocket socket;
	PrintWriter pw;
	Connection myConnection;
	MyJDBChandler handler; 
	String table = "user_account";
	
	public LoginHandler(SSLSocket socket) throws IOException {
		this.socket = socket;
		pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		
//		System.setProperty("javax.net.ssl.keyStore", "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore");
//		System.setProperty("javax.net.ssl.keyStorePassword", "serVerstoRepasS");
		
		handler = new MyJDBChandler("mysql", "secure_chat_db", "root", "comodo25PAnda", "localhost", 3306);
		
		try {
			//handler.loadDriver();
			myConnection = handler.getConnection();
			receiveRequest();
			if (myConnection == null) {
				System.err.println("connection not made");
			}
		} catch (SQLException s) {
			handler.printSQLException(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.closeConnection(myConnection);
		}

	}

	protected void receiveRequest() throws IOException, SQLException {
		Scanner in = new Scanner(socket.getInputStream());
		String request = null;
		if (in.hasNextLine()) {
			request = in.nextLine();
		}
		if (request.equals("NEW_USER")) {
			insertNewUser();
		} else if (request.equals("RETURNING_USER")) {
			String userName = in.nextLine();
			String email = in.nextLine();
			String password = in.nextLine();
			String[] results = handler.searchTable(myConnection, table, userName, email, true, false);
			  //output[0] = id; [1] = salt; [2] = hash; [3] = iterations; [4] = hash_algo;
			
		}
		in.close();
	}
	
	protected boolean sendAnswer() {
		
		return false;
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
}//eoc
