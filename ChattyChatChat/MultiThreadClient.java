package ChattyChatChat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiThreadClient implements Runnable {
	
	private static Socket s = null;
	private static DataInputStream din = null;
	private static PrintStream dout = null;
	
	private static BufferedReader stdIn = null;
	private static boolean closed = false;
	
	public static void main(String[] args) throws IOException {
		String hostName = "localhost";
		int portNumber = 9004;
		
	     System.out
         .println("Now using host=" + hostName + ", portNumber=" + portNumber);
	     
		try {
			//Open socket, input, and output streams
			s = new Socket(hostName, portNumber);
			din = new DataInputStream(s.getInputStream());
			dout = new PrintStream(s.getOutputStream());
			stdIn = new BufferedReader(new InputStreamReader(System.in));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName); System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName); 
			System.exit(1);
		}
		
		//Write data to socket
		if (s != null && din != null && dout != null) {
			try {
				
				System.out.println("test");
				
				new Thread(new MultiThreadClient()).start();
				while (!closed) {
					dout.println(stdIn.readLine().trim());
				}
				
				//Close socket, input, and output streams
				din.close();
				stdIn.close();
				dout.close();
				s.close();	
				
			} catch (IOException e) {
		        System.err.println("IOException:  " + e);
		    }
		}
	}
	
	//Thread that reads from server
	public void run() {
		String response = "";
		try { 
			while (!response.equals("/quit")) {
				response = stdIn.readLine().trim();
				//dout.write(msgOut);
				//dout.writeUTF(msgOut);
				//msgIn = din.readUTF();
				System.out.println("S: " + response);
			}
			closed = true;
		} catch (IOException e) {
			System.err.println("IOException:  " + e);
		}
	}
}
