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
	
	/* Returns a String array containing
	 * [0] = number of iterations as a String
	 * [1] = a hex String of the salt 
	 * [2] = a hex String of the hash 
	 * */
	public String[] generatePasswordHash(int keyLength, String hash_algorithm, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		char[] pass = originalPassword.toCharArray();
		
		//keyLength set to 64 when generating pw for first time storage
		PBEKeySpec spec = new PBEKeySpec(pass, salt, iterations, keyLength * 8);
		SecretKeyFactory skf = 	SecretKeyFactory.getInstance(hash_algorithm);
		byte[] hash = skf.generateSecret(spec).getEncoded();
				
		String[] iterations_salt_hash = new String[3];
		iterations_salt_hash[0] = Integer.toString(iterations);
		iterations_salt_hash[1] = toHex(salt);
		iterations_salt_hash[2] = toHex(hash);
		return  iterations_salt_hash;
	}
	
	public String[] createNewHash() {
		byte[] salt;
		String[] iterations_salt_hash = new String[3];
		try {
			salt = getSalt();
			iterations_salt_hash = generatePasswordHash(64, "PBKDF2WithHmacSHA256", salt);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return iterations_salt_hash;
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
