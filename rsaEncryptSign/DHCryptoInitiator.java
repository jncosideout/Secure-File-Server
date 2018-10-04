package rsaEncryptSign;

import java.io.IOException;
/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DHCryptoInitiator {

    /*
     * Now let's create a SecretKey object using the shared secret
     * and use it for encryption. First, we generate SecretKeys for the
     * "AES" algorithm (based on the raw shared secret data) and
     * Then we use AES in CBC mode, which requires an initialization
     * vector (IV) parameter. Note that you have to use the same IV
     * for encryption and decryption: If you use a different IV for
     * decryption than you used for encryption, decryption will fail.
     *
     * If you do not specify an IV when you initialize the Cipher
     * object for encryption, the underlying implementation will generate
     * a random one, which you have to retrieve using the
     * javax.crypto.Cipher.getParameters() method, which returns an
     * instance of java.security.AlgorithmParameters. You need to transfer
     * the contents of that object (e.g., in encoded format, obtained via
     * the AlgorithmParameters.getEncoded() method) to the party who will
     * do the decryption. When initializing the Cipher for decryption,
     * the (reinstantiated) AlgorithmParameters object must be explicitly
     * passed to the Cipher.init() method.
     */
	
    private byte[] bobSharedSecret = null;
    private SecretKeySpec bobAesKey = null;
    Cipher bobCipher = null;
    
    public DHCryptoInitiator(byte[] bobSharedSecret) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.bobSharedSecret = bobSharedSecret;
		bobCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		System.out.println("Use shared secret as SecretKey object ...");
	    bobAesKey = new SecretKeySpec(bobSharedSecret, 0, 16, "AES");
	}
    
    public byte[] encrypt() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    /*
     * Bob encrypts, using AES in CBC mode
     */
    bobCipher.init(Cipher.ENCRYPT_MODE, bobAesKey);
    byte[] cleartext = "This is just an example".getBytes();
    byte[] ciphertext = bobCipher.doFinal(cleartext);
    
    return ciphertext;
    }

    public void sendParams() throws IOException { //bobCipher should be in ENCRYPT_MODE?
    // Retrieve the parameter (IV) that was used, and transfer it to Alice in
    // encoded format
    byte[] encodedParams = bobCipher.getParameters().getEncoded();
    // TODO transfer params
    }
    
    
    public byte[] decrypt(byte[] ciphertext) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	/*
         * Bob decrypts, using AES in CBC mode
         */

        AlgorithmParameters aesParams = bobCipher.getParameters();
        Cipher aliceCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aliceCipher.init(Cipher.DECRYPT_MODE, bobAesKey, aesParams);
        byte[] recovered = aliceCipher.doFinal(ciphertext);
        //for demo purposes
//        if (!java.util.Arrays.equals(cleartext, recovered))
//            throw new Exception("AES in CBC mode recovered text is " +
//                    "different from cleartext");
//        System.out.println("AES in CBC mode recovered text is " +
//                "same as cleartext");
        return recovered;
	}

/*
 * Converts a byte to hex digit and writes to the supplied buffer
 */
private static void byte2hex(byte b, StringBuffer buf) {
    char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    int high = ((b & 0xf0) >> 4);
    int low = (b & 0x0f);
    buf.append(hexChars[high]);
    buf.append(hexChars[low]);
}

/*
 * Converts a byte array to hex string
 */
private static String toHexString(byte[] block) {
    StringBuffer buf = new StringBuffer();
    int len = block.length;
    for (int i = 0; i < len; i++) {
        byte2hex(block[i], buf);
        if (i < len-1) {
            buf.append(":");
        }
    }
    return buf.toString();
}
}
