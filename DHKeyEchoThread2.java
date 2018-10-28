package myClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void run() {
		if (isSender) {
			alice();			
		} else {
			bob();
		}
		//to signal finished to our connected client
		try {
			dos.writeInt(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EchoThread client = new EchoThread(server, socket);
		client.start();                 // Fork the thread
		server.getClients().add(client);

	}
		
	private void alice(){
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (DHKeyEchoThread2 bob : server.getDHClients()) {
			if (!bob.equals(this)){
				try {
					//need to kickstart synchronized handshake
					int hs = dis.readInt();
					bob.dos.writeInt(hs);
					
					//Alice sends her certificate for signature verification
					int certLen = dis.readInt();
					byte[] encodedCert = new byte[certLen];//in this case cert is read in without length
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
						int sigLen = dis.readInt();
						bob.dos.writeInt(sigLen);
						byte[] signature = new byte[sigLen];
						dis.readFully(signature);
						bob.dos.write(signature);
						// Alice received bobPubKeyEnc
						// received int bPKElen
						//received byte[] bobPubKeyEnc 
						// created shared secret
						// send aliceLen to bob
						int aliceLen = dis.readInt();
						bob.dos.writeInt(aliceLen);
						//DEMO PURPOSES TO CONFIRM SAME SHAREDSECRET
						byte[] aliceSharedSecret = new byte[aliceLen];
						dis.readFully(aliceSharedSecret);
						bob.dos.write(aliceSharedSecret);
						bob.dos.flush();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void bob(){
		for (DHKeyEchoThread2 alice : server.getDHClients()) {
			if (!alice.equals(this)){
				try {
				        
				        synchronized (lock) {lock.notify();}
						//need to kickstart synchronized handshake

						//Bob sends his certificate for signature verification
						int certLen = dis.readInt();
						byte[] encodedCert = new byte[certLen];//in this case cert is read in without length
						dis.readFully(encodedCert);
						alice.dos.write(encodedCert);
						alice.dos.flush();
						
				        // received alicePubKeyEnc
						// created alicePubKey

				        // sending bobPubKeyEnc DH parameters
				        int bPKElen;
				        bPKElen = dis.readInt();
						alice.dos.writeInt(bPKElen);
						byte [] bobPubKeyEnc = new byte[bPKElen];
						dis.readFully(bobPubKeyEnc);
						alice.dos.write(bobPubKeyEnc);
						alice.dos.flush();
						int sigLen = dis.readInt();
						alice.dos.writeInt(sigLen);
						byte[] signature = new byte[sigLen];
						dis.readFully(signature);
						alice.dos.write(signature);
						// received aliceLen
						// created bobSharedSecret
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}
        synchronized (lock) {lock.notify();}
	}
	
	
	

}//eoc
