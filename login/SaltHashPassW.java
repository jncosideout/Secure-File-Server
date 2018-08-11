package login;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SaltHashPassW {

	private String originalPassword;
	private int iterations;

	public SaltHashPassW(String originalPassword, int iterations) {
		
		this.iterations = iterations;
		this.originalPassword = originalPassword;
	}
	
	//keyLength set to 64 when generating pw for first time storage
	public String generatePasswordHash(int keyLength, String hash_algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		char[] pass = originalPassword.toCharArray();
		byte[] salt = getSalt();
		
		PBEKeySpec spec = new PBEKeySpec(pass, salt, iterations, keyLength * 8);
		SecretKeyFactory skf = 	SecretKeyFactory.getInstance(hash_algorithm);
		byte[] hash = skf.generateSecret(spec).getEncoded();
				
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}
	
	public byte[] getSalt() throws NoSuchAlgorithmException {
		
		SecureRandom random = SecureRandom.getInstanceStrong();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		
		return salt;

	}
	
	public String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
		return hex;
		}
	}
	
}//end class
