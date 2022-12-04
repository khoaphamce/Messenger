package Client;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler {
    BufferedWriter writer;
    BufferedReader reader;
    BufferedWriter mySocketWriter;
    BufferedReader mySocketreader;

    Socket socket;
    Client myClient;

    public Socket getSocket(){
        return this.socket;
    }

    public ClientHandler(Socket socket, Client myClient) throws IOException {
        this.socket = socket;
        this.myClient = myClient;

        System.out.println("Client name in clienthandler: " + myClient.getUsername());

        InputStream is = socket.getInputStream();
        reader = new BufferedReader(new InputStreamReader(is));

        OutputStream os = socket.getOutputStream();
        writer = new BufferedWriter(new OutputStreamWriter(os));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        String receivedMessage;
                        try {
                            receivedMessage = reader.readLine();
                            System.out.println("Message received: " + receivedMessage);
                            String parseMess[] = receivedMessage.split(",");
                            route(parseMess);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } while (true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.print("Socket closed");
                }
            }
        });
        t.start();
    }

    public void route(String[] parseMess) throws IOException {
        switch(parseMess[0]){
            case "chat":
                String sender = parseMess[1];
                String msg = parseMess[3];
                receiveMsg(sender, msg);
                break;
            case "info":
                String from = parseMess[1];
                String to = parseMess[2];
                String name = parseMess[3];
                String length = parseMess[4];
                break;
        }
    }

    public void send(String sentMessage) throws IOException {
        writer.write(sentMessage);
        writer.newLine();
        writer.flush();
    }

//    public void sendMySocket(String sentMessage) throws IOException {
//        mySocketWriter.write(sentMessage);
//        mySocketWriter.newLine();
//        mySocketWriter.flush();
//    }

    public void receiveMsg(String sender, String msg) throws IOException{
        if(myClient.chatbox == null){
            myClient.chatbox = new ChatBoxUI(myClient.getUsername());
        }
        myClient.chatbox.getTextArea().append(sender + ": " + msg + "\n");
    }
}
