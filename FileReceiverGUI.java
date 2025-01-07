import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

public class FileReceiverGUI extends JFrame {
    private JTextField portField;
    private JLabel statusLabel, ipLabel;
    private JButton startButton, openLocationButton, openFileButton, copyIPButton;
    private File receivedFile;

    public FileReceiverGUI() {
        setTitle("AntShare Receiver");
        setSize(500, 400);  // Increased size for better fit
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(230, 240, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        portField = new JTextField("12345");

        statusLabel = new JLabel("Waiting to start...");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        ipLabel = new JLabel("IP: " + getLocalIPAddress());
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 12));

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

        copyIPButton = new JButton("Copy IP");
        copyIPButton.setBackground(new Color(100, 200, 150));
        copyIPButton.setForeground(Color.WHITE);
        copyIPButton.setFocusPainted(false);

        // Layout components with adjusted grid constraints
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(portLabel, gbc);

        gbc.gridx = 1;
        add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;  // Make this button span both columns
        add(startButton, gbc);

        gbc.gridy = 2;
        add(statusLabel, gbc);

        gbc.gridy = 3;
        add(ipLabel, gbc);

        gbc.gridy = 4;
        add(openLocationButton, gbc);

        gbc.gridy = 5;
        add(openFileButton, gbc);

        gbc.gridy = 6;
        add(copyIPButton, gbc);

        startButton.addActionListener(e -> {
            int port = Integer.parseInt(portField.getText());
            startReceiver(port);
        });

        openLocationButton.addActionListener(e -> {
            if (receivedFile != null) {
                try {
                    Desktop.getDesktop().open(receivedFile.getParentFile());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FileReceiverGUI.this, "Failed to open file location.");
                }
            }
        });

        openFileButton.addActionListener(e -> {
            if (receivedFile != null) {
                try {
                    Desktop.getDesktop().open(receivedFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(FileReceiverGUI.this, "Failed to open file.");
                }
            }
        });

        copyIPButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(getLocalIPAddress());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(FileReceiverGUI.this, "IP address copied to clipboard.");
        });
    }

    private String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown IP";
        }
    }

    public void startReceiver(int port) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(port);
                SwingUtilities.invokeLater(() -> statusLabel.setText("Listening on port " + port + "..."));

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                String fileName = new String(packet.getData(), 0, packet.getLength());

                socket.receive(packet);
                long fileSize = Long.parseLong(new String(packet.getData(), 0, packet.getLength()));

                receivedFile = new File("received_files/" + fileName);
                if (!receivedFile.getParentFile().exists() && !receivedFile.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create directory.");
                }
                FileOutputStream fileOutputStream = new FileOutputStream(receivedFile);

                long totalBytesReceived = 0;
                while (totalBytesReceived < fileSize) {
                    socket.receive(packet);
                    fileOutputStream.write(packet.getData(), 0, packet.getLength());
                    totalBytesReceived += packet.getLength();
                }

                fileOutputStream.close();
                socket.close();

                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("File received: " + receivedFile.getName());
                    openLocationButton.setEnabled(true);
                    openFileButton.setEnabled(true);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Error receiving file."));
            }
        }).start();
    }
}
