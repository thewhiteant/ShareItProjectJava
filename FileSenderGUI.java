// FileSenderGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class FileSenderGUI extends JFrame {
    private JTextField serverField, portField;
    private JLabel statusLabel;
    private JButton selectFileButton, sendFileButton;
    private File selectedFile;

    public FileSenderGUI() {
        setTitle("File Sender");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(230, 250, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setFont(new Font("Arial", Font.BOLD, 14));
        serverField = new JTextField("localhost");

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        portField = new JTextField("12345");

        statusLabel = new JLabel("Select a file to send.");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        selectFileButton = new JButton("Select File");
        selectFileButton.setBackground(new Color(100, 150, 250));
        selectFileButton.setForeground(Color.WHITE);
        selectFileButton.setFocusPainted(false);

        sendFileButton = new JButton("Send File");
        sendFileButton.setEnabled(false);
        sendFileButton.setBackground(new Color(100, 200, 150));
        sendFileButton.setForeground(Color.WHITE);
        sendFileButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(serverLabel, gbc);
        gbc.gridx = 1;
        add(serverField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(portLabel, gbc);
        gbc.gridx = 1;
        add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(selectFileButton, gbc);

        gbc.gridy = 3;
        add(sendFileButton, gbc);

        gbc.gridy = 4;
        add(statusLabel, gbc);

        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(FileSenderGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    statusLabel.setText("Selected File: " + selectedFile.getName());
                    sendFileButton.setEnabled(true);
                }
            }
        });

        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    String server = serverField.getText();
                    int port = Integer.parseInt(portField.getText());
                    try {
                        sendFile(selectedFile, server, port);
                        statusLabel.setText("File sent successfully.");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setText("Failed to send file.");
                    }
                }
            }
        });
    }

    public void sendFile(File file, String serverAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;

        byte[] fileNameBytes = file.getName().getBytes();
        DatagramPacket namePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverInetAddress, port);
        socket.send(namePacket);

        long fileLength = file.length();
        DatagramPacket sizePacket = new DatagramPacket(Long.toString(fileLength).getBytes(), Long.toString(fileLength).getBytes().length, serverInetAddress, port);
        socket.send(sizePacket);

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverInetAddress, port);
            socket.send(packet);
        }

        fileInputStream.close();
        socket.close();
    }


}
