import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DatabaseViewer extends JFrame {

    private JTabbedPane tabbedPane;

    public DatabaseViewer(Connection connection) {
        setTitle("Pregled baze podatkov");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Glavni tabs za tabele
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Gumbi na spodnjem delu
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnOsvezi = new JButton("Osveži");
        JButton btnDodaj = new JButton("Dodaj");
        JButton btnUredi = new JButton("Uredi");
        JButton btnIzbrisi = new JButton("Izbriši");

        buttonPanel.add(btnOsvezi);
        buttonPanel.add(btnDodaj);
        buttonPanel.add(btnUredi);
        buttonPanel.add(btnIzbrisi);
        add(buttonPanel, BorderLayout.SOUTH);

        // Naloži vse tabele
        izpisiVseTabele(connection);
    }

    private void izpisiVseTabele(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();

                // Priprava podatkov za JTable
                Vector<String> columnNames = new Vector<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(rsMeta.getColumnName(i));
                }

                Vector<Vector<Object>> data = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getObject(i));
                    }
                    data.add(row);
                }

                JTable table = new JTable(data, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                table.setFillsViewportHeight(true);
                table.setRowHeight(25);

                JScrollPane scrollPane = new JScrollPane(table);
                tabbedPane.addTab(tableName, scrollPane);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Napaka pri branju baze: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Glavni zagon za test
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://zaklucni-delopst-3439.c.aivencloud.com:21525/defaultdb?sslmode=require",
                    "avnadmin",
                    "AVNS_28Co5bU1SYV6F4LHOPy"
            );
            SwingUtilities.invokeLater(() -> new DatabaseViewer(conn).setVisible(true));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
