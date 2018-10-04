package rsaEncryptSign;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DHCryptoReceiver {

	private AlgorithmParameters aesParams;
	private SecretKeySpec aliceAesKey;

	
	public DHCryptoReceiver(byte[] encodedParams, byte[] aliceSharedSecret) throws NoSuchAlgorithmException, IOException {
	    // Instantiate AlgorithmParameters object (IV) from parameter encoding
	    // obtained from Bob
	    aesParams = AlgorithmParameters.getInstance("AES");
	    aesParams.init(encodedParams);
		System.out.println("Use shared secret as SecretKey object ...");
		aliceAesKey  = new SecretKeySpec(aliceSharedSecret, 0, 16, "AES");
	}


	public byte[] decrypt(byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		/*
	     * Alice decrypts, using AES in CBC mode
	     */

	    Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    aliceCipher.init(Cipher.DECRYPT_MODE, aliceAesKey, aesParams);
	    byte[] recovered = aliceCipher.doFinal(ciphertext);
	    
//	    if (!java.util.Arrays.equals(cleartext, recovered))
//	        throw new Exception("AES in CBC mode recovered text is " +
//	                "different from cleartext");
//	    System.out.println("AES in CBC mode recovered text is " +
//	            "same as cleartext");
	    
	    return recovered;
		}
}//eoc

