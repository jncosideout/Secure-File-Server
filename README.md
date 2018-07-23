# Secure-File-Server
Originally this project was a Java app that provides users with access to their encrypted files.
Now I am converting it into a secure instant messaging system.
Currently I am implementing SSL/TLS through JSSE.
The TLS connections will use RSA mutual authentication. Every client who logs on will need their own certificate,
which will be signed by the server. A test Certificate Authority will be at the top of the chain for both server and client.
Next I will implement a password database that stores hashes made with PBKDF2 and HMAC-SHA-256 hashing.
If I have time, maybe add facial recognition for authentication.
