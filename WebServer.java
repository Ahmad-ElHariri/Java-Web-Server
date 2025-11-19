import java.io.*;
import java.net.*;
import java.util.*;

/**
 * CMPS 242 - Computer Networks
 * Simple Multithreaded Web Server
 *
 * This server handles HTTP GET requests over a TCP connection.
 * It supports serving HTML, CSS, JavaScript, and image files,
 * returning appropriate MIME types, and returns a 404 page
 * when a requested file does not exist.
 *
 * This implementation follows the project’s two-phase design:
 *
 * STEP 1: Accept connections, read request line + headers, print them.
 * STEP 2: Generate a correct HTTP response and send requested file.
 *
 * Author: Sandra Dergham & Ahmad El Hariri
 * Semester: Fall 2025/26
 */
public class WebServer {
    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------
        // 1. Select a port number for the server (must be > 1024)
        // ------------------------------------------------------------
        int port = 6789;

        // Create a server socket that listens for HTTP requests
        ServerSocket listenSocket = new ServerSocket(port);
        System.out.println("Web server running on port " + port);

        // ------------------------------------------------------------
        // 2. Continuously accept and process client connections.
        // Each incoming connection is assigned its own thread.
        // ------------------------------------------------------------
        while (true) {

            // Accept a TCP connection from a browser (client)
            Socket connectionSocket = listenSocket.accept();

            // Create a runnable HttpRequest handler for this client
            HttpRequest request = new HttpRequest(connectionSocket);

            // Create a new thread to serve the request
            Thread thread = new Thread(request);

            // Start thread execution (multithreading)
            thread.start();
        }
    }
}

/**
 * This class processes a single HTTP request from a client.
 * It implements Runnable so that each request runs in a separate thread.
 */
class HttpRequest implements Runnable {

    // CRLF indicates end-of-line in HTTP headers
    final String CRLF = "\r\n";

    // Socket dedicated to communicating with one specific browser
    Socket socket;

    // Constructor receives the connected client socket
    public HttpRequest(Socket socket) {
        this.socket = socket;
    }

    /**
     * Entry point for the request-handling thread.
     * Calls processRequest() and catches exceptions.
     */
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * STEP 1:
     * - Read request line
     * - Read and print headers
     *
     * STEP 2:
     * - Extract file name from the GET request
     * - Attempt to open file
     * - Build HTTP response message
     * - Send file or 404 page
     * - Close the connection
     */
    private void processRequest() throws Exception {

        // ------------------------------------------------------------
        // 1. Get input and output streams from the socket
        // ------------------------------------------------------------
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // ------------------------------------------------------------
        // 2. Read the Request Line (e.g., "GET /index.html HTTP/1.1")
        // ------------------------------------------------------------
        String requestLine = br.readLine();

        // If browser opens a speculative TCP connection with no data:
        if (requestLine == null) {
            os.close();
            br.close();
            socket.close();
            return;
        }

        System.out.println();
        System.out.println("Request Line: " + requestLine);

        // ------------------------------------------------------------
        // 3. Extract requested file name from Request Line
        // ------------------------------------------------------------
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // Skip the "GET"
        String fileName = tokens.nextToken(); // e.g., "/index.html"
        fileName = "." + fileName; // Serve from current directory
        System.out.println("Extracted file name: " + fileName);

        // ------------------------------------------------------------
        // 4. Read and print all HTTP headers until blank line
        // ------------------------------------------------------------
        String headerLine;
        while (true) {
            headerLine = br.readLine();

            if (headerLine == null) {
                // Client closed connection unexpectedly
                os.close();
                br.close();
                socket.close();
                return;
            }

            if (headerLine.length() == 0) {
                // End of headers
                break;
            }

            System.out.println("Header: " + headerLine);
        }

        // ------------------------------------------------------------
        // 5. STEP 2: Attempt to open requested file
        // ------------------------------------------------------------
        FileInputStream fis = null;
        boolean fileExists = true;

        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        // ------------------------------------------------------------
        // 6. Build status line and content-type line
        // ------------------------------------------------------------
        String statusLine;
        String contentTypeLine;
        String entityBody = "";

        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF;
            contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;
        } else {
            statusLine = "HTTP/1.1 404 Not Found" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML><HEAD><TITLE>Not Found</TITLE></HEAD>"
                    + "<BODY>404 File Not Found</BODY></HTML>";
        }

        // ------------------------------------------------------------
        // 7. Send the HTTP response headers
        // ------------------------------------------------------------
        os.writeBytes(statusLine);
        os.writeBytes(contentTypeLine);
        os.writeBytes(CRLF); // End of headers

        // ------------------------------------------------------------
        // 8. Send file or 404 error body
        // ------------------------------------------------------------
        if (fileExists) {
            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody);
        }

        // ------------------------------------------------------------
        // 9. Close streams and socket
        // ------------------------------------------------------------
        os.close();
        br.close();
        socket.close();
    }

    /**
     * Sends a file in 1KB chunks through the output stream.
     * This avoids loading the entire file into memory.
     */
    private static void sendBytes(FileInputStream fis, OutputStream os)
            throws Exception {

        byte[] buffer = new byte[1024];
        int bytes;

        // Read from file and write to socket in chunks
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    /**
     * Determines the MIME type based on the file extension.
     * This ensures images, CSS, JS, and HTML display correctly in the browser.
     */
    private static String contentType(String fileName) {
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".htm") || lower.endsWith(".html"))
            return "text/html";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return "image/jpeg";
        if (lower.endsWith(".gif"))
            return "image/gif";
        if (lower.endsWith(".png"))
            return "image/png";
        if (lower.endsWith(".css"))
            return "text/css";
        if (lower.endsWith(".js"))
            return "text/javascript";
        if (lower.endsWith(".json"))
            return "application/json";

        // Default MIME type
        return "application/octet-stream";
    }
}
