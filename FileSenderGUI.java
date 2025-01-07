import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class FileSenderGUI extends JFrame {
    private JTextField serverField, portField;
    private JLabel statusLabel;
    private JButton selectFileButton, sendFileButton;
    private JProgressBar progressBar;
    private File selectedFile;

    public FileSenderGUI() {
        setTitle("AntShare Sender");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(230, 250, 240));
        String localIp = "127.0.0.1"; // Default to localhost loopback address

        try {
            localIp = InetAddress.getByName("localhost").getHostAddress(); // Resolve localhost directly
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setFont(new Font("Arial", Font.BOLD, 14));
        serverField = new JTextField(localIp);
        serverField.setPreferredSize(new Dimension(200, 30));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Arial", Font.BOLD, 14));
        portField = new JTextField("12345");
        portField.setPreferredSize(new Dimension(100, 30));

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

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

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
        add(progressBar, gbc);

        gbc.gridy = 5;
        add(statusLabel, gbc);

        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(FileSenderGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                statusLabel.setText("Selected File: " + selectedFile.getName());
                sendFileButton.setEnabled(true);
            }
        });

        sendFileButton.addActionListener(e -> {
            if (selectedFile != null) {
                String server = serverField.getText();
                try {
                    int port = Integer.parseInt(portField.getText());
                    if (port < 1 || port > 65535) {
                        throw new NumberFormatException();
                    }
                    progressBar.setVisible(true);
                    progressBar.setValue(0);
                    sendFileInBackground(selectedFile, server, port);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid port. Enter a number between 1 and 65535.");
                }
            }
        });
    }

    private void sendFileInBackground(File file, String serverAddress, int port) {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                DatagramSocket socket = new DatagramSocket();
                InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesSent = 0;
                long fileLength = file.length();

                byte[] fileNameBytes = file.getName().getBytes();
                DatagramPacket namePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, serverInetAddress, port);
                socket.send(namePacket);

                DatagramPacket sizePacket = new DatagramPacket(Long.toString(fileLength).getBytes(), Long.toString(fileLength).getBytes().length, serverInetAddress, port);
                socket.send(sizePacket);

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, serverInetAddress, port);
                    socket.send(packet);
                    totalBytesSent += bytesRead;
                    int progress = (int) ((totalBytesSent * 100) / fileLength);
                    publish(progress);
                }

                fileInputStream.close();
                socket.close();
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int lastProgress = chunks.get(chunks.size() - 1);
                progressBar.setValue(lastProgress);
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("File sent successfully.");
                    JOptionPane.showMessageDialog(FileSenderGUI.this, "File sent successfully!");
                } catch (Exception e) {
                    statusLabel.setText("Failed to send file.");
                    e.printStackTrace();
                } finally {
                    progressBar.setVisible(false);
                }
            }
        };
        worker.execute();
    }


}
