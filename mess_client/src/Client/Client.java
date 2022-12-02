package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
//buferredreader bufferedwriter inputstreamreader outputstreamwriter

import java.net.*;
import java.util.ArrayList;

import java.util.HashMap;

public class Client
{
    private static Client instance;
    private Socket s;
    private InputStream is;
    private BufferedReader br;
    private BufferedWriter bw;
    private OutputStream os;
    private String receivedMessage;
    private String[] res;
    private JPanel panel = new JPanel();
    private String onl="";
    private String username;
    File sendingFile = new File("");

    private Client() {
    }

    public Socket getSocket(){
        return this.s;
    }

    public void setSendingFile(File sendingFile){
        this.sendingFile = sendingFile;
    }

    public BufferedWriter getOut(){
        return this.bw;
    }


    public String[] parseString(String csvStr) {
        String[] res = null;
        if (csvStr != null) {
            res = csvStr.split(",");
        }
        return res;
    }

    public String[] parseOnl(String csvStr) {
        String[] res = null;
        if (csvStr != null) {
            res = csvStr.split("`");
        }
        return res;
    }

    public void connect() throws IOException {
        s = new Socket("localhost",3200);
        System.out.println(s.getPort());

        is=s.getInputStream();
        br=new BufferedReader(new InputStreamReader(is));

        os=s.getOutputStream();
        bw = new BufferedWriter(new OutputStreamWriter(os));

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    do {
                        receivedMessage = br.readLine();
                        res = parseString(receivedMessage);
                        route();
                    } while (true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    public void StringParser(String FILENAME, String csvString){

    }

    public void route(){
        switch(res[0]){
            case "reg":
                JOptionPane.showMessageDialog(panel, res[1]);
                new LoginUI();
                break;
            case "login":
                if(res[1].equals("false")){
                    new LoginUI();
                    JOptionPane.showMessageDialog(panel, res[2]);
                } else{
                    JOptionPane.showMessageDialog(panel, res[2]);
                    onl=res[3];
                    new ChatBoardUI();
                }
                break;
            case "refresh":
                onl=res[1];
                new ChatBoardUI();
                break;
            case "info":
                String fileName = res[2];
                String from = res[1];
                String length = res[3];

            case "accept":
                try{
                    DataInputStream in = new DataInputStream(new FileInputStream(sendingFile));
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());


                    byte[] buffer = new byte[4096];
                    int count;

                    while ((count=in.read(buffer))>0) {
                        out.write(buffer,0,count);
                    }
                    out.flush();
                    in.skip(in.available());
                } catch (IOException ex){
                    ex.getMessage();
                }
                break;
            case "send-file":
                try {
                    receiveFile(res[1],res[2]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
//        }
        }


    public static Client getObject() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public String getUsername() {
        return this.username;
    }

    public void send(String sentMessage) throws IOException {
        bw.write(sentMessage);
        bw.newLine();
        bw.flush();
    }

    public ArrayList<String> getOnl(){
        String onlList[]=parseOnl(onl);
        ArrayList<String> res = new ArrayList<>();
        for(int i=0;i<onlList.length;i++){
            res.add(onlList[i]);
        }

                return res;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
