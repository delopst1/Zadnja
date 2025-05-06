import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class DatabaseViewer extends JFrame {

    private JTabbedPane tabbedPane;

    public DatabaseViewer(Connection connection, String identifikator) {
        setTitle("Pregled baze podatkov");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 248, 255));
        JButton btnOsvezi = new JButton("Osveži");
        JButton btnDodaj = new JButton("Dodaj");
        JButton btnUredi = new JButton("Uredi");
        JButton btnIzbrisi = new JButton("Izbriši");

        styleButton(btnOsvezi);
        styleButton(btnDodaj);
        styleButton(btnUredi);
        styleButton(btnIzbrisi);

        buttonPanel.add(btnOsvezi);

        boolean jeAdmin = jeUporabnikAdmin(connection, identifikator);
        if (jeAdmin) {
            buttonPanel.add(btnDodaj);
            buttonPanel.add(btnUredi);
            buttonPanel.add(btnIzbrisi);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        izpisiVseTabele(connection);
    }

    private boolean jeUporabnikAdmin(Connection conn, String identifikator) {
        try {
            String sql = "SELECT je_admin FROM student WHERE email = ? OR davcna_stevilka = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, identifikator);
            stmt.setString(2, identifikator);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("je_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void izpisiVseTabele(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");

                Statement stmt = conn.createStatement();

                // Prilagojena poizvedba za tabelo 'delo'
                String query;
                if (tableName.equalsIgnoreCase("delo")) {
                    query = """
                        SELECT d.naziv, d.placilo, d.prosta_mesta, 
                               del.ime_podjetja AS delodajalec, 
                               n.stevilka AS napotnica
                        FROM delo d
                        JOIN delodajalec del ON d.delodajalec_id = del.id
                        JOIN napotnica n ON d.napotnica_id = n.id
                    """;
                } else {
                    query = "SELECT * FROM " + tableName;
                }

                ResultSet rs = stmt.executeQuery(query);
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();

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

                // Skrij stolpec 'id' in 'student_id', če obstajata
                int idColumnIndex = columnNames.indexOf("id");
                if (idColumnIndex != -1) {
                    table.removeColumn(table.getColumnModel().getColumn(idColumnIndex));
                }

                int studentIdColumnIndex = columnNames.indexOf("student_id");
                if (studentIdColumnIndex != -1) {
                    table.removeColumn(table.getColumnModel().getColumn(studentIdColumnIndex));
                }

                styleTable(table);

                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                tabbedPane.addTab(tableName, scrollPane);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Napaka pri branju baze: " + e.getMessage(), "Napaka", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(220, 220, 220));
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(184, 207, 229));
        table.setSelectionForeground(Color.BLACK);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        header.setBackground(new Color(200, 220, 240));
        header.setForeground(new Color(30, 30, 30));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(new Color(166, 202, 240));
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : new Color(235, 243, 255));
                }
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                return c;
            }
        });

        JScrollBar verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
            }
        });

        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
        if (scrollPane != null) {
            scrollPane.setVerticalScrollBar(verticalScrollBar);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }
}
