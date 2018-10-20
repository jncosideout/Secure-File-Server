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
import java.security.spec.*;
import java.net.*;

class VerSig {

    public static void main(String[] args) {

        /* Verify a DSA signature */
    	int port = 9999;
    	
        try(ServerSocket s = new ServerSocket(port)){ 
        	System.out.println("waiting for client");
        	Socket c = s.accept();
            System.out.println("accepted conn");
            OutputStream out = c.getOutputStream();
            DataInputStream din = new DataInputStream(c.getInputStream());
        	
            //import raw data
            int dataLen = din.readInt();
            byte[] rawData = new byte[dataLen];
            din.readFully(rawData);
            
            /* import the signature bytes */
            int siglen = din.readInt();
            byte[] sigToVerify = new byte[siglen]; 
            din.readFully(sigToVerify );
            
            /* import encoded public cert */
            int certLen = din.readInt();
            byte[] encodedCert = new byte[certLen];  
            //din.readFully(encodedCert);
            
            java.security.cert.CertificateFactory cf =
            		java.security.cert.CertificateFactory.getInstance("X.509");
			java.security.cert.Certificate cert =  cf.generateCertificate(din);
			PublicKey pub = cert.getPublicKey();
			
            System.out.println("pubKey.toString \n" + pub.toString());
            
            /* create a Signature object and initialize it with the public key */
            Signature sig = Signature.getInstance("SHA1withRSA");
            sig.initVerify(pub);

            /* Update and verify the data */
            sig.update(rawData);

            din.close();


            boolean verifies = sig.verify(sigToVerify);

            System.out.println("signature verifies: " + verifies);

           
            
        } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
            e.printStackTrace();
        } 
    }

}


