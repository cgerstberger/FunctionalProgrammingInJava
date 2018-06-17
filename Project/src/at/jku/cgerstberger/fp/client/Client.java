package at.jku.cgerstberger.fp.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	private static final String HOST = "localhost";
	private static final int PORT = 12345;
	
	public static void main(String[] args) {
		System.out.println("I am the client");
		Scanner sysIn = new Scanner(System.in);
		
		try {
			Socket socket = new Socket(HOST, PORT);
			PrintWriter serverOut = new PrintWriter(socket.getOutputStream());
			while(true) {
				String str = sysIn.nextLine();
				System.out.println("Writing: " + str);
				serverOut.println(str);
				serverOut.flush();
				
				if(str.equals("QUIT"))
					break;
			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Client disconnected ...");
	}
	
}
