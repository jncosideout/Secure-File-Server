package login;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class ValidateHashedPassW {

	private String givenPassword;
	private String storedPassword;
	private int iterations;
	private String salt;
	private String givenFingerPrints;
	private String storedFingerPrints;
	
	public ValidateHashedPassW(String givenPassword, String storedPassword, String givenFingerPrints, 
								String storedFingerPrints, int iterations, String salt) {
		this.givenPassword = givenPassword;
		this.storedPassword = storedPassword;
		this.iterations = iterations;
		this.salt = salt;
		this.givenFingerPrints = givenFingerPrints;
		this.storedFingerPrints = storedFingerPrints; 
	}
	
	public boolean validate() throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] byteSalt = fromHex(salt);
		byte[] hash = fromHex(storedPassword);
		
		PBEKeySpec spec = new PBEKeySpec(givenPassword.toCharArray(), byteSalt, iterations, hash.length * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		byte[] testHash = skf.generateSecret(spec).getEncoded();
		
		int diff = hash.length ^ testHash.length;
		for (int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}
	
	public boolean compareFingerPrints() {
		char[] stored = storedFingerPrints.toCharArray();
		char[] given = givenFingerPrints.toCharArray();
		int diff = stored.length ^ given.length;
		for (int i = 0; i < stored.length && i < given.length; i++) {
			diff |= stored[i] ^ given[i];
		}
		return diff == 0;
	}
	
	public byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2*i, 2+2*i), 16);
		}
		return bytes;
	}
	
}//end class
