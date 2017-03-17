package ChattyChatChat;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class ChattyChatChatServer {

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
      System.out.println("Usage: java ChattyChatChatServerSync <portNumber>\n"
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

  private String clientName = "anonymous";
  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket clientSocket = null;
//  private final clientThread[] threads;
//  private int maxClientsCount;
  private ArrayList<clientThread>threads;

	public clientThread(Socket clientSocket, ArrayList<clientThread>threads) {
	this.clientSocket = clientSocket;
	this.threads = threads;
	}

  @SuppressWarnings("deprecation")
public void run() {
	 ArrayList<clientThread>threads = this.threads;
	
    try {
      /*
       * Create input and output streams for this client.
       */
    System.out.println("Testing Runnable");
    os = new PrintStream(clientSocket.getOutputStream());
    is = new DataInputStream(clientSocket.getInputStream());
      String name = "anonymous";
//      while (true) {
//        os.println("Enter your name.");
//        name = is.readLine().trim();
//        if (name.indexOf('@') == -1) {
//          break;
//        } else {
//          os.println("The name should not contain '@' character.");
//        }
//      }

      /* Welcome the new the client. */
      os.println("Welcome to our chat room.\nTo set your nickname enter /nick yourname.\n"
      		+ "To send direct message, enter /dm name msg.\n"
      		+ "To leave enter /quit in a new line.");
      
      
      /* Start the conversation. */
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        if (line.startsWith("/nick")){
        	String[] words = line.split("\\s", 2);
        	if (words.length > 1 && words[1] != null) {
                name = words[1].trim();
        	}
        	synchronized (this) {
          	  for (int i = 0; i < threads.size(); i++) {
          		  if (threads.get(i) != null && threads.get(i) == this) {
                      clientName = name;
                      break;
          		  }
          	  }
//          	  for (int i = 0; i < threads.size(); i++) {
//          		  if (threads.get(i) != null && threads.get(i) != this) {
//                      threads.get(i).os.println("*** A new user " + name
//                      + " entered the chat room !!! ***");
//          		  }
//          	  }
            }
        }
        /* If the message is private sent it to the given client. */
        else if (line.startsWith("/dm")) {
          String[] words = line.split("\\s", 100);
          if (words.length > 2 && words[2] != null) {
            for (int i = 3; i < words.length; i++)
            {
            	words[2] = words[2] + " " + words[i];
            }
            words[2] = words[2].trim();
            if (!words[2].isEmpty()) {
              synchronized (this) {
            	  for (int i = 0; i < threads.size(); i++) {
            		  if (threads.get(i) != null && threads.get(i) != this 
            				  && threads.get(i).clientName != null 
            				  && threads.get(i).clientName.equals(words[1])) {
            			  threads.get(i).os.println(name + ": " + words[2]);
                        /*
                        * Echo this message to let the client know the private
                        * message was sent.
                        */
                       this.os.println(name + ": " + words[2]);
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
            		threads.get(i).os.println(name + ": " + line);
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