# Secure-File-Server

Originally this project was a Java app that provides users with access to their encrypted files. You can see those humble beginnings on branch1.

Now I am converting it into a secure instant messaging system. This project will be my senior year self-study project for my BS in Computer Science at University of Houston - Downtown. 

It is configured for TLS 1.2 via Sun JSSE. The TLS connection uses mutual authentication, meaning every connected client must have a certificate signed by a CA before connecting. This project assumes the Server Certificate acts as the CA that signs each client certificate. A test Certificate Authority will be at the top of the chain for both server and client. Certificates were made using Java keytool and OpenSSL and should be made as RSA keys using the RSA signing algorithm. This program can make its own certs by calling Java keytool -genkeypair from the JRE. However, the next step in the signing process is using keytool -certreq to make a certificate signing request .csr, and I could not get keytool -certreq to execute from the JRE. Not even if I executed it with elevated privileges. 

ClientA and ClientB are hardcoded into the login because KeyManagerFactory cannot be initialized for two separate keys with different passwords. But since a real user would presumably not share their application with other users, it would make sense to have one certificate per client anyway. So in our case we create one keystore per client key entry.

The password database was made with MySQL Workbench. It stores 64 byte hashes using PBKDF2 with HMAC-SHA-256 and 16 byte salts. The fingerprints of each user are also stored to be used in authentication. Once a user logs in, the program calls keytool -list from the JRE to extract the fingerprints of the certificate provided by the client, and sends them to the server to be verified against the fingerprints in the database.

This project uses the Diffie-Hellman key exchange for AES/CBC symmetric encryption. A session key is generated before login to encrypt the password credentials that are sent to the server. A new session key is generated between two corresponding clients before their chat begins. Right now the protocol can only handle a 2-way DH key agreement. The first user to log in sucessfully must choose to wait for his correspondent to log in so that a synchronized DH key exchange can be performed. A third actor would only be able to log in and communicate with a fourth actor, and so on.

Every DH key exchange is protected from a MITM attack by replicating a DHE scheme. The long-term RSA keys that were used in the TLS handshake are used to sign and verify the DH parameters that are sent. Signatures are created for each DH prime number parameter exchanged by using private keys. The two peers, either client-server or client-client, will first exchange encoded copies of their certificates and extract the public keys to be used to verify the other party's signatures. If the signatures are invalid the protocol ends.

Chat messages are encrypted first and hashed second. This project uses HMAC SHA-256 to hash each message using the shared DH key. Only the two corresponding clients with that DH key can recalculate the HMAC and thus can verify the integrity of the message.
