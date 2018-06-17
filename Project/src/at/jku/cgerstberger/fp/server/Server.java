package at.jku.cgerstberger.fp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

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
		while(!str.equals("QUIT")) {
			System.out.println("Received command: " + str);
			str = in.nextLine();
		}
		
		socketHandler.interrupt();
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

		public ClientHandler(Socket socket) {
			this.socket = socket;
			try {
				this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				this.out = new PrintWriter(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(
					"Client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ") connnected ...");
		}

		@Override
		public void run() {
			System.out.println("ClientHandler - run()");
			String str;
			while (!this.isInterrupted()) {
				try {
					str = in.readLine();
					System.out.println("Client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "): " + str);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println(
					"Client(" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + ") disconnected ...");
		}
	}
}
