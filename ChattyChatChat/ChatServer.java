package ChattyChatChat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatServer {
	public static void main(String[] args) throws IOException
	{
		System.out.println("S: Server has started");
		
		String hostName = "localhost";
		int portNumber = 9111;
		
		try{
		ServerSocket ss = new ServerSocket(portNumber);
		System.out.println("S: Server is waiting for client request");
		Socket s = ss.accept();
		
		System.out.println("S: Client connected");
		DataInputStream din = new DataInputStream(s.getInputStream());
		DataOutputStream dout = new DataOutputStream(s.getOutputStream());
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		String msgIn = "";
		String msgOut = "";
		
		while (!msgIn.equals("/quit"))
		{
			msgIn = din.readUTF();
			System.out.println("C: " + msgIn);
			msgOut = stdIn.readLine();
			dout.writeUTF(msgOut);
			dout.flush();
		}
		
		din.close();
		stdIn.close();
		dout.close();
		s.close();
		
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
