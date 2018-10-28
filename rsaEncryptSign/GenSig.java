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

public class GenSig {

	private Signature dsa;
	
    public GenSig(PrivateKey priv) {
    	try {
            /* Create a Signature object and initialize it with the private key */
            dsa = Signature.getInstance("SHA256withRSA"); //used to be sha1withrsa
            dsa.initSign(priv);
            } catch (Exception e) {
            System.err.println("Caught exception " + e.toString());
            e.printStackTrace();
        }

    }

public void sendSignature(DataOutputStream dos, byte[] dataToSign) throws IOException{
	/* Generate a DSA signature */
	byte[] realSig = null;
    try { /* Update and sign the data */
		dsa.update(dataToSign, 0, dataToSign.length);
	    realSig = dsa.sign();
	} catch (SignatureException e) {
		e.printStackTrace();
	}
    /* Now that all the data to be signed has been read in, 
            generate a signature for it */
    int sigLen = realSig.length;
    dos.writeInt(sigLen);
    /* Send the signature to peer */
    dos.write(realSig);
    dos.flush();
}

}//eoc


