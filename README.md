# Secure-File-Server

Originally this project was a Java app that provides users with access to their encrypted files. 
Now I am converting it into a secure instant messaging system. This project will be my senior year self-study project for my BS in Computer Science at University of Houston - Downtown. 
Currently I am configuring SSL/TLS through JSSE.
The TLS connections will use RSA mutual authentication. Every client who logs on will need their own certificate,
which will be signed by the server. A test Certificate Authority will be at the top of the chain for both server and client.
Next I will implement a password database that stores hashes made with PBKDF2 and HMAC-SHA-256 hashing.
If I have time, I might add facial recognition for authentication.
