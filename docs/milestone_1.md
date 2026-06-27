# Milestone 1: The Basic TCP Socket Server (Single Client)

In this milestone, we will build a basic TCP server that listens on port `6379` (the default Redis port). It will accept a connection from a client, read any command sent to it, and respond with a simple RESP (Redis Serialization Protocol) string: `+PONG\r\n`.

---

## 💡 Key Concepts

### 1. TCP Sockets
TCP (Transmission Control Protocol) is a connection-oriented protocol that ensures reliable, ordered delivery of a stream of bytes between two endpoints. In Java:
- `ServerSocket`: Listens for incoming connection requests on a specific port. When a connection request arrives, it accepts it and returns a `Socket` object representing the connection.
- `Socket`: Represents the bidirectional communication channel between the client and the server. It provides an `InputStream` to read data from the client and an `OutputStream` to write data to the client.

### 2. The Redis Port
Redis by default listens on port `6379`. We will bind our server to this port so we can test it using the official `redis-cli` tool.

### 3. RESP (Redis Serialization Protocol) Simple String
Redis communicates using a protocol called RESP. The simplest response type is a **Simple String**, which starts with `+` and ends with a carriage return and newline (`\r\n`). 
For example, the string `PONG` is serialized in RESP as:
```
+PONG\r\n
```

---

## 🛠️ Step-by-Step Implementation

### Step 1: Rename your file (Action Needed)
Please rename your file `main.java` in the root directory to `Main.java` (capital **M**). In Java, public class names must match their filenames exactly, and standard convention is to start class names with a capital letter.

### Step 2: The Code
Below is the complete, self-contained implementation for `Main.java`. Copy this code into your renamed `Main.java` file.

```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        int port = 6379;
        
        System.out.println("Starting Java Redis Server on port " + port + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Set SO_REUSEADDR so we can quickly restart the server without "Address already in use" errors
            serverSocket.setReuseAddress(true);
            System.out.println("Server is listening and ready to accept connections!");
            
            // For Milestone 1, we handle one client at a time in a blocking loop
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Accepted a new connection from: " + clientSocket.getRemoteSocketAddress());
                    
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();
                    
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    
                    // Read incoming bytes from the client connection in a loop
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        String received = new String(buffer, 0, bytesRead);
                        System.out.println("Received raw bytes: " + received.replace("\r", "\\r").replace("\n", "\\n"));
                        
                        // Respond to the client with a RESP Simple String "+PONG\r\n"
                        outputStream.write("+PONG\r\n".getBytes());
                        outputStream.flush();
                    }
                    
                    System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
```

---

## 🧪 How to Run and Test

1. **Compile the program**:
   Open a terminal and compile the class:
   ```bash
   javac Main.java
   ```

2. **Run the program**:
   ```bash
   java Main
   ```

3. **Test with `redis-cli`**:
   Open another terminal and run the Redis client command-line interface:
   ```bash
   redis-cli ping
   ```
   You should see:
   ```
   "PONG"
   ```
   Alternatively, you can test using Netcat (`nc`) or Telnet:
   ```bash
   nc localhost 6379
   ```
   Type `PING` and press enter; you should receive:
   ```
   +PONG\r\n
   ```
