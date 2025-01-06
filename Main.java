// Main.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {
    public Main() {
        setTitle("File Transfer - Main Menu");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(255, 240, 230));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel label = new JLabel("Choose an option:", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));

        JButton senderButton = new JButton("Launch File Sender");
        senderButton.setBackground(new Color(100, 200, 100));
        senderButton.setForeground(Color.WHITE);
        senderButton.setFocusPainted(false);

        JButton receiverButton = new JButton("Launch File Receiver");
        receiverButton.setBackground(new Color(100, 150, 250));
        receiverButton.setForeground(Color.WHITE);
        receiverButton.setFocusPainted(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(label, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(senderButton, gbc);

        gbc.gridx = 1;
        add(receiverButton, gbc);

        senderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new FileSenderGUI().setVisible(true));
                dispose(); // Close the main menu
            }
        });

        receiverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new FileReceiverGUI().setVisible(true));
                dispose(); // Close the main menu
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
