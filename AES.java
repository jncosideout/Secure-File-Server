package myClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES 
{
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String CODIFICATION = "UTF-8";
	byte[] sharedSecret;
	public Cipher cipher;
	
	public AES(byte[] sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	
	//Encrypt Function
	public String encrypt(String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException,InvalidKeyException, IllegalBlockSizeException,BadPaddingException, IOException{
	 

	   // byte[] raw = DatatypeConverter.parseHexBinary(key);          //Converts the string key into an array of bytes
	 SecretKeySpec skeySpec = new SecretKeySpec(sharedSecret, 0, 16, "AES");  //SecretKeySpec used to construct a SecretKey from raw byte array
	        
	 Cipher cipher = Cipher.getInstance(ALGORITHM);         //getting encryption algorithm
	                                    
	 cipher.init(Cipher.ENCRYPT_MODE, skeySpec);           //initializing the cipher with a key and encrypt mode
	        
	 byte[] cipherText = cipher.doFinal(plaintext.getBytes(CODIFICATION)); //saving the plaintext file and coverting it to bytes and using cipher on it
	        
	 byte[] iv = cipher.getIV();         //Initialization Vector 
	        
	 ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //implements an output stream in which the data is written into a byte array
	        
	 outputStream.write(iv);    //calls the write method of argument iv on each of the bytes to be written out
	        
	 outputStream.write(cipherText); //calls the write method of argument ciphertext on each of the bytes to be written out
	        
	 byte[] finalData = outputStream.toByteArray(); //returns the current contents of this output stream, as a byte array
	        
	 String encodedFinalData = DatatypeConverter.printBase64Binary(finalData); //Converts an array of bytes into a string and we save that into encodedFinalData variable
	        
	 return encodedFinalData; //return encryption
	}
	//Decrypt Function
	//The same parameters that were used for encryption must be used for decryption
	public String decrypt(String encodedInitialData)throws InvalidKeyException, IllegalBlockSizeException,BadPaddingException, UnsupportedEncodingException,NoSuchAlgorithmException, NoSuchPaddingException,InvalidAlgorithmParameterException{
	 
	    byte[] encryptedData = DatatypeConverter.parseBase64Binary(encodedInitialData); //Converts the string into an array of bytes
	    
	// byte[] raw = DatatypeConverter.parseHexBinary(key); //Converts the string key into an array of bytes
	        
	 SecretKeySpec skeySpec = new SecretKeySpec(sharedSecret, 0, 16, "AES"); //constructs secret key
	 Cipher cipher = Cipher.getInstance(ALGORITHM);  
	        
	 byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16); //copies the ecryptedData array into iv array 
	        
	 byte[] cipherText = Arrays.copyOfRange(encryptedData, 16, encryptedData.length); //copies the ecryptedData array into cipherText array 
	        
	 IvParameterSpec iv_specs = new IvParameterSpec(iv); //iv_specs is created using the bytes in iv as the IV.
	        
	 cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv_specs);    //initializing the cipher with a key and decrypt mode
	        
	 byte[] plainTextBytes = cipher.doFinal(cipherText); //The data is decrypted
	        
	 String plainText = new String(plainTextBytes); //decodes the plainTextBytes array of bytes
	        
	 return plainText;  //return plaintext
	 }
	}
	  

