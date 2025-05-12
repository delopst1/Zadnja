import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Vector;

public class DatabaseViewer extends JFrame {

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel profilePanel;
    private JPanel tablesPanel;
    private JPanel myWorkPanel;
    private JLabel imageLabel;
    private JLabel emailLabel;
    private JLabel nameLabel;
    private JLabel taxNumberLabel;
    private boolean jeAdmin;
    private String uporabnikEmail;
    private Connection conn;
    private JPanel topSidebarPanel;

    public DatabaseViewer(Connection connection, String identifikator) {
        this.conn = connection;
        this.uporabnikEmail = identifikator;

        setTitle("Študentski Portal");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        jeAdmin = jeUporabnikAdmin(conn, identifikator);

        // Zgornji Sidebar (navigacija)
        topSidebarPanel = new JPanel();
        topSidebarPanel.setBackground(new Color(45, 62, 80));
        topSidebarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton btnProfile = createSidebarButton("Osebna stran");
        JButton btnTables = createSidebarButton("Pregled tabele");
        JButton btnMyWork = createSidebarButton("Moja dela");
        JButton btnLogout = createSidebarButton("Odjava");

        topSidebarPanel.add(btnProfile);
        topSidebarPanel.add(btnTables);
        topSidebarPanel.add(btnMyWork);
        topSidebarPanel.add(btnLogout);

        add(topSidebarPanel, BorderLayout.NORTH);

        // Glavna vsebina
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        profilePanel = buildProfilePanel();
        tablesPanel = new JPanel(new BorderLayout());
        myWorkPanel = new JPanel(new BorderLayout());

        mainPanel.add(profilePanel, "PROFILE");
        mainPanel.add(tablesPanel, "TABLES");
        mainPanel.add(myWorkPanel, "MY_WORK");

        add(mainPanel, BorderLayout.CENTER);

        // Dogodki
        btnProfile.addActionListener(e -> showCard("PROFILE"));
        btnTables.addActionListener(e -> {
            refreshTables();
            showCard("TABLES");
        });
        btnMyWork.addActionListener(e -> {
            refreshMyWorkPanel(myWorkPanel);
            showCard("MY_WORK");
        });
        btnLogout.addActionListener(e -> System.exit(0));

        showCard("PROFILE");
        setVisible(true);
    }

    private void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(150, 150));
        imageLabel.setMaximumSize(new Dimension(150, 150));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JButton btnUpload = new JButton("Spremeni sliko");
        btnUpload.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUpload.setBackground(new Color(66, 133, 244));
        btnUpload.setForeground(Color.WHITE);

        emailLabel = new JLabel(uporabnikEmail);
        emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        nameLabel = new JLabel();
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        taxNumberLabel = new JLabel();
        taxNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnUpload.addActionListener(e -> izberiInShraniSliko(conn));

        if (!jeAdmin) {
            loadUserDetails();
        }

        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(taxNumberLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(emailLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnUpload);

        osveziProfilnoSliko();

        return panel;
    }

    private void loadUserDetails() {
        try {
            String sql = "SELECT ime, priimek, davcna_stevilka FROM student WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uporabnikEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String ime = rs.getString("ime");
                String priimek = rs.getString("priimek");
                String davcna = rs.getString("davcna_stevilka");
                nameLabel.setText("Ime: " + ime + " " + priimek);
                taxNumberLabel.setText("Davčna številka: " + davcna);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void osveziProfilnoSliko() {
        try {
            String sql = "SELECT osebna_slika FROM student WHERE email = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uporabnikEmail);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                byte[] imageBytes = rs.getBytes("osebna_slika");
                if (imageBytes != null) {
                    ImageIcon icon = new ImageIcon(imageBytes);
                    Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(img));
                } else {
                    imageLabel.setIcon(null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshTables() {
        tablesPanel.removeAll();
        JTabbedPane tabbedPane = new JTabbedPane();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                Statement stmt = conn.createStatement();

                String query;
                if (tableName.equalsIgnoreCase("delo")) {
                    query = "SELECT d.naziv, d.placilo, d.prosta_mesta, del.ime_podjetja AS delodajalec, n.stevilka AS napotnica " +
                            "FROM delo d " +
                            "JOIN delodajalec del ON d.delodajalec_id = del.id " +
                            "JOIN napotnica n ON d.napotnica_id = n.id";
                } else if (!jeAdmin && tableName.equalsIgnoreCase("student")) {
                    query = "SELECT * FROM student WHERE email = '" + uporabnikEmail + "'";
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
                styleTable(table);
                JScrollPane scrollPane = new JScrollPane(table);
                tabbedPane.addTab(tableName, scrollPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablesPanel.add(tabbedPane, BorderLayout.CENTER);
        tablesPanel.revalidate();
        tablesPanel.repaint();
    }

    private void refreshMyWorkPanel(JPanel panel) {
        panel.removeAll();
        try {
            String sql = """
            SELECT d.naziv, d.placilo, d.prosta_mesta, del.ime_podjetja AS delodajalec, 
                   n.stevilka AS napotnica 
            FROM delo d
            JOIN delodajalec del ON d.delodajalec_id = del.id
            JOIN napotnica n ON d.napotnica_id = n.id
            JOIN student s ON d.student_id = s.id
            WHERE s.email = ?
        """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, uporabnikEmail);
            ResultSet rs = stmt.executeQuery();
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
            styleTable(table);
            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        panel.revalidate();
        panel.repaint();
    }


    private void izberiInShraniSliko(Connection conn) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                String sql = "UPDATE student SET osebna_slika = ? WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setBinaryStream(1, fis, (int) selectedFile.length());
                stmt.setString(2, uporabnikEmail);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Slika uspešno naložena.");
                osveziProfilnoSliko();
            } catch (IOException | SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Napaka pri nalaganju slike: " + ex.getMessage());
            }
        }
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
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(66, 133, 244));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
}
