package rsaEncryptSign;
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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.net.ssl.SSLSocket;

public class DHKeyBob {

	private KeyAgreement bobKeyAgree = null;
	private byte[] bobSharedSecret = null;
	
	public DHKeyBob(SSLSocket socket) {
		try {
			DataInputStream dIn = new DataInputStream(socket.getInputStream()); 
			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			// receive alicePubKeyEnc
			int aPKElen = dIn.readInt();
			byte[] alicePubKeyEnc = new byte[aPKElen];
			dIn.readFully(alicePubKeyEnc);
			PublicKey alicePubKey = createAlicePubKey(alicePubKeyEnc);
			// send bobPubKeyEnc
			byte[] bobPubKeyEnc = bobPubKey(alicePubKey);
			// DEMO PURPOSES
			System.out.println("bob encoded pub key");
	        for (byte b : bobPubKeyEnc) {
	        	System.out.print(b);
	        }
	        System.out.print("\n");
	        int bPKElen = bobPubKeyEnc.length;
			dOut.writeInt(bPKElen);
			Thread.sleep(200);
			dOut.write(bobPubKeyEnc);
			dOut.flush();
			Thread.sleep(200);
			// receive aliceLen
			int aliceLen = dIn.readInt();
			// create bobSharedSecret
			sharedSecret(aliceLen, alicePubKey);
			// DEMO PURPOSES verify shared secret is the same for both parties
			byte[] aliceSharedSecret = new byte[aliceLen];
			dIn.readFully(aliceSharedSecret);
			confirmSharedSecret(aliceSharedSecret);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private PublicKey createAlicePubKey(byte[] alicePubKeyEnc) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		 /*
         * Let's turn over to Bob. Bob has received Alice's public key
         * in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);

        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
        return alicePubKey;
	}
	
	private byte[] bobPubKey(PublicKey alicePubKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
		/*
         * Bob gets the DH parameters associated with Alice's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamFromAlicePubKey = ((DHPublicKey)alicePubKey).getParams();

        // Bob creates his own DH key pair
        System.out.println("BOB: Generate DH keypair ...");
        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
        bobKpairGen.initialize(dhParamFromAlicePubKey);
        KeyPair bobKpair = bobKpairGen.generateKeyPair();

        // Bob creates and initializes his DH KeyAgreement object
        System.out.println("BOB: Initialization ...");
        bobKeyAgree = KeyAgreement.getInstance("DH");
        bobKeyAgree.init(bobKpair.getPrivate());

        // TODO Bob encodes his public key, and sends it over to Alice.
        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();

        return bobPubKeyEnc;
	}
	
	private byte[] sharedSecret(int aliceLen, PublicKey alicePubKey) throws InvalidKeyException, IllegalStateException {
		 /*
         * Bob uses Alice's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        System.out.println("BOB: Execute PHASE1 ...");
        bobKeyAgree.doPhase(alicePubKey, true);

        /*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */        
        int bobLen;

        try {
            
            bobSharedSecret = new byte[aliceLen];
            bobLen = bobKeyAgree.generateSecret(bobSharedSecret, 0);
        } catch (ShortBufferException e) {
        	// provide LARGER output buffer of required size
            System.out.println(e.getMessage());
        } 
        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));

        return bobSharedSecret;
	}
	
	public void confirmSharedSecret(byte[] aliceSharedSecret) throws Exception {
        System.out.println("Alice secret: " +
                toHexString(aliceSharedSecret));
        System.out.println("Bob secret: " +
                toHexString(bobSharedSecret));
        if (!java.util.Arrays.equals(aliceSharedSecret, bobSharedSecret))
            throw new Exception("Shared secrets differ");
        System.out.println("Shared secrets are the same");
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

    public byte[] getBobSecret() { return bobSharedSecret; }
}//eoc