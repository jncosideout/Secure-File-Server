package myClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;
/*This class is designed for facilitating a Diffie-Hellman 
 * key exchange between 2 parties. DH with 3 or more parties
 * would require a new protocol and is far more complicated.
 */
public class DHKeyEchoThread2 extends Thread{

	private EchoServer server;
	private SSLSocket socket; // The socket that we'll be talking over
	private DataOutputStream dos;
	private DataInputStream dis;
	private boolean isSender;
	public static Object lock = new Object();
	
	public DHKeyEchoThread2(EchoServer server, SSLSocket socket, boolean isSender) {
		this.server = server;
		this.socket = socket;
		this.isSender = isSender;
		try {
			dos = new DataOutputStream(this.socket.getOutputStream());
			dis = new DataInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void run() {
		if (isSender) {//true if this client is initiating DH 
			alice();			
		} else {
			bob();
		}
		//to signal finished to our connected client
		try {
			dos.writeInt(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		EchoThread client = new EchoThread(server, socket);
		client.start();// Fork the thread, start the chat
		server.getClients().add(client);
		

	}
		
	private void alice(){
		synchronized (lock) {
			try {//wait for "Bob" (second client) to enter 
				//his DHKeyEchoThread2
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (DHKeyEchoThread2 bob : server.getDHClients()) {
			if (!bob.equals(this)){
				try {
	
					//Alice sends her certificate for signature verification
					int certLen = dis.readInt();
					//in this case cert is read in by Bob without length to initialize an array
					byte[] encodedCert = new byte[certLen];
					dis.readFully(encodedCert);
					bob.dos.write(encodedCert);
					bob.dos.flush();
					
					//  Alice encodes her DH public key, and sends it over to Bob.
						int aPKElen = dis.readInt();
						bob.dos.writeInt(aPKElen);
						byte[] alicePubKeyEnc = new byte[aPKElen];
						dis.readFully(alicePubKeyEnc);
						bob.dos.write(alicePubKeyEnc);
						bob.dos.flush();
						//pass on the signature to Bob
						int sigLen = dis.readInt();
						bob.dos.writeInt(sigLen);
						byte[] signature = new byte[sigLen];
						dis.readFully(signature);
						bob.dos.write(signature);
						bob.dos.flush();
						//Alice has:
						// 1) received bobPubKeyEnc
						// 2) received int bPKElen
						// 3) received byte[] bobPubKeyEnc 
						// 4) created shared secret
						//Now send aliceLen to Bob so that
						//he can create the shared secret
						int aliceLen = dis.readInt();
						bob.dos.writeInt(aliceLen);
						//DEMO PURPOSES TO CONFIRM SAME SHAREDSECRET
						//THIS COMPROMISES SECURITY
						byte[] aliceSharedSecret = new byte[aliceLen];
						dis.readFully(aliceSharedSecret);
						bob.dos.write(aliceSharedSecret);
						bob.dos.flush();
					} catch (Exception e) {
						e.printStackTrace();
					} 
					break;
			}
		}//wait for Bob thread to finish
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void bob(){
		for (DHKeyEchoThread2 alice : server.getDHClients()) {
			if (!alice.equals(this)){
				try {
				        //wake up Alice thread
				        synchronized (lock) {lock.notify();}

						//Bob sends his certificate for signature verification
						int certLen = dis.readInt();
						//in this case Alice reads cert without length to initialize an array
						byte[] encodedCert = new byte[certLen];
						dis.readFully(encodedCert);
						alice.dos.write(encodedCert);
						alice.dos.flush();
						
				        //Bob has:
						// 1) received alicePubKeyEnc
						// 2) created alicePubKey
				        //Now sending bobPubKeyEnc DH parameters
				        int bPKElen;
				        bPKElen = dis.readInt();
						alice.dos.writeInt(bPKElen);
						byte [] bobPubKeyEnc = new byte[bPKElen];
						dis.readFully(bobPubKeyEnc);
						alice.dos.write(bobPubKeyEnc);
						alice.dos.flush();
						//Send signature for Alice to verify
						int sigLen = dis.readInt();
						alice.dos.writeInt(sigLen);
						byte[] signature = new byte[sigLen];
						dis.readFully(signature);
						alice.dos.write(signature);
						alice.dos.flush();
						// received aliceLen
						// created bobSharedSecret
						//Now to stay synchronized with client DHKeyBob,
						//read his signal of "end of process"
						dis.readInt();
					}  catch (Exception e) {
						e.printStackTrace();
					} 
					break;
			}
		}//wake up Alice thread
        synchronized (lock) {lock.notify();}
	}
	
	
	

}//eoc
