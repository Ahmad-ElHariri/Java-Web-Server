import java.io.*;
import java.net.*;
import java.util.*;

public class WebServer {
    public static void main(String[] args) throws Exception {

        // 1. Choose a port number for the web server (must not be port 80)
        int port = 6789;

        // 2. Create a listening server socket (door where clients connect)
        ServerSocket listenSocket = new ServerSocket(port);
        System.out.println("Web server running on port " + port);

        // 3. Keep accepting connections forever (one per browser request)
        while (true) {

            // Accept an incoming TCP connection from a client
            Socket connectionSocket = listenSocket.accept();

            // Create a handler object for this request
            HttpRequest request = new HttpRequest(connectionSocket);

            // Run the handler in a new thread (so many clients can connect at once)
            Thread thread = new Thread(request);
            thread.start();
        }
    }
}

class HttpRequest implements Runnable {

    // HTTP lines end with CRLF: "\r\n"
    final String CRLF = "\r\n";

    // The socket dedicated to this specific client
    Socket socket;

    // Constructor receives the client’s socket
    public HttpRequest(Socket socket) {
        this.socket = socket;
    }

    // This runs when the thread starts
    public void run() {
        try {
            processRequest();  // handle the request
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Reads and prints the HTTP request from the browser
    private void processRequest() throws Exception {

        // 1. Get input stream (from client → server)
        //    and output stream (from server → client)
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // 2. Wrap the input stream so we can read text lines easily
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // 3. Read the first line of the HTTP request (the Request Line)
        String requestLine = br.readLine();
        System.out.println();
        System.out.println("Request Line: " + requestLine);

        // Extract the requested filename from the Request Line
        // Example: "GET /index.html HTTP/1.1"
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();                // skip the word "GET"
        String fileName = tokens.nextToken();  // get "/index.html"
        fileName = "." + fileName;         // convert to "./index.html"
        System.out.println("Extracted file name: " + fileName);

        // 4. Read and print all header lines until the empty line
        String headerLine;
        while ((headerLine = br.readLine()) != null && headerLine.length() != 0) {
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
