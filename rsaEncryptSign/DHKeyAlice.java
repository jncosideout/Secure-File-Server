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
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
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

	private KeyPairGenerator aliceKpairGen = null;
	private KeyAgreement aliceKeyAgree = null;
	private byte[] aliceSharedSecret = null;
	private java.security.cert.Certificate myCert;
	
	public DHKeyAlice(SSLSocket socket, java.security.cert.Certificate myCert, PrivateKey myPrivKey, boolean toClientPeer) {
		this.myCert = myCert;
		
		try {
			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			DataInputStream dIn = new DataInputStream(socket.getInputStream()); 
			
			//needed to kickstart synchronized handshake with server
			if (!toClientPeer) {dOut.writeInt(1);}

			//send our encoded cert to peer
			/*two methods because client-to-client
			 * goes through DHKeyEchoThread2
			 */
			if (toClientPeer) { sendCertificateToClientPeer(dOut); //to another client
			} else {sendCertificate(dOut); } //to server
			/*//receive our peer's RSA cert
			 * then extract RSA public key from it
			 */
			PublicKey bobsPubKey = receiveCertInstantiateKey(dIn);
			//for signing DH prime num parameters
			GenSig gs = new GenSig(myPrivKey);
			//for verifying peer's DH prime num parameters
			VerSig vs = new VerSig(bobsPubKey);
			
			// Alice encodes her DH public key, and sends it over to Bob.
			byte[] alicePubKeyEnc = genKeyPair();
			int aPKElen = alicePubKeyEnc.length;
			dOut.writeInt(aPKElen);
			Thread.sleep(200);
			dOut.write(alicePubKeyEnc);
			dOut.flush();
			gs.sendSignature(dOut, alicePubKeyEnc);
			Thread.sleep(500);
			
			//  receive Bob's DH Public Key
			int bPKElen = dIn.readInt();
			byte[] bobPubKeyEnc = new byte[bPKElen];
			dIn.readFully(bobPubKeyEnc);
			if (!vs.verifySignature(dIn, bobPubKeyEnc)){
			throw new Exception("Invalid Signature! Discontinuing DH Key exchange.");	
			}
			System.out.println("Signature is valid");
			alicePhaseOne(bobPubKeyEnc);
			
			// create shared secret
			int aliceLen = sharedSecret();
			// send aliceLen to bob
			dOut.writeInt(aliceLen);
			Thread.sleep(200);
			//DEMO PURPOSES TO CONFIRM SAME SHAREDSECRET
			dOut.write(aliceSharedSecret);
			dOut.flush();
			//to keep this thread from exiting early
			if (toClientPeer) { dIn.readInt();}
			
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception falseSig) {
			System.out.println(falseSig.getMessage());
		}
		
		
		
	}

	private void sendCertificate(DataOutputStream dos) throws IOException {
        /* Send the public certificate to peer */	
		byte[] encodedCert = null;
		try {
			encodedCert = myCert.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
        dos.write(encodedCert);
        dos.flush();
	}
	
	private void sendCertificateToClientPeer(DataOutputStream dos) throws IOException {
        /* Send the public certificate to peer
         * We use this method when DH agreement takes 
         * place between two connected clients 
         * indirectly via their DHKeyEchoThreads
         *  */
		byte[] encodedCert = null;
		try {
			encodedCert = myCert.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		int certLen = encodedCert.length;
		dos.writeInt(certLen);
        dos.write(encodedCert);
        dos.flush();
	}
	
	private PublicKey receiveCertInstantiateKey (DataInputStream dIn) throws IOException {
        
        /* import encoded public cert */
        java.security.cert.Certificate bobsCert = null;
		try {
			java.security.cert.CertificateFactory cf =
	        		java.security.cert.CertificateFactory.getInstance("X.509");
			//automatically read in through dIn
			bobsCert = cf.generateCertificate(dIn);
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		PublicKey bobsPubKey = bobsCert.getPublicKey();
		System.out.println("here is your peer's RSA public key");
		System.out.println(bobsPubKey.toString());
		return bobsPubKey;
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
         //  send aliceLen to Bob so he can finish the protocol    
		return aliceLen;
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
	
    public byte[] getAliceSecret() { return aliceSharedSecret;}
}//eoc
