package tests;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import rsaEncryptSign.AsymCrypto;

public class TestAsymCrypto {

	public static void main(String[] args) {
		
		String ksName; //file path of keystore
		ksName = "C:\\temp-openssl-32build\\serverKeystore\\NEWclientkeystore.jks"; 			

		String	storePass = "NEWkeYs4clianTs";
    	char[] spass = storePass.toCharArray();  				// password for keystore
    	
    	
		String keypass = "rsakeypass";
    	char[] kpass = keypass.toCharArray();  // password for private key
    	
    	String alias = "newRSAkey";
    	
    	 try {
						//initialize KeyStore
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream ksfis = new FileInputStream(ksName);
			BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
			
			ks.load(ksbufin, spass);
			ksfis.close();
			
			//create rsaMessaging object
			AsymCrypto rsaObj = new AsymCrypto(ks, alias, kpass);
		
			// encrypt the message
	        byte [] encrypted = rsaObj.encrypt("This is a secret message");     
	        System.out.println(new String(encrypted));  // <<encrypted message>>
	        
	        // decrypt the message
	        byte[] secret = rsaObj.decrypt(encrypted);                                 
	        System.out.println(new String(secret)); // This is a secret message
	        
    	 } catch (NoSuchAlgorithmException e1) {
 			// TODO Auto-generated catch block
 			System.err.println(e1.getMessage());
 			e1.printStackTrace();
 		} catch (KeyStoreException e2) {
 			System.err.println(e2.getMessage());
 			e2.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		} catch (CertificateException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception eee) {
 			System.err.println(eee.getMessage());
 			eee.printStackTrace();
 		}
			
	
	}//end main

}
