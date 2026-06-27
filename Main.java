import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Main {
    public static void main(String[] args) {
        int port = 6379;

        System.out.println("Starting Java Redis Server on port " + port  + "...");

        try(ServerSocket serverSocket = new ServerSocket(port)){
        // Set SO_REUSEADDR so we can quickly restart the server without "Address already in use" error 
        serverSocket.setReuseAddress(true);

        // handling one client at a time in a blocking loop
            while(true){
                try (Socket clientSocket =  serverSocket.accept()){
                    System.out.println();
                    System.out.println("Accepted a new connection from: "+ clientSocket.getRemoteSocketAddress());
                    
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();

                    byte[] buffer = new byte[1024];
                    int bytesRead;



                    // Read incoming bytes from the client connection in a loop

                    while((bytesRead = inputStream.read(buffer)) != -1)
                    {
                        String received = new String(buffer, 0, bytesRead);
                        System.out.println("Received raw bytes: " + received.replace("\r", "\\r").replace("\n", "\\n"));


                        // Respond to the client with a RESP Simple String "+PONG\r\n"

                        outputStream.write("+PONG\r\n".getBytes());
                        outputStream.flush();
                    }

                    System.out.println("Client Disconnected: " + clientSocket.getRemoteSocketAddress()); 


                } catch (IOException e) {
                    // TODO: handle exception
                    System.out.println("Error handling client connection" + e.getMessage() );
                }
            }
        }
        catch(IOException e){
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
