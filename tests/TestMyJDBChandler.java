package tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import login.MyJDBChandler;

import java.util.*;
import java.io.*;

public class TestMyJDBChandler {

	public static void main(String[] args) {

	Connection myConnection = null;
	String table = "user_account";
	System.setProperty("javax.net.ssl.keyStore", "C:\\temp-openssl-32build\\serverKeystore\\serverkeystore");
	System.setProperty("javax.net.ssl.keyStorePassword", "serVerstoRepasS");
	
	MyJDBChandler handler = new MyJDBChandler("mysql", "secure_chat_db", "root", "comodo25PAnda", "localhost", 3306);
	
	try {
		//handler.loadDriver();
		myConnection = handler.getConnection();
		if (myConnection == null) {
			System.err.println("connection not made");
		}
		
		String[] results = handler.searchTable(myConnection, table, "nrispine7", "despinos7@usnews.com", true, true);
		for (String r : results) { 
		System.out.println(r);
		}
		
		
//		String newSaltAndHash = new TestSaltHashPass().testHash();
//		
//		String[] parts = newSaltAndHash.split(":");
//		int iterations = Integer.parseInt(parts[0]);
//		String salt = parts[1];
//		String hash = parts[2];
//		
//		
//		handler.updateRow(myConnection, table, "nrispine7", 8, salt, hash);
//		handler.insertRow(myConnection, table, "romeo16", "romeo@uh.edu", salt, hash, iterations, algo);
//		handler.deleteRow(myConnection, table, "romeo16", 102);
		
		} catch (SQLException s) {
			handler.printSQLException(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			handler.closeConnection(myConnection);
		}
	
	}
	
}//end class
