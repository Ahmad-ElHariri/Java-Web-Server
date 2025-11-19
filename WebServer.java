import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {
    public static void main(String[] args) throws Exception {
    
    // 1. Set the port number
    int port = 6789;

    // 2. Setup the listen socket
    ServerSocket listenSocket = new ServerSocket(port);
    System.out.println("Web server running on port " + port);

    // 3. Process HTTP requests by means of an infinite loop
    while (true) {
        // Wait to be contacted.
        Socket connectionSocket = listenSocket.accept();

        // Construct an object to process the HTTP request message
        HttpRequest request = new HttpRequest(connectionSocket);

        // Create a new thread attached to the request
        Thread thread = new Thread(request);

        // Start the execution of the thread
        thread.start();
    }

    }
}

class HttpRequest implements Runnable {
    final String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Thread entry point
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
    
    // 1. Get a reference to the socket’s input and output streams
    InputStream is = socket.getInputStream();
    DataOutputStream os = new DataOutputStream(socket.getOutputStream());

    // 2. Set up input stream filters
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    // 3. Get the request line of the HTTP request message
    String requestLine = br.readLine();

    // 4. Display the request line
    System.out.println();
    System.out.println("Request Line: " + requestLine);

    // 5. Get and display the header lines
    String headerLine = null;
    while ((headerLine = br.readLine()) != null && headerLine.length() != 0) {
        System.out.println("Header: " + headerLine);
    }

    // 6. Close streams and socket
    os.close();
    br.close();
    socket.close();
    }
}
