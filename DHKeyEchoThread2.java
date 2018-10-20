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
	
	private DataOutputStream getDos() {return dos;}
	
	public void run() {
		if (isSender) {
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alice();
			}
		} else {
			bob();
		}

		EchoThread client = new EchoThread(server, socket);
		client.start();                 // Fork the thread
		server.getClients().add(client);
	}
		
	private void alice(){
		
		for (DHKeyEchoThread2 bob : server.getDHClients()) {
			if (!bob.equals(this)){
				try {
					// TODO Alice encodes her public key, and sends it over to Bob.
						int aPKElen = dis.readInt();
						bob.dos.writeInt(aPKElen);
						byte[] alicePubKeyEnc = new byte[aPKElen];
						dis.readFully(alicePubKeyEnc);
						bob.dos.write(alicePubKeyEnc);
						bob.dos.flush();
						// TODO receive bobPubKeyEnc
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
				        int bPKElen;
				        synchronized (lock) {lock.notify();}
				        // received alicePubKeyEnc
						// created alicePubKey
				        // send bobPubKeyEnc
				        bPKElen = dis.readInt();
						alice.dos.writeInt(bPKElen);
						byte [] bobPubKeyEnc = new byte[bPKElen];
						dis.readFully(bobPubKeyEnc);
						alice.dos.write(bobPubKeyEnc);
						alice.dos.flush();
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
