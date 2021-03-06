package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.*;
import java.io.*;

import java.sql.SQLWarning;

public class MyJDBChandler {

	  public String dbms;
	  public String dbName; 
	  public String userName;
	  public String password;
	  
	  private String serverName;
	  private int portNumber;

	  public MyJDBChandler(String dbms, String dbName, String userName, String password, String serverName, int portNumber) {
		this.dbms = dbms;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
		this.serverName = serverName;
		this.portNumber = portNumber;
	  }
	  
	  public Connection getConnection() throws SQLException {
		    Connection conn = null;
		    Properties connectionProps = new Properties();
		    connectionProps.put("user", this.userName);
		    connectionProps.put("password", this.password);
		    connectionProps.put("verifyServerCertificate", "true");
		    connectionProps.put("useSSL", "true");
		    
		    String currentUrlString = null;

		    if (this.dbms.equals("mysql")) {

		    	currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName +
		                                      ":" + this.portNumber + "/" + dbName;
		    	
		      conn =
		          DriverManager.getConnection(currentUrlString,
		                                      connectionProps);
		      
		      conn.setCatalog(this.dbName);
		    } 

		    System.out.println("Connected to database");
		    return conn;
		  } 
	  
	  public void loadDriver() {
		        try {
		            // The newInstance() call is a work around for some
		            // broken Java implementations

		            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		        } catch (Exception ex) {
		            // handle the error]
		        	System.err.println(ex.getMessage());
		        	ex.printStackTrace();
		        }
		    }
		
	  
	  public void closeConnection(Connection connArg) {
		    System.out.println("Releasing all open database resources ...");
		    try {
		      if (connArg != null) {
		        connArg.close();
		        connArg = null;
		      }
		    } catch (SQLException sqle) {
		      printSQLException(sqle);
		    }
		  }
	  
	  public void printSQLException(SQLException ex) {
		    while (ex != null) {
		      System.err.println("SQLState: " + ex.getSQLState());
		      System.err.println("Error Code: " + ex.getErrorCode());
		      System.err.println("Message: " + ex.getMessage());
		      Throwable t = ex.getCause();
		      while (t != null) {
		        System.out.println("Cause: " + t);
		        t = t.getCause();
		      }
		      ex = ex.getNextException();
		    }
		  }
	
	  //output[0] = id; [1] = salt; [2] = hash; [3] = fingerprints [4] = iterations; [5] = hash_algo;
	  public String[] searchTable(Connection con, String tableName, String userName, String email, 
			  						boolean wantSaltHash, boolean wantIterAlgo) throws SQLException {
		 Statement stmt = null;
		 String query = null;
		 String salt = null;
		 String hash = null;
		 String id = null;
		 String fgrpts = null;
		 int iterations = 0;
		 String hash_algo = null;
		 
		 if (wantSaltHash && wantIterAlgo){
			 query = "SELECT id, salt, hash, iterations, hash_algorithm, SHA256_fingerprints FROM " + dbName + "." + tableName + 
					  " WHERE user_name = \'" + userName  + "\'" + " AND email = \'" + email + "\'";		 }
		 else if (wantSaltHash) {
			 	query = "SELECT id, salt, hash, SHA256_fingerprints FROM " + dbName + "." + tableName + 
				  " WHERE user_name = \'" + userName  + "\'" + " AND email = \'" + email + "\'";
			 } 
		 
		 else if (wantIterAlgo) {
				 query = "SELECT id, iterations, hash_algorithm FROM " + dbName + "." 
		 + tableName + " WHERE user_name = \'" + userName + "\'" + " AND email = \'" + email + "\'";
			 }
		 else if (!wantSaltHash && !wantIterAlgo){
				 System.out.println("uspecified query");
			 }
		 
		 try {
			 stmt = con.createStatement();
			 ResultSet rs = null;
			 rs = stmt.executeQuery(query);
			 while (rs.next()) {
				 id = rs.getString("id");
				   if (wantSaltHash) {
					 salt = rs.getString("salt");
					 hash = rs.getString("hash");
					 fgrpts = rs.getString("SHA256_fingerprints");
				 } if (wantIterAlgo) {
					 iterations = rs.getInt("iterations");
					 hash_algo = rs.getString("hash_algorithm");
				 }
			 }
		 } catch (SQLException sql) {
			 printSQLException(sql);
		 } finally {
			 if (stmt != null) {  stmt.close(); }
		 }
		 
		 String[] output = new String[6];
		 output[0] = id;
		 
		   if (wantSaltHash) {
			 output[1] = salt;
			 output[2] = hash;
			 output[3] = fgrpts;
		 } if (wantIterAlgo) {
			 output[4] = Integer.toString(iterations);
			 output[5] = hash_algo;		
		 } 
		 
		  return output;
	  }
	  
	  public void updateRow(Connection con, String tableName, String userName, String email, 
			  					int id, String saltVal, String hashVal) throws SQLException {
		  
		  PreparedStatement update = null;
		  
		  String updateString = "UPDATE " + dbName + "." + tableName + " SET ";
		  if (hashVal != null && saltVal != null) { updateString += "salt = ?, hash = ?";}
		  if (userName != null && email != null) { updateString += ", user_name = ?, email = ?";}
		  updateString +=  " WHERE id = " + Integer.toString(id);
		  
		  try {
			  con.setAutoCommit(false);
			  update = con.prepareStatement(updateString);
			  update.setString(1, saltVal);
			  update.setString(2, hashVal);
			  update.setString(3, userName);
			  update.setString(4, email);
			  update.executeUpdate();
			  con.commit();
		  } catch (SQLException sql) {
			  printSQLException(sql);
			  
			  if (con != null) {
				  try {
				  System.err.println("Transaction is being rolled back");
				  con.rollback();
				  } catch (SQLException s) {
					  printSQLException(s);
				  }
			  }
		  } finally { 
			  if (update != null) {
				  update.close();
			  }
		  con.setAutoCommit(true);
		  }
	  }
	  
	  public void insertRow(Connection con, String tableName, String userName, String email,
			 				String saltVal, String hashVal, String fingerPrints, int iterations, String algo) throws SQLException {
			  
			  PreparedStatement insert = null;
			  
			  String insertString = "INSERT INTO " + dbName + "." + tableName +
					  			" (user_name, email, iterations, salt, hash, hash_algorithm, SHA256_fingerprints) " + 
					  " VALUES (?, ?, ?, ?, ?, ?, ?)";
			  
			  try {
				  con.setAutoCommit(false);
				  insert = con.prepareStatement(insertString);
				  insert.setString(1, userName);
				  insert.setString(2, email);
				  insert.setInt(3, iterations);
				  insert.setString(4, saltVal);
				  insert.setString(5, hashVal);
				  insert.setString(6, algo);
				  insert.setString(7, fingerPrints);
				  insert.executeUpdate();
				  con.commit();
			  } catch (SQLException sql) {
				  printSQLException(sql);
				  
				  if (con != null) {
					  try {
					  System.err.println("Transaction is being rolled back");
					  con.rollback();
					  } catch (SQLException s) {
						  printSQLException(s);
					  }
				  }
			  } finally { 
				  if (insert != null) {
					  insert.close();
				  }
			  con.setAutoCommit(true);
			  }
		  }
	 
		  public void deleteRow(Connection con, String tableName, String userName, int id) throws SQLException {
	
				PreparedStatement delete = null;
				
				String deleteString = "DELETE FROM " + dbName + "." + tableName +
						  			" WHERE id = ? AND user_name = ?"; 
						  
		
				try {
					  con.setAutoCommit(false);
					  delete = con.prepareStatement(deleteString);
					  delete.setInt(1, id);
					  delete.setString(2, userName);
					  
					  delete.executeUpdate();
					  con.commit();
				} catch (SQLException sql) {
					  printSQLException(sql);
					  
					  if (con != null) {
						  try {
						  System.err.println("Transaction is being rolled back");
						  con.rollback();
						  } catch (SQLException s) {
							  printSQLException(s);
						  }
					  }
				} finally { 
					  if (delete != null) {
						  delete.close();
					  }
				con.setAutoCommit(true);
				}
		}
	  
 
	  
}//end class
