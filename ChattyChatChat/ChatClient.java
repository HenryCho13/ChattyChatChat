package ChattyChatChat;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
	public static void main(String[] args) throws IOException
	{
		String hostName = "localhost";
		int portNumber = 9111;
		
		try
		{
			Socket s = new Socket(hostName, portNumber);
			DataInputStream din = new DataInputStream(s.getInputStream());
			DataOutputStream dout = new DataOutputStream(s.getOutputStream());
			
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			
			String msgIn = "";
			String msgOut = "";
			
			while (!msgIn.equals("/quit"))
			{
				msgOut = stdIn.readLine();
				dout.writeUTF(msgOut);
				msgIn = din.readUTF();
				System.out.println("S: " + msgIn);
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
