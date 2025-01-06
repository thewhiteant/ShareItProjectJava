// FileReceiverGUI.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class FileReceiverGUI extends JFrame {
    private JTextField portField;
    private JLabel statusLabel;
    private JButton startButton, openLocationButton, openFileButton;
    private File receivedFile;

    public FileReceiverGUI() {
        setTitle("File Receiver");
        setSize(450, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(230, 240, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        portField = new JTextField("12345");
        statusLabel = new JLabel("Waiting to start...");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        startButton = new JButton("Start Receiver");
        startButton.setBackground(new Color(100, 150, 250));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);

        openLocationButton = new JButton("Open File Location");
        openLocationButton.setEnabled(false);
        openLocationButton.setBackground(new Color(100, 200, 150));
        openLocationButton.setForeground(Color.WHITE);
        openLocationButton.setFocusPainted(false);

        openFileButton = new JButton("Open File");
        openFileButton.setEnabled(false);
        openFileButton.setBackground(new Color(100, 200, 150));
        openFileButton.setForeground(Color.WHITE);
        openFileButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(portLabel, gbc);
        gbc.gridx = 1;
        add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        add(startButton, gbc);

        gbc.gridy = 2;
        add(statusLabel, gbc);

        gbc.gridy = 3;
        add(openLocationButton, gbc);

        gbc.gridy = 4;
        add(openFileButton, gbc);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = Integer.parseInt(portField.getText());
                startReceiver(port);
            }
        });

        openLocationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (receivedFile != null) {
                    try {
                        Desktop.getDesktop().open(receivedFile.getParentFile());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(FileReceiverGUI.this, "Failed to open file location.");
                    }
                }
            }
        });

        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (receivedFile != null) {
                    try {
                        Desktop.getDesktop().open(receivedFile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(FileReceiverGUI.this, "Failed to open file.");
                    }
                }
            }
        });
    }

    public void startReceiver(int port) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(port);
                statusLabel.setText("Listening on port " + port + "...");

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String fileName = new String(packet.getData(), 0, packet.getLength());

                socket.receive(packet);
                long fileSize = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));

                receivedFile = new File("received_files/" + fileName);
                receivedFile.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);

                long totalBytesReceived = 0;
                while (totalBytesReceived < fileSize) {
                    socket.receive(packet);
                    fileOutputStream.write(packet.getData(), 0, packet.getLength());
                    totalBytesReceived += packet.getLength();
                }

                fileOutputStream.close();
                socket.close();

                statusLabel.setText("File received: " + receivedFile.getName());
                openLocationButton.setEnabled(true);
                openFileButton.setEnabled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error receiving file.");
            }
        }).start();
    }


}
