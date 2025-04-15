import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText().trim();
                String davcna = davcnaField.getText().trim();

                // Preverjanje uporabnika z uporabo DatabaseManager
                User user = DatabaseManager.prijaviUporabnika(email, davcna);

                if (user != null) {
                    JOptionPane.showMessageDialog(null,
                            "Prijava uspešna!\nPozdravljen/a, " + user.getIme() + " " + user.getPriimek(),
                            "Uspeh", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Nepravilen email ali davčna številka!",
                            "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

    }
}
