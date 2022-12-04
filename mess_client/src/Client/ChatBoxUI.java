package Client;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;

public class ChatBoxUI extends JFrame implements ActionListener {
    JTextArea chat = new JTextArea(20, 10);
    JScrollPane scroll;
    JButton send;
    String name;
    JButton sendFile;
    JTextField text;

    public JTextArea getTextArea(){
        return this.chat;
    }

    public ChatBoxUI(String name) {
        this.name = name;

        setLayout(new BorderLayout());
        setTitle(name);
        setPreferredSize(new Dimension(500, 400));

        scroll = setScrollPane();

        JPanel type = new JPanel(new FlowLayout(5,10,5));
        text = new JTextField(18);
        send = new JButton("Send");
        send.addActionListener(this);

        sendFile = new JButton("File");
        sendFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                String filePath="";
                String fileName="";
                if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(null)) {
                    filePath = chooser.getSelectedFile().getAbsolutePath();
                    fileName=chooser.getSelectedFile().getName();
                }

                try{
                    File file = new File(filePath);
                    Client.getObject().setSendingFile(file);

                    String username = Client.getObject().getUsername();
                    Client.getObject().send("info,"+username+","+name+","+fileName+","+file.length());
                } catch (Exception ex){
                    ex.getMessage();
                }
            }
        });

        type.setBorder(new EmptyBorder(4,4,4,4));

        type.add(text);
        type.add(sendFile);
        type.add(send);

        JLabel username = new JLabel("Me: " + Client.getObject().getUsername());

        add(username,BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);
        add(type,BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                Client.getObject().getChatBox().remove(name);
            }
        });
    }

    JScrollPane setScrollPane(){
        chat.setEditable(false);
        JScrollPane log = new JScrollPane(chat);
        log.setBorder(new EmptyBorder(4, 4, 4, 4));
        return log;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == send){
            try {
                String username = Client.getObject().getUsername();
                String msg = text.getText();
                Client.getObject().getChatBox().put(name,this);
                chat.append(username+": "+msg+"\n");
                Client.getObject().sendp2p("chat,"+username+","+name+","+msg);
                text.setText("");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
