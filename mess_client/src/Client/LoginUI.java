package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginUI extends JFrame implements ActionListener {
    JPanel container = new JPanel();
    JLabel userLabel = new JLabel("Username  ");
    JLabel passwordLabel = new JLabel("Password  ");
    JTextField userTextField = new JTextField(15);
    JPasswordField passwordField = new JPasswordField(15);
    JButton loginButton = new JButton("Login");
    JButton regButton = new JButton("Register");

    public LoginUI() {
        try {
            Client.getObject().connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addComponentsToContainer();
        add(container, BorderLayout.CENTER);
        add(buttonHorizontal(), BorderLayout.SOUTH);

        addActionEvent();

        pack();
        setVisible(true);
    }

    public void addActionEvent() {
        loginButton.addActionListener(this);
        regButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String userText;
        String pwdText;
        userText = userTextField.getText();
        pwdText = passwordField.getText();

        String text;
        if (e.getSource() == loginButton){
            text = "login," + userText + ',' + pwdText;
            Client.getObject().setUsername(userText);
        } else {
            text = "reg," + userText + ',' + pwdText;
        }

        try {
            Client.getObject().send(text);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(container, ex.getMessage());
        }
        dispose();
    }

    public void addComponentsToContainer() {
        container.setPreferredSize(new Dimension(300, 80));
        container.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        container.add(userLabel, gc);
        gc.gridx++;
        container.add(userTextField, gc);

        gc.gridy++;
        gc.gridx = 0;
        container.add(passwordLabel, gc);
        gc.gridx++;
        container.add(passwordField, gc);

        container.setBorder(new EmptyBorder(2, 2, 2, 2));
    }

    public JPanel buttonHorizontal() {
        JPanel button = new JPanel(new FlowLayout());
        button.add(regButton);
        Component rigidArea = Box.createRigidArea(new Dimension(10, 10));
        button.add(rigidArea);
        button.add(loginButton);

        return button;
    }
}
