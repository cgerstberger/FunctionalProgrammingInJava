package at.jku.cgerstberger.fp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Server extends Thread {

	private Scanner in;
	private final int PORT = 12345;

	private List<ClientHandler> connectedClients = new LinkedList<ClientHandler>();

	public Server() {
		System.out.println("Server instance is created.");
		in = new Scanner(System.in);
	}

	public void run() {
		SocketHandler socketHandler = new SocketHandler();
		socketHandler.start();

		String str = in.nextLine();
		while(!str.toLowerCase().equals("quit")) {
			System.out.println("Received command: " + str);
			str = in.nextLine();
			
			switch(str) {
			case "CountWords":
				countWords();
			}
		}
		
		socketHandler.interrupt();
	}
	
	private void countWords() {
		
	}

	private class SocketHandler extends Thread {
		
		@Override
		public void run() {
			ServerSocket server = null;
			try {
				server = new ServerSocket(PORT);
				System.out.println("Listening on port " + PORT + " ...");
				while (true) {
					Socket socket = server.accept();

					// client connected
					ClientHandler handler = new ClientHandler(socket);
					handler.start();
					connectedClients.add(handler);
				}
			} catch (IOException exc) {
				exc.printStackTrace();
			} finally {
				connectedClients.stream().forEach(c -> c.interrupt());
				
				if (server != null && !server.isClosed()) {
					try {
						server.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("Listening-Server stopped ...");
			}
		}
		
		@Override
		public void interrupt() {
			
			super.interrupt();
		}
	}

	private class ClientHandler extends Thread {

		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		
		private String hostAddress;
		private int port;
		
		private List<String> messages = new ArrayList<>();

		public ClientHandler(Socket socket) {
			this.socket = socket;
			try {
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out = new PrintWriter(socket.getOutputStream());
				hostAddress = socket.getInetAddress().getHostAddress();
				port = socket.getPort();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Client(" + hostAddress + ":" + port + ") connnected ...");
		}

		@Override
		public void run() {
			System.out.println("ClientHandler - run()");
			String str;
			while (!this.isInterrupted()) {
				try {
					str = in.readLine();
					messages.add(str);
					System.out.println("Client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "): " + str);
					if(str.toLowerCase().equals("quit"))
						this.interrupt();
					
//					System.out.println("Client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "): " + str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				in.close();
				out.flush();
				out.close();
				socket.close();
				
				in = null;
				out = null;
				socket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Client(" + hostAddress + ":" + port + ") connnected ...");
		}
		
		public List<String> getMessages(){
			return new ArrayList<>(messages);
		}
	}
}
