package rsaEncryptSign;

import java.security.*;
import java.security.SecureRandom;
import java.io.*;
import java.nio.charset.*;
import sun.security.provider.*;
import  javax.crypto.*;
import java.security.cert.Certificate;

public class AsymCrypto {

	private KeyStore keystore;
	private String alias;
	private char[] keypass;
	private Certificate cert;
	private PrivateKey priv;

	public AsymCrypto(KeyStore ks, String as, char[] kp){
		keystore = ks;
		alias = as;
		keypass = kp;
		
		try {
			priv = (PrivateKey) keystore.getKey(alias, keypass);
			cert = keystore.getCertificate(alias);

		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
				
	}
	
	public byte[] encrypt(String plaintext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		SecureRandom random = SecureRandom.getInstanceStrong();
		cipher.init(Cipher.ENCRYPT_MODE, cert, random);
		
		return cipher.doFinal(plaintext.getBytes());
	}
	
	public byte[] decrypt(byte[] encrypted) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		SecureRandom random = SecureRandom.getInstanceStrong();
		cipher.init(Cipher.DECRYPT_MODE, priv, random);
		
		return cipher.doFinal(encrypted);
	}

	public String signHash(){
		
		return new String();
	}
	
	public String msgDigest(String ciphertext){
		
		return new String();
	}

}//end class
