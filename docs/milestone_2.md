# Milestone 2: Handling Concurrency (Multi-Client Support)

In Milestone 1, our server could only handle a single client at a time. If a second client tried to connect while the first was active, the second client would block until the first client disconnected. 

In this milestone, we will enable our server to handle multiple clients concurrently using Java's multithreading features and a **Thread Pool**.

---

## 💡 Key Concepts

### 1. The Concurrency Problem in Blocking I/O
By default, standard Java Sockets are **blocking**:
- `serverSocket.accept()` blocks until a client connects.
- `inputStream.read()` blocks until the client sends data.

If our server handles everything sequentially in a single thread, it gets stuck in the client communication loop (`while (bytesRead = read(...))`) for the first client, meaning it cannot execute `serverSocket.accept()` to welcome new clients.

### 2. Multi-Threading vs. Thread Pools
To handle multiple clients, we need to offload each connection's communication loop to a separate thread.
- **Thread-per-connection**: Spawning a `new Thread(...)` for every connection works but is dangerous in production. If 10,000 clients connect, the JVM will try to create 10,000 threads, quickly crashing due to memory limits (Out of Memory) and CPU thrashing (excessive context switching).
- **Thread Pools (`ExecutorService`)**: A thread pool maintains a queue of tasks and a managed set of worker threads. Using `Executors.newCachedThreadPool()` allows us to reuse existing idle threads and dynamically scale up/down, avoiding the overhead of creating new threads for every request.

---

## 🛠️ Step-by-Step Implementation

We will refactor `Main.java` to use an `ExecutorService` and move the connection logic to a dedicated helper method `handleClient(Socket clientSocket)`.

### The Code
Copy this code and paste it into your `Main.java` file:

```java
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        int port = 6379;
        
        System.out.println("Starting Java Redis Server on port " + port + "...");
        
        // Create a Cached Thread Pool to handle connections concurrently
        ExecutorService threadPool = Executors.newCachedThreadPool();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Server is listening and ready to accept connections!");
            
            while (true) {
                try {
                    // accept() blocks until a client connects
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted a new connection from: " + clientSocket.getRemoteSocketAddress());
                    
                    // Offload the client connection handling to the Thread Pool
                    threadPool.submit(() -> handleClient(clientSocket));
                    
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } finally {
            // Good practice: shut down the thread pool when the server finishes
            threadPool.shutdown();
        }
    }
    
    /**
     * Handles the interaction loop for a single client connection.
     * Running inside a separate worker thread from the thread pool.
     */
    private static void handleClient(Socket clientSocket) {
        // Try-with-resources here ensures that clientSocket is closed automatically when leaving this block
        try (Socket socket = clientSocket) {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            
            // Read loop for this specific client
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String received = new String(buffer, 0, bytesRead);
                
                // Log the thread name along with the received data to confirm concurrency
                System.out.println("[" + Thread.currentThread().getName() + "] Received: " 
                        + received.replace("\r", "\\r").replace("\n", "\\n"));
                
                // Respond with PONG
                outputStream.write("+PONG\r\n".getBytes());
                outputStream.flush();
            }
            
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Error handling client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
        }
    }
}
```

---

## 🧪 How to Test Concurrency

1. **Recompile and run** your server:
   ```bash
   javac Main.java
   java Main
   ```

2. **Open multiple terminal windows**:
   - In **Terminal A**, connect to the server:
     ```bash
     redis-cli
     ```
   - In **Terminal B**, connect to the server:
     ```bash
     redis-cli
     ```

3. **Verify simultaneous activity**:
   - Send `PING` in Terminal A, and then immediately send `PING` in Terminal B.
   - Look at your Java server log window. You should see logs printed by different threads, like `[pool-1-thread-1]` and `[pool-1-thread-2]`, handling the requests concurrently!
