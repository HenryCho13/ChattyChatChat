package ChattyChatChat;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class MultiThreadServer {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;

  //The chat server can accept multiple client connections 
  private static ArrayList<clientThread>threads = new ArrayList<clientThread>(10);

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 9111;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber. Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    
    int i = 0;
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        System.out.println("client socket accepted");
       
        threads.add(new clientThread(clientSocket, threads));
        threads.get(i).start();
        i++;
        System.out.println("passed client socket to thread");
//		break;
      
        }
       catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

  private String clientName = null;
  private BufferedReader is = null;
  private PrintWriter os = null;
  private Socket clientSocket = null;
//  private final clientThread[] threads;
//  private int maxClientsCount;
  private ArrayList<clientThread>threads;

	public clientThread(Socket clientSocket, ArrayList<clientThread>threads) {
	this.clientSocket = clientSocket;
	this.threads = threads;
	}

  public void run() {
	 ArrayList<clientThread>threads = this.threads;
	
    try {
      /*
       * Create input and output streams for this client.
       */
    System.out.println("Testing Runnable");
      is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintWriter(clientSocket.getOutputStream());
      String name;
      while (true) {
        os.println("Enter your name.");
        name = is.readLine().trim();
        if (name.indexOf('@') == -1) {
          break;
        } else {
          os.println("The name should not contain '@' character.");
        }
      }

      /* Welcome the new the client. */
      os.println("Welcome " + name
          + " to our chat room.\nTo leave enter /quit in a new line.");
      
      synchronized (this) {
    	  for (int i = 0; i < threads.size(); i++) {
    		  if (threads.get(i) != null && threads.get(i) == this) {
                clientName = "@" + name;
                break;
    		  }
    	  }
    	  for (int i = 0; i < threads.size(); i++) {
    		  if (threads.get(i) != null && threads.get(i) != this) {
                threads.get(i).os.println("*** A new user " + name
                + " entered the chat room !!! ***");
    		  }
    	  }
      }
      /* Start the conversation. */
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        /* If the message is private sent it to the given client. */
        if (line.startsWith("@")) {
          String[] words = line.split("\\s", 2);
          if (words.length > 1 && words[1] != null) {
            words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
              synchronized (this) {
            	  for (int i = 0; i < threads.size(); i++) {
            		  if (threads.get(i) != null && threads.get(i) != this 
            				  && threads.get(i).clientName != null 
            				  && threads.get(i).clientName.equals(words[0])) {
            			  threads.get(i).os.println("<" + name + "> " + words[1]);
                        /*
                        * Echo this message to let the client know the private
                        * message was sent.
                        */
                       this.os.println(">" + name + "> " + words[1]);
                       break;
            		  }
            	  }
              }
            }
          }
        } else {
          /* The message is public, broadcast it to all other clients. */
          synchronized (this) {
            for (int i = 0; i < threads.size(); i++) {
            	if (threads.get(i) != null && threads.get(i).clientName != null) {
            		threads.get(i).os.println("<" + name + "> " + line);
            	}
            }
          }
        }
      }
      synchronized (this) {
    	  for (int i = 0; i < threads.size(); i++) {
            if (threads.get(i) != null && threads.get(i) != this
            && threads.get(i).clientName != null) {
          threads.get(i).os.println("*** The user " + name
              + " is leaving the chat room !!! ***");
        	}
    	  }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
      synchronized (this) {
    	  for (int i = 0; i < threads.size(); i++) {
            if (threads.get(i) == this) {
            	threads.set(i, null);
            }
    	  }
      }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
      is.close();
      os.close();
      clientSocket.close();
    } catch (IOException e) {
    }
  }
}