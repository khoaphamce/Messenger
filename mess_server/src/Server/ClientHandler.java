package Server;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler {
    BufferedWriter writer;
    BufferedReader reader;
    Socket socket;
    Server log;
    String FILENAME = "user.csv";
    int hostPort;

    public Socket getSocket(){
        return this.socket;
    }

    public ClientHandler(Socket socket, Server log) throws IOException {
        this.socket = socket;
        this.log = log;
        this.hostPort = socket.getPort();

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
                            log.getText().append("Received: " + receivedMessage + '\n');
                            String parseMess[] = receivedMessage.split(",");
                            route(parseMess);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } while (true);
                } catch (Exception e) {
                    System.out.print("Socket closed");
                }
            }
        });
        t.start();
    }

    public static String[] parseStringCSV(String csvStr) {
        String[] res = null;
        if (csvStr != null) {
            res = csvStr.split(",");
        }
        return res;
    }

    public void writeAccount(String fname, String username, String password) {
        String info = username + ',' + password;
        try {
            FileWriter fw  = new FileWriter(fname, true);
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(info);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(log.getNotify(), e.getMessage());
        }
    }

//    public void writeBuffer()

    public void checkReg(String username, String password) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
            try {
                while ((line = reader.readLine()) != null) {
                    String[] res = parseStringCSV(line);
                    if (username.equals(res[0])) {
                        writer.write("reg,Account existed");
                        writer.newLine();
                        writer.flush();
                        return;
                    }
                }

                writeAccount(FILENAME, username, password);
                writer.write("reg,Registered successfully!");
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(log.getNotify(), "File not found!");
        }
    }

    public void checkLogin(String username, String password) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(FILENAME));
            try {
                while ((line = reader.readLine()) != null) {
                    String[] res = parseStringCSV(line);
                    if (username.equals(res[0]) && password.equals(res[1])) {
                        log.getOnline().put(username,this);
                        String onlStr = getOnl();
                        writer.write("login,true,Logged in successfully!,"+onlStr);
                        writer.newLine();
                        writer.flush();
                        return;
                    }
                }
                writer.write("login,false,Invalid username or password!");
                writer.newLine();
                writer.flush();
                return;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(log.getNotify(), "File not found!");
        }
    }

    ClientHandler getClientHandler(String clientName){
        HashMap<String, ClientHandler> onl = log.getOnline();
        return onl.get(clientName);
    }

    public String getOnl(){
        String res = "";
        HashMap<String, ClientHandler> onl = log.getOnline();
        ArrayList<String> onlName = new ArrayList<>();
        for(String i: onl.keySet()){
            onlName.add(i);
        }
        for(int i = 0;i<onlName.size()-1;i++){
            res+=onlName.get(i)+"`";
        }
        res+=onlName.get(onlName.size()-1);
        return res;
    }

    public void getClientPort(String clientName, String destinationName){
        try {
            ClientHandler targetHandler = getClientHandler(clientName);
            System.out.println("Getting client handler of " + clientName);
            targetHandler.send("get-p2p-server-port," + destinationName);
        }
        catch(IOException e){
            System.out.println("Can not get port of " + clientName);
        }
    }

    public void route(String[] parseMess) throws IOException {
        switch(parseMess[0]){
            case "reg":
                checkReg(parseMess[1],parseMess[2]);
                break;
            case "login":
                checkLogin(parseMess[1],parseMess[2]);
                break;
            case "refresh":
                String onlList = getOnl();
                send("refresh,"+onlList);
                break;
            case "logout":
                log.getOnline().remove(parseMess[1]);
                socket.close();
                break;
            case "get-client-p2p-port":
                getClientPort(parseMess[1], parseMess[2]);
                break;
            case "return-p2p-server-port":
                ClientHandler targetClientHandler = getClientHandler(parseMess[2]);
                targetClientHandler.send("return-client-port," + parseMess[1]);
                break;


        }
    }

    public void processFile(String receiver, String fileName, String fileSize) throws IOException {
        ClientHandler receive = log.getOnline().get(receiver);
        receive.send("send-file,"+fileName+","+fileSize);
        log.getText().append(fileName+"@"+fileSize+"\n");

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(receive.getSocket().getOutputStream());

        byte[] buffer = new byte[4096];

        int count;
        int remain = Integer.parseInt(fileSize);

        while (remain>0) {
            count = in.read(buffer,0,Math.min(remain,4096));
            remain -= count;
            out.write(buffer,0,count);
            log.getText().append("Received: "+remain+" byte\n");
        }
        out.flush();
        in.skip(in.available());
    }

    public void send(String sentMessage) throws IOException {
        writer.write(sentMessage);
        writer.newLine();
        writer.flush();
    }
}
