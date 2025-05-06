import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;  // Dodan gumb za registracijo

    public LoginForm() {
        setTitle("Prijava");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10)); // več vrstic za dodatni gumb

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Geslo:");
        passwordField = new JPasswordField();

        loginButton = new JButton("Prijavi se");
        registerButton = new JButton("Registriraj se"); // Gumb za registracijo

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel()); // prazen prostor
        panel.add(loginButton);
        panel.add(new JLabel()); // prazen prostor
        panel.add(registerButton); // Dodaj gumb v panel

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                    dispose(); // zapri LoginForm
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Nepravilen email ali geslo!",
                            "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Dodana akcija za prehod na registracijo
        registerButton.addActionListener(e -> {
            dispose(); // zapremo login okno
            new RegisterForm().setVisible(true); // odpremo registracijsko okno
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
