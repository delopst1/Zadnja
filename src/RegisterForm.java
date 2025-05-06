import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterForm extends JFrame {
    private JTextField imeField;
    private JTextField priimekField;
    private JTextField emailField;
    private JTextField telefonField;
    private JTextField datumRojstvaField;
    private JTextField emsoField;
    private JTextField davcnaField;
    private JTextField racunField;
    private JTextField krajIdField;
    private JPasswordField gesloField;
    private JButton registerButton;

    public RegisterForm() {
        setTitle("Registracija");
        setSize(450, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(11, 2, 10, 10));

        panel.add(new JLabel("Ime:"));
        imeField = new JTextField();
        panel.add(imeField);

        panel.add(new JLabel("Priimek:"));
        priimekField = new JTextField();
        panel.add(priimekField);

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Telefon:"));
        telefonField = new JTextField();
        panel.add(telefonField);

        panel.add(new JLabel("Datum rojstva (YYYY-MM-DD):"));
        datumRojstvaField = new JTextField();
        panel.add(datumRojstvaField);

        panel.add(new JLabel("EMŠO:"));
        emsoField = new JTextField();
        panel.add(emsoField);

        panel.add(new JLabel("Davčna številka:"));
        davcnaField = new JTextField();
        panel.add(davcnaField);

        panel.add(new JLabel("Št. bančnega računa:"));
        racunField = new JTextField();
        panel.add(racunField);

        panel.add(new JLabel("Kraj ID:"));
        krajIdField = new JTextField();
        panel.add(krajIdField);

        panel.add(new JLabel("Geslo:"));
        gesloField = new JPasswordField();
        panel.add(gesloField);

        registerButton = new JButton("Registriraj se");
        panel.add(new JLabel());
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ime = imeField.getText().trim();
                String priimek = priimekField.getText().trim();
                String email = emailField.getText().trim();
                String telefon = telefonField.getText().trim();
                String datumRojstva = datumRojstvaField.getText().trim();
                String emso = emsoField.getText().trim();
                String davcna = davcnaField.getText().trim();
                String stevilkaRacuna = racunField.getText().trim();
                String krajIdText = krajIdField.getText().trim();
                String plainPassword = new String(gesloField.getPassword()).trim();

                String hashedPassword = PasswordHasher.hashPassword(plainPassword);

                try (Connection conn = DatabaseManager.getConnection()) {
                    // Popravek: posodobi sekvenco ID-jev glede na trenutno največjo vrednost v tabeli
                    String updateSequence = "SELECT setval(pg_get_serial_sequence('student', 'id'), (SELECT MAX(id) FROM student))";
                    PreparedStatement seqStmt = conn.prepareStatement(updateSequence);
                    seqStmt.execute();

                    // Registracija uporabnika
                    String sql = """
                        INSERT INTO student (
                            ime, priimek, email, telefon, datum_rojstva, emso, davcna_stevilka,
                            "stevilka_Bancnega_racuna", kraj_id, geslo, je_admin
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false)
                        """;

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, ime);
                    stmt.setString(2, priimek);
                    stmt.setString(3, email);
                    stmt.setInt(4, Integer.parseInt(telefon));
                    stmt.setDate(5, java.sql.Date.valueOf(datumRojstva));
                    stmt.setString(6, emso);
                    stmt.setString(7, davcna);
                    stmt.setString(8, stevilkaRacuna);
                    stmt.setInt(9, Integer.parseInt(krajIdText));
                    stmt.setString(10, hashedPassword);

                    int rowsInserted = stmt.executeUpdate();

                    if (rowsInserted > 0) {
                        JOptionPane.showMessageDialog(null, "Registracija uspešna! Sedaj se lahko prijavite.");
                        dispose();
                        new LoginForm().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(null, "Napaka pri registraciji.", "Napaka", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Napaka pri povezavi z bazo ali pri vnosu podatkov.", "Napaka", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
