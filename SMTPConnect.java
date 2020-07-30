/*************************************
 * Filename:  SMTPConnect.java
 * Date: 17/10/2019
 *************************************/

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Open an SMTP connection to mailserver and send one mail.
 *
 */
public class SMTPConnect {
    /* Socket to the server */
    private Socket connection;

    /* Streams for reading from and writing to socket */
    private BufferedReader fromServer;
    private DataOutputStream toServer;

    private static final String CRLF = "\r\n";
	  String outputString = "";

    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnect object. Create the socket and the
       associated streams. Initialise SMTP connection. */

    public SMTPConnect(EmailMessage mailmessage) throws IOException {
        // Open a TCP client socket with hostname and portnumber specified in
        // mailmessage.DestHost and  mailmessage.DestHostPort, respectively.

	      //connection = new Socket("student.csc.liv.ac.uk",1025);
	      connection = new Socket(mailmessage.DestHost,mailmessage.DestHostPort);

        // attach the BufferedReader fromServer to read from the socket and
        // the DataOutputStream toServer to write to the socket
	      fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	      toServer  = new DataOutputStream(connection.getOutputStream());

	      String response = fromServer.readLine();
        System.out.println(response);
        if (!response.startsWith("220")) {
          throw new IOException("220 reply not received from server.");
        }

	      /* SMTP handshake. We need the name of the local machine.
	       Send the appropriate SMTP handshake command. */
	      String localhost = InetAddress.getLocalHost().getHostName();
	      sendCommand("HELO "+localhost+CRLF,250);

	      isConnected = true;
    }

    /* Send message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(EmailMessage mailmessage) throws IOException {

       sendCommand("MAIL FROM: "+mailmessage.Sender+CRLF,250);
       if (mailmessage.Recipient != "") {
         sendCommand("RCPT TO: "+mailmessage.Recipient+CRLF,250);
       }
       if (mailmessage.Cc != "") {
         sendCommand("RCPT TO: "+mailmessage.Cc+CRLF,250);
       }
  		 sendCommand("DATA"+CRLF,354);
  		 sendCommand(mailmessage.Headers+CRLF+" "+CRLF+mailmessage.Body+CRLF+"."+CRLF,250);

    }

    /* Close SMTP connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
	      isConnected = false;
	      try {

	         sendCommand("QUIT"+CRLF,221);
	         connection.close();

	      } catch (IOException e) {

	         System.out.println("Unable to close connection: " + e);
	         isConnected = true;

	      }
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {

       /* Write command to server and read reply from server. */
	     toServer.writeBytes(command);
	     String response1 = fromServer.readLine();
       System.out.println(response1);
	     String stringRc = Integer.toString(rc);

       if (!response1.startsWith(stringRc)) {
          throw new IOException(stringRc + " reply not received from server.");
       }

    }

    /* Destructor. Closes the connection if something bad happens. */
    protected void finalize() throws Throwable {
	      if(isConnected) {
           close();
	      }
	      super.finalize();
    }
}
