package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;

public class ChatBoardUI extends JFrame implements ActionListener {
    JScrollPane scrollPane;
    JList online;
    JButton refresh;
    JButton logout;
    ArrayList<String> onl;
    DefaultListModel<String> model = new DefaultListModel<>();
    JList linkList = new JList( model );

    public ChatBoardUI(){
        setLayout(new BorderLayout());
        setTitle("Chat board");

        setPreferredSize(new Dimension(400, 350));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel name = new JLabel("Hi, "+ Client.getObject().getUsername());
        JLabel label = new JLabel("Currently online friends");
        label.setForeground(Color.blue);
        label.setVerticalAlignment(JLabel.TOP);
        label.setFont(new Font("Verdana", Font.PLAIN, 15));
        label.setPreferredSize(new Dimension(100, 20));
        label.setHorizontalAlignment(JLabel.LEFT);

        JLabel gloss = new JLabel("Double-click to chat!");

        refresh = new JButton("Refresh");
        refresh.addActionListener(this);

        logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Client.getObject().send("logout,"+Client.getObject().getUsername());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                dispose();
            }
        });

        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane,BoxLayout.Y_AXIS));

        topPane.add(name);
        topPane.add(Box.createRigidArea(new Dimension(0,5)));
        topPane.add(label);
        topPane.add(Box.createRigidArea(new Dimension(0,5)));
        topPane.add(gloss);
        topPane.add(Box.createRigidArea(new Dimension(0,5)));
        topPane.add(refresh);
        topPane.add(Box.createRigidArea(new Dimension(0,5)));
        topPane.add(logout);
        topPane.add(Box.createRigidArea(new Dimension(0,5)));

        scrollPane = ScrollPane();

        JPanel panel= new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(topPane, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public JScrollPane ScrollPane(){
        DefaultListModel<String> model = new DefaultListModel<>();
        onl = Client.getObject().getOnl();
        for(String user : onl){
            model.addElement(user);
        }
        online = new JList( model );
        online.addMouseListener(mouseListener);
        JScrollPane s = new JScrollPane(online);
        return s;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == refresh){
            try {
                Client.getObject().send("refresh");
                dispose();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                String name = (String) online.getSelectedValue();
                Chat chatbox = new Chat(name);
                Client.getObject().getChatBox().put(name,chatbox);
            }
        }
    };
}
