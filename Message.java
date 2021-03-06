package myClient;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Very stupid-simple message class.  By implementing the Serializble
 * interface, objects of this class can be serialized automatically by
 * Java to be sent across IO streams.  
 *
 *  
 */
public class Message implements java.io.Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1392477413891219772L;
	/** The text string encoded in this Message object */
    public String theMessage;
    private byte[] originalHash;
    private byte[] dhData;
    
    /**
     * Constructor.
     *
     * @param _msg The string to be encoded in this Message object
     *  use only after session key has been created.
     */
    public Message(String _msg, byte[] hash){
	theMessage = _msg;
	originalHash = hash;
    }
    
    //for verifying DSA signed data during DH key exchange
    public Message(byte[] data){
    	dhData = data;
//TODO sign data with sig obj and priv key
    	}
    
    //for encrypted string messages 
    //use only after session key has been created.
    public static byte[] computeHash(String aMessage, AES aes) {
        SecretKeySpec key = aes.getSkeySpec();
    	byte[] newHash = null;
    	try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			newHash = mac.doFinal(aMessage.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return newHash;
    }
    
    public byte[] signData(byte[] _data) {
    	byte[] newHash = null;
//TODO use a signature object and private key so sign data
    	return newHash;
    }
    
    
    public boolean compareHash(String toCompare, AES aes) {
    	byte[] newHash = computeHash(toCompare, aes);
    	
		int diff = originalHash.length ^ newHash.length;
		for (int i = 0; i < originalHash.length && i < newHash.length; i++) {
			diff |= originalHash[i] ^ newHash[i];
		}
		return diff == 0;    	
    }
    
    //for verifying DSA signed data during DH key exchange
    public boolean verifySig(byte[] _data) {
    	byte[] newHash = null;
  
		return false;    	
    }

}  //-- End class Message


