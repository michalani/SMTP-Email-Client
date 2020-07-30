/*************************************
 * Filename:  HttpInteract.java
 * Date: 17/10/2019
 *************************************/

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Class for downloading one object from http server.
 *
 */
public class HttpInteract {
	private String host;
	private String path;
	private String requestMessage;

	private static final int HTTP_PORT = 80;
	private static final String CRLF = "\r\n";
	private static final int BUF_SIZE = 4096;
	private static final int MAX_OBJECT_SIZE = 102400;

 	/* Create HttpInteract object. */
	public HttpInteract(String url){

		/* Split "URL" into "host name" and "path name", and
		 * set host and path class variables.
		 * if the URL is only a host name, use "/" as path
		 */
		if(!url.contains("/")){
 			path = "/";
 			host = url;
 		}
 	  else{
 			String[] urlSplitArray = url.split("/", 2);
 			host = urlSplitArray[0];
 			path = "/" + (urlSplitArray[1]);
 		}

		/* Construct requestMessage, add a header line so that
		 * server closes connection after one response. */
		requestMessage = ("GET " + path + " HTTP/1.1" + CRLF + "Host: " + host + CRLF + CRLF);

		return;
	}


	/* Send Http request, parse response and return requested object
	 * as a String (if no errors),
	 * otherwise return meaningful error message.
	 * Don't catch Exceptions. EmailClient will handle them. */
	public String send() throws IOException {

		/* buffer to read object in 4kB chunks */
		char[] buf = new char[BUF_SIZE];

		/* Maximum size of object is 100kB, which should be enough for most objects.
		 * Change constant if you need more. */
		char[] body = new char[MAX_OBJECT_SIZE];

		String statusLine="";	// status line
		int status;		// status code
		String headers="";	// headers
		int bodyLength=-1;	// lenght of body

		/* The socket to the server */
		Socket connection;

		/* Streams for reading from and writing to socket */
		BufferedReader fromServer;
		DataOutputStream toServer;
		System.out.println("Connecting server: " + host + CRLF);

		/* Connect to http server on port 80.
		 * Assign input and output streams to connection. */
		connection = new Socket(host, HTTP_PORT);
		fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		toServer = new DataOutputStream(connection.getOutputStream());

		System.out.println("Send request:\n" + requestMessage);


		/* Send requestMessage to http server */
		toServer.writeBytes(requestMessage);

		/* Read the status line from response message */
		statusLine = fromServer.readLine();
		System.out.println("Status Line:\n" + statusLine + CRLF);

		/* Extract status code from status line. If status code is not 200,
		 * close connection and return an error message.
		 * Do NOT throw an exception */
		status = Integer.parseInt(statusLine.substring(9, 12));
		if (status != 200) {
			System.out.println("Error, status code: " + status + ". Closing connection...");
			connection.close();
		}

		/* Read header lines from response message, convert to a string,
 		 * and assign to "headers" variable.
		 * Recall that an empty line indicates end of headers.
		 * Extract length  from "Content-Length:" (or "Content-length:")
		 * header line, if present, and assign to "bodyLength" variable.
		*/

		String contentString = "";
		String readingHdrs = "";

		while((readingHdrs = fromServer.readLine()) != null){
			if(readingHdrs.trim().isEmpty()) {
				break;
			}

			else {
				headers = headers + readingHdrs + CRLF;

				if (readingHdrs.startsWith("Content-Length:") || (readingHdrs.startsWith("Content-length:"))) {
					contentString = readingHdrs;
				}
			}
		}

		if (contentString != "") {
			bodyLength = Integer.parseInt(contentString.replaceAll("\\D+",""));
			System.out.println("Headers:\n"+headers+CRLF);
		}


		/* If object is larger than MAX_OBJECT_SIZE, close the connection and
		 * return meaningful message. */
		if (bodyLength > MAX_OBJECT_SIZE) {
			connection.close();
			return("Connection closed, length of message: " +bodyLength);
		}

		/* Read the body in chunks of BUF_SIZE using buf[] and copy the chunk
		 * into body[]. Stop when either we have
		 * read Content-Length bytes or when the connection is
		 * closed (when there is no Content-Length in the response).
		 * Use one of the read() methods of BufferedReader here, NOT readLine().
		 * Make sure not to read more than MAX_OBJECT_SIZE characters.
		 */
		int bytesRead = 0;
		int bodyIndex = 0;

		while (bytesRead < MAX_OBJECT_SIZE) {
			if (bytesRead >= bodyLength) {
				break;
			}
			else {
				bytesRead = fromServer.read(buf, 0, BUF_SIZE);
				for (int j = 0; j < BUF_SIZE; j++) {
					body[bodyIndex] = buf[j];
					bodyIndex++;
				}
			}

		}

		if (bytesRead > 0) {
			bytesRead = bytesRead - 1;
		}

		/* At this points body[] should hold to body of the downloaded object and
		 * bytesRead should hold the number of bytes read from the BufferedReader
		 */

		/* Close connection and return object as String. */
		System.out.println("Done reading file. Closing connection.");
		connection.close();
		System.out.println("Connection successfully closed.");
		return(new String(body, 0, bytesRead));
	}
}
