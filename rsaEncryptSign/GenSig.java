/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
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
 *   - Neither the name of Oracle or the names of its
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
package rsaEncryptSign;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.net.*;

class GenSig {
	private static String ksName; //file path of keystore
    private static String	storePass;
	private static String alias;
	private static String keypass;
	private static PrivateKey priv;
	private static java.security.cert.Certificate cert;
	
    public static void main(String[] args) {

        /* Generate a DSA signature */

        //setup sockets
    	int port = 9999;
        String host = "localhost";
        
        try (Socket s = new Socket(host, port)){
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            InputStream in = s.getInputStream();
        
            
            /* retrieve a key pair */
            initKeystore(assignKeystorePaths());

            /* Create a Signature object and initialize it with the private key */

            Signature dsa = Signature.getInstance("SHA1withRSA"); 
            dsa.initSign(priv);

            /* Update and sign the data */
            byte[] buffer = "qwertyuiop1234567890-=][poiuytrewasdfghjklmnbvcxz".getBytes("UTF-8");
            dsa.update(buffer, 0, buffer.length);
            
            //Send raw data to peer
            int dataLen = buffer.length;
            dos.writeInt(dataLen);
            dos.write(buffer);
            
            /* Now that all the data to be signed has been read in, 
                    generate a signature for it */

            byte[] realSig = dsa.sign();
            int sigLen = realSig.length;
            dos.writeInt(sigLen);
            /* Send the signature to peer */
            dos.write(realSig);
            
            /* Send the public key to peer */
    		byte[] encodedCert = cert.getEncoded();
            int certLen = encodedCert.length;
            //int certLen = encodedCert.length;
            dos.writeInt(certLen);
            dos.write(encodedCert);

            dos.close();

        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
            e.printStackTrace();
        }

    };

 private static ArrayList<char[]> assignKeystorePaths() {
    	
		 //file path of keystore
		ksName = "C:\\temp-openssl-32build\\serverKeystore\\NEWclientkeystore.jks"; 			

		String	storePass = "NEWkeYs4clianTs";
		char[] spass = storePass.toCharArray();  				// password for keystore
 	
		System.out.printf("Input key password for %s ?\n", alias);
		String keypass = "rsakeypass";
		char[] kpass = keypass.toCharArray();  // password for private key
 	
    	
    	System.out.println("What is the certificate alias?\n");
		alias = "newRSAkey";				
				//scan.nextLine();
		
     	ArrayList<char[]> passwords = new ArrayList<>();
     	passwords.add(spass);
     	passwords.add(kpass);
     	
 		return passwords;  
     }

private static void initKeystore(ArrayList<char[]> jksPassWs) {
	try {		
		//initialize KeyStore
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream ksfis = new FileInputStream(ksName);
		BufferedInputStream ksbufin = new BufferedInputStream(ksfis);
		
		ks.load(ksbufin, jksPassWs.get(0));//element 0 is store pass
		ksfis.close();
		priv = (PrivateKey) ks.getKey(alias, jksPassWs.get(1)); //element 1 is keypass
		cert = ks.getCertificate(alias);
	} catch (NoSuchAlgorithmException e1) {
		// TODO Auto-generated catch block
		System.err.println(e1.getMessage());
		e1.printStackTrace();
	} catch (KeyStoreException e2) {
		System.err.println(e2.getMessage());
		e2.printStackTrace();
	} catch (CertificateException | IOException | UnrecoverableKeyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}//eoc


