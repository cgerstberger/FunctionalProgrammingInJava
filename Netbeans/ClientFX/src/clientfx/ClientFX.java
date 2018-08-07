/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientfx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Christoph
 */
public class ClientFX extends Application {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    Socket socket = null;
    PrintWriter serverOut = null;
    BufferedReader serverIn = null;

    ListView listMessages;
    TextField txtInput;
    Button btnSend;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        
        BorderPane borderPane = (BorderPane) scene.lookup("#borderPane");
        MenuBar menuBar = new MenuBar();
        Menu menuClient = new Menu("Client");
        
        MenuItem menuLogin = new MenuItem("Login");
        menuLogin.setOnAction(event -> login());
        
        MenuItem menuLogout = new MenuItem("Logout");
        menuLogout.setOnAction(event -> logout());
        
        menuClient.getItems().addAll(menuLogin, menuLogout);
        menuBar.getMenus().add(menuClient);
        borderPane.setTop(menuBar);
        scene.setOnKeyPressed(event -> keyPressed(event));

        listMessages = (ListView) scene.lookup("#listMessages");
        listMessages.setEditable(false);
        txtInput = (TextField) scene.lookup("#txtInput");
        btnSend = (Button) scene.lookup("#btnSend");
        btnSend.setOnAction(event -> sendMessageToServer());

        stage.setScene(scene);
        stage.show();
    }

    private void keyPressed(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER){
            sendMessageToServer();
        }
    }

    private void login() {
        if (socket == null) {
            try {
                socket = new Socket(HOST, PORT);
                serverOut = new PrintWriter(socket.getOutputStream());
                serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Logged in...");
                listMessages.getItems().add(0, getCurrentTime() + ": " + "Logged in...");
            } catch (IOException ex) {
                Logger.getLogger(ClientFX.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    }

    private void logout() {
        if (socket != null && socket.isConnected()){
            try {
                serverOut.println("QUIT");
                serverOut.flush();
                socket.close();
                listMessages.getItems().add(0, getCurrentTime() + ": " + "Logged out...");
                
                socket = null;
                serverOut = null;
                serverIn = null;
            } catch (IOException ex) {
                Logger.getLogger(ClientFX.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
            }
        }
    }

    private void sendMessageToServer() {
        String text = txtInput.getText();
        if (socket.isConnected() && !text.isEmpty()) {
            serverOut.println(text);
            serverOut.flush();
            listMessages.getItems().add(0, getCurrentTime() + ": " + text);
            
            txtInput.setText("");
        }
    }
    
    private String getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(cal.getTime());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
