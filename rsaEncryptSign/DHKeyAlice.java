package rsaEncryptSign;

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
import javax.net.ssl.SSLSocket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DHKeyAlice {

	private SSLSocket socket;
	private KeyPairGenerator aliceKpairGen = null;
	private KeyAgreement aliceKeyAgree = null;
	private byte[] aliceSharedSecret = null;
	
	public DHKeyAlice(SSLSocket sock) {
		this.socket = sock;
		try {
			BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream bufIn = new BufferedInputStream(socket.getInputStream()); 
			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			DataInputStream dIn = new DataInputStream(socket.getInputStream()); 

			// TODO Alice encodes her public key, and sends it over to Bob.
			byte[] alicePubKeyEnc = genKeyPair();
			int aPKElen = alicePubKeyEnc.length;
			dOut.writeInt(aPKElen);
			Thread.sleep(200);
			bufOut.write(alicePubKeyEnc);
			bufOut.flush();
			Thread.sleep(200);
			// TODO receive bobPubKeyEnc
			int bPKElen = dIn.readInt();
			byte[] bobPubKeyEnc = new byte[bPKElen];
			bufIn.read(bobPubKeyEnc);
			alicePhaseOne(bobPubKeyEnc);
			// create shared secret
			int aliceLen = sharedSecret();
			// send aliceLen to bob
			dOut.writeInt(aliceLen);
			Thread.sleep(200);
			bufOut.write(aliceSharedSecret);
			bufOut.flush();
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	private byte[] genKeyPair() throws NoSuchAlgorithmException, InvalidKeyException {
		/*
         * Alice creates her own DH key pair with 2048-bit key size
         */
        System.out.println("ALICE: Generate DH keypair ...");
        aliceKpairGen = KeyPairGenerator.getInstance("DH");
        aliceKpairGen.initialize(2048);
        KeyPair aliceKpair = aliceKpairGen.generateKeyPair();
        
        // Alice creates and initializes her DH KeyAgreement object
        System.out.println("ALICE: Initialization ...");
        aliceKeyAgree = KeyAgreement.getInstance("DH");
        aliceKeyAgree.init(aliceKpair.getPrivate());
        
        // TODO Alice encodes her public key, and sends it over to Bob.
        byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
        System.out.println("alice encoded pub key");
        for (byte b : alicePubKeyEnc) {
        	System.out.print(b);
        }
        System.out.print("\n");
        
        return alicePubKeyEnc;
	}
	
	private void alicePhaseOne(byte [] bobPubKeyEnc) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalStateException {
		 /*
         * Alice uses Bob's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
        PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
        System.out.println("ALICE: Execute PHASE1 ...");
        aliceKeyAgree.doPhase(bobPubKey, true);
	}
	
	private int sharedSecret() { 
		/*
         * At this stage, both Alice and Bob have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        
        int aliceLen;
        
        aliceSharedSecret = aliceKeyAgree.generateSecret();
		aliceLen = aliceSharedSecret.length;
		System.out.println("Alice secret: " +
                toHexString(aliceSharedSecret));
         // TODO send aliceLen to Bob!    
		return aliceLen;
	}
	
	public byte[] getSharedSecret() { return aliceSharedSecret;}
		
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
