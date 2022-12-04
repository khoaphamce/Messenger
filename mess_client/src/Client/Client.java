package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Client
{
    private static Client instance;
    private Socket s;
    private InputStream is;
    private BufferedReader br;
    private OutputStream os;
    private BufferedWriter bw;
    private String receivedMessage;
    private String[] res;
    private JPanel panel = new JPanel();
    private String onl="";
    private String username;
    File sendingFile = new File("");
    HashMap<String, ChatBoxUI> chatBoxList = new HashMap<>();
    ChatBoxUI chatbox;

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

    public HashMap<String, ChatBoxUI> getChatBox(){
        return this.chatBoxList;
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

    public void route(){
//        System.out.println(res[0]);
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
            case "chat":
                String sender=res[1];
                if(chatBoxList.get(sender) == null){
                    chatbox = new ChatBoxUI(sender);
                    chatBoxList.put(sender, chatbox);
                }
                String msg=""+sender+": "+res[2]+"\n";
                chatBoxList.get(sender).getTextArea().append(msg);
                break;
            case "info":
                String fileName = res[2];
                String from = res[1];
                String length = res[3];

                confirm(from,fileName,length);
            case "accept":
                try{
                    DataInputStream in = new DataInputStream(new FileInputStream(sendingFile));
                    DataOutputStream out = new DataOutputStream(s.getOutputStream());

                    send("send-file,"+res[1]+","+sendingFile.getName()+","+sendingFile.length());

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
        }
    }

    private void receiveFile(String fileName, String fileSize) throws IOException {
        System.out.println(fileName+fileSize);

        // Get input stream
        DataInputStream in = new DataInputStream(s.getInputStream());

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

    public void confirm(String from, String fileName, String length){
        JFrame frame = new JFrame("Confirm receive");
        frame.setLocationRelativeTo(null);
        frame.setPreferredSize(new Dimension(400, 150));

        JPanel panel = new JPanel(new BorderLayout());

        JPanel button = new JPanel(new FlowLayout());

        JButton yes = new JButton("Yes");
        JButton no = new JButton("Decline");

        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    send("accept,"+username+","+from+","+fileName+","+length);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
            }


        });

        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    send("decline,"+from);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                frame.dispose();
            }
        });

        button.add(yes);
        Component rigidArea = Box.createRigidArea(new Dimension(8, 0));
        button.add(rigidArea);
        button.add(no);

        panel.add(new JLabel("Do you want to receive "+fileName+"?"),BorderLayout.NORTH);
        panel.add(button,BorderLayout.SOUTH);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
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
