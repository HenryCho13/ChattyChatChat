package ChattyChatChat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MultiThreadServer {

	private static ServerSocket ss = null;
	private static Socket cs = null;
	private static ArrayList <clientThread> threads = new ArrayList<clientThread>();

	public static void main(String[] args) throws IOException {
		System.out.println("S: Server has started");
		
		String hostName = "localhost";
		int portNumber = 9004;
		
		try{
		ServerSocket ss = new ServerSocket(portNumber);
		
		while (true) {
			cs = ss.accept();
			for (int i=0; i < threads.size(); i++) {
				if (threads.get(i) == null) {
					threads.set(i, new clientThread(cs, threads)).start();
					break;
				}
			}
		}
		
		}
		catch (UnknownHostException e) 
		{
			System.err.println("Don't know about host " + hostName); System.exit(1);
		} 
		catch (IOException e) 
		{
			System.err.println("Couldn't get I/O for the connection to " + hostName); 
			System.exit(1);
		}
	}
}

class clientThread extends Thread {
	private DataInputStream din = null;
	private PrintStream dout = null;
	private Socket clientSocket = null;
	private ArrayList <clientThread> threads;
	
	  public clientThread(Socket clientSocket, ArrayList<clientThread>threads) {
		    this.clientSocket = clientSocket;
		    this.threads = new ArrayList<clientThread>(threads);
	  }
	  
	  public void run() {
		  ArrayList <clientThread> threads = this.threads;
		  try {
			  din = new DataInputStream(clientSocket.getInputStream());
			  dout = new PrintStream(clientSocket.getOutputStream());
			  dout.println("Set nickname by typing /nick <name>\n"
			  		+ "Nicknames must be single words and can't contain spaces.");
			  String name = din.readUTF().trim();
			  dout.println("Hello" + name + ".\nTo leave chat enter /quit");
			  
			  while (true) {
				  String line = din.readUTF().trim();
				  if (line == "/quit") {
					  break;
				  }
				  for (int i=0; i < threads.size(); i++) {
					  if (threads.get(i) != null) {
						  threads.get(i).dout.println(name + ":" + line);
					  }
				  }
			  }
			
			  din.close();
			  dout.close();
			  clientSocket.close();
			  
		  } catch (IOException e) {
		  }
	  }	
}
