# Java Web Server — CMPS 242 (Computer Networks)

## Overview

This project implements a **simple multi-threaded HTTP web server** in Java.  
The server accepts incoming TCP connections, parses HTTP GET requests, prints the
request line & headers, and returns the requested file with correct HTTP response
formatting.

It supports:

- Multiple simultaneous clients (via multithreading)
- HTTP GET requests
- Returning `200 OK` responses with file contents
- Returning a custom `404 Not Found` page when files do not exist
- Serving HTML, CSS, JavaScript, images (PNG/JPG/GIF), JSON, and other file types
- Safe connection handling (null-safety when parsing request/headers)

This project follows the structure and requirements provided in the CMPS 242 Networks
Project PDF.

---

## How It Works

### 1. Accept Connections

The server:

- Listens on port `6789`
- Accepts incoming TCP connections
- Creates a new thread for each client (so multiple clients can connect at once)

### 2. Parse HTTP Request

The server reads:

- The **Request Line**
- All HTTP **Header Fields**

It extracts the requested filename from the GET request.

### 3. Build Response

The server attempts to open the requested file:

- If it exists → returns `HTTP/1.1 200 OK`
- If it does not exist → returns `HTTP/1.1 404 Not Found` + HTML error page

The correct `Content-Type` is determined based on file extension:

- `.html` → `text/html`
- `.css` → `text/css`
- `.js` → `text/javascript`
- `.png` → `image/png`
- `.jpg` / `.jpeg` → `image/jpeg`
- `.gif` → `image/gif`
- `.json` → `application/json`
- Default → `application/octet-stream`

### 4. Send Response to Browser

- Server sends Status Line, Content-Type line, blank line
- Streams the file in 1KB chunks (using `sendBytes()`)
- Closes the socket afterward

---

## How to Compile

From the project folder, run:

```bash
javac WebServer.java
```
