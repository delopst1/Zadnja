import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JTextField davcnaField;
    private JButton loginButton;

    public LoginForm() {
        setTitle("Prijava");
        setSize(350, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));

        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField();

        JLabel davcnaLabel = new JLabel("Davčna številka:");
        davcnaField = new JTextField();

        loginButton = new JButton("Prijavi se");

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(davcnaLabel);
        panel.add(davcnaField);
        panel.add(new JLabel()); // prazen prostor
        panel.add(loginButton);

        add(panel);

        // Dogodek za klik na gumb
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String davcna = davcnaField.getText().trim();

                // Preveri uporabnika
                User user = DatabaseManager.prijaviUporabnika(email, davcna);

                if (user != null) {
                    JOptionPane.showMessageDialog(null,
                            "Prijava uspešna!\nPozdravljen/a, " + user.getIme() + " " + user.getPriimek(),
                            "Uspeh", JOptionPane.INFORMATION_MESSAGE);

                    // Odpri DatabaseViewer z obstoječo povezavo
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Connection conn = DatabaseManager.getConnection();
                            new DatabaseViewer(conn).setVisible(true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Napaka pri povezavi z bazo!", "Napaka", JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    dispose(); // zapri LoginForm

                } else {
                    JOptionPane.showMessageDialog(null,
                            "Nepravilen email ali davčna številka!",
                            "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
