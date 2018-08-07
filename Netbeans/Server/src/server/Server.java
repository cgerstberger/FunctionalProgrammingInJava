/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 *
 * @author Christoph
 */
public class Server extends Thread {

    private final Scanner in;
    private final int PORT = 12345;

    private final List<ClientHandler> connectedClients = new LinkedList<ClientHandler>();

    public Server() {
        System.out.println("Server instance is created.");
        in = new Scanner(System.in);
        
        ClientHandler cOne = new ClientHandler(null, "10.0.0.3");
        cOne.addMessage("This is my first sentence!");
        cOne.addMessage("Respond");
        cOne.addMessage("a a a");
        cOne.addMessage("idiot");
        connectedClients.add(cOne);
        ClientHandler cTwo = new ClientHandler(null, "10.0.0.4");
        cTwo.addMessage("This is my second sentence!");
        cTwo.addMessage("Acknowledge");
        cTwo.addMessage("b b b");
        cTwo.addMessage("faggot");
        connectedClients.add(cTwo);
    }

    @Override
    public void run() {
        SocketHandler socketHandler = new SocketHandler();
        socketHandler.start();

        String str = in.nextLine();
        while (!str.toLowerCase().equals("quit")) {

            switch (str.toLowerCase()) {
                case "1":
                    countWords();
                    break;
                case "2":
                    wordsPerClient();
                    break;
            }
            
            System.out.println("Received command: " + str + "\n");
            str = in.nextLine();
        }

        socketHandler.interrupt();
    }

    private void countWords() {
        Object[] arr = connectedClients.stream().flatMap(c -> {
            return c.getMessages().stream();
        }).toArray();
        Object[] arr2 = connectedClients.stream().flatMap(c -> {
            return c.getMessages().stream();
        }).flatMap(s -> Arrays.stream(s.split(" "))).toArray();
        System.out.println("Amount of words: " + arr2.length);
    }

    private void wordsPerClient() {
        Map<ClientHandler, List<String>> wordsPerClient = 
                connectedClients
                        .stream() // stream(ClientHandlers)
                        .collect(Collectors.toMap(
                                c -> c, 
                                c -> new ArrayList<String>(c.getMessages())));
        wordsPerClient.entrySet().stream()
                .forEach(e -> {
                    String words = e.getValue().stream()
                            .flatMap(l -> Arrays.stream(l.split(" "))) // flattening the String lists
                            .sorted(Comparator.comparing(s -> s.toLowerCase())) // sorting by first character lower case
                            .collect(Collectors.joining(" ")); // connecting them together in one string
                    System.out.println(e.getKey().hostAddress + ": " + words + "  [" + words.split(" ").length + " words]");
                });
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
                    ClientHandler handler = new ClientHandler(socket, null);
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

        public ClientHandler(Socket socket, String name) {
            if(name != null)
                hostAddress = name;
            if(socket == null)
                return;
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
                    if (str.toLowerCase().equals("quit")) {
                        this.interrupt();
                    }

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
        
        public String getAddress(){
            return hostAddress;
        }

        public List<String> getMessages() {
            return new ArrayList<>(messages);
        }
        
        public void addMessage(String msg){
            messages.add(msg);
        }
    }
}
