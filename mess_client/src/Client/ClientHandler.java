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

        myClient.p2pHandlerServer = this;
    }

    public void route(String[] parseMess) throws IOException {
        switch(parseMess[0]){
            case "chat":
                String sender = parseMess[1];
                String msg = parseMess[3];
                receiveMsg(sender, msg);
                break;

            case "init-file-send":
                String from = parseMess[1];
                String filename = parseMess[2];
                String fileSize = parseMess[3];
                myClient.confirm(from, filename, fileSize);
                break;

            case "accept-file":
                myClient.startSendingFile();
                break;

            case "send-file":
                myClient.receiveFile(parseMess[1], parseMess[2]);
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

    public void receiveFile(String fileName, String fileSize) throws IOException {
        System.out.println(fileName+fileSize);

        // Get input stream
        DataInputStream in = new DataInputStream(myClient.getSocket().getInputStream());

        // Init output stream
        FileOutputStream out = new FileOutputStream(fileName);

        int remain = Integer.parseInt(fileSize);

        byte[] buffer = new byte[4096];

        System.out.println("Starting to receive");

        while (remain>0) {
            int outBufferSize = in.read(buffer,0,Math.min(4096,remain));
            remain -= outBufferSize;

            byte[] tempBuffer = new byte[outBufferSize];

            for (int i = 0; i < outBufferSize;i++)
                tempBuffer[i] = buffer[i];

            out.write(tempBuffer);

            System.out.println("The rest size: " + remain);
        }

        out.flush();
        out.close();

        in.skipBytes(in.available());

        JOptionPane.showMessageDialog(null,"File saved!");
    }


}
