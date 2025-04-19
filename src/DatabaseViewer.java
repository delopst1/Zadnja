import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class DatabaseViewer extends JFrame {

    private static final String URL = "jdbc:postgresql://zaklucni-delopst-3439.c.aivencloud.com:21525/defaultdb?sslmode=require";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_28Co5bU1SYV6F4LHOPy";

    private JTextArea textArea;

    public DatabaseViewer() {
        setTitle("Pregled baze podatkov");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // TextArea za izpis podatkov
        textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Panel z gumbi
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton btnOsvezi = new JButton("Osveži");
        JButton btnDodaj = new JButton("Dodaj");
        JButton btnUredi = new JButton("Uredi");
        JButton btnIzbrisi = new JButton("Izbriši");

        buttonPanel.add(btnOsvezi);
        buttonPanel.add(btnDodaj);
        buttonPanel.add(btnUredi);
        buttonPanel.add(btnIzbrisi);

        add(buttonPanel, BorderLayout.SOUTH);

        // Naloži podatke
        izpisiVseTabele();
    }

    private void izpisiVseTabele() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                textArea.append("Tabela: " + tableName + "\n");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);

                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();

                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        textArea.append(rsMeta.getColumnName(i) + ": " + rs.getString(i) + "  ");
                    }
                    textArea.append("\n");
                }
                textArea.append("\n----------------------------------------\n\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            textArea.append("Napaka pri dostopu do baze: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DatabaseViewer().setVisible(true);
        });
    }
}
