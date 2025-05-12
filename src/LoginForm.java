import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginForm() {
        setTitle("Prijava v sistem");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Dobrodošli!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(new Color(52, 73, 94));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField();
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Geslo:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        loginButton = new JButton("Prijavi se");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(46, 204, 113));
        loginButton.setForeground(Color.WHITE);
        panel.add(loginButton, gbc);

        gbc.gridx = 1;
        registerButton = new JButton("Registriraj se");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerButton.setBackground(new Color(52, 152, 219));
        registerButton.setForeground(Color.WHITE);
        panel.add(registerButton, gbc);

        add(panel);

        // Login logika
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String plainPassword = new String(passwordField.getPassword()).trim();

            User user = DatabaseManager.prijaviUporabnika(email, plainPassword);

            if (user != null) {
                JOptionPane.showMessageDialog(null,
                        "Prijava uspešna!\nPozdravljen/a, " + user.getIme() + " " + user.getPriimek(),
                        "Uspeh", JOptionPane.INFORMATION_MESSAGE);

                SwingUtilities.invokeLater(() -> {
                    try {
                        Connection conn = DatabaseManager.getConnection();
                        new DatabaseViewer(conn, email).setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Napaka pri povezavi z bazo!", "Napaka", JOptionPane.ERROR_MESSAGE);
                    }
                });

                dispose();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Nepravilen email ali geslo!",
                        "Napaka", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Prehod na registracijo
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
