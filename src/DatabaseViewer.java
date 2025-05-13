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
import javax.swing.table.TableColumn;

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
        JButton btnAllJobs = createSidebarButton("Vsa dela");
        JButton btnMyWork = createSidebarButton("Moja dela");
        JButton btnLogout = createSidebarButton("Odjava");

        // Onemogočimo gumb za "Pregled tabele" za ne-administratorske uporabnike
        if (!jeAdmin) {
            btnTables.setEnabled(false);
        }

        topSidebarPanel.add(btnProfile);
        topSidebarPanel.add(btnTables);
        topSidebarPanel.add(btnMyWork);
        topSidebarPanel.add(btnAllJobs);
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
        JPanel allJobsPanel = new JPanel(new BorderLayout());
        mainPanel.add(allJobsPanel, "ALL_JOBS");
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
        btnAllJobs.addActionListener(e -> {
            refreshAllJobsPanel(allJobsPanel);
            showCard("ALL_JOBS");
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

                // SKRIJ SAMO STOLPEC Z IMENOM "id"
                for (int i = columnNames.size() - 1; i >= 0; i--) {
                    String colName = columnNames.get(i).toLowerCase();
                    if (colName.equals("id")) {
                        TableColumn column = table.getColumnModel().getColumn(i);
                        column.setMinWidth(0);
                        column.setMaxWidth(0);
                        column.setPreferredWidth(0);
                        column.setResizable(false);
                    }
                }

                JPanel tableContainer = new JPanel(new BorderLayout());
                JScrollPane scrollPane = new JScrollPane(table);
                tableContainer.add(scrollPane, BorderLayout.CENTER);

                // GUMBI ZA ADMINA
                // GUMBI ZA ADMINA
                if (jeAdmin) {
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

                    JButton btnAdd = new JButton("Dodaj");
                    JButton btnDelete = new JButton("Izbriši");
                    JButton btnUpdate = new JButton("Posodobi");

                    btnAdd.setBackground(new Color(52, 152, 219));
                    btnAdd.setForeground(Color.WHITE);
                    btnDelete.setBackground(new Color(231, 76, 60));
                    btnDelete.setForeground(Color.WHITE);
                    btnUpdate.setBackground(new Color(241, 196, 15));
                    btnUpdate.setForeground(Color.BLACK);

                    buttonPanel.add(btnAdd);
                    buttonPanel.add(btnDelete);
                    buttonPanel.add(btnUpdate);

                    // Klik na gumb "Dodaj" – prikaže obrazec za dodajanje delodajalca
                    btnAdd.addActionListener(e -> {
                        JTextField imePodjetjaField = new JTextField(20);
                        JTextField stDelavcevField = new JTextField(5);

                        JPanel formPanel = new JPanel(new GridLayout(0, 2));
                        formPanel.add(new JLabel("Ime podjetja:"));
                        formPanel.add(imePodjetjaField);
                        formPanel.add(new JLabel("Število delavcev:"));
                        formPanel.add(stDelavcevField);

                        int result = JOptionPane.showConfirmDialog(
                                null,
                                formPanel,
                                "Dodaj novega delodajalca",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE
                        );

                        if (result == JOptionPane.OK_OPTION) {
                            String imePodjetja = imePodjetjaField.getText().trim();
                            int stDelavcev = 0;
                            try {
                                stDelavcev = Integer.parseInt(stDelavcevField.getText().trim());
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Število delavcev mora biti celo število.");
                                return;
                            }

                            // Klic funkcije v bazi
                            if (DatabaseManager.dodajDelodajalca(imePodjetja, stDelavcev)) {
                                JOptionPane.showMessageDialog(null, "Delodajalec uspešno dodan.");
                                // osveziTabelo(tableName, table); // če uporabljaš JTable
                            } else {
                                JOptionPane.showMessageDialog(null, "Napaka pri dodajanju delodajalca.");
                            }
                        }
                    });

                    tableContainer.add(buttonPanel, BorderLayout.SOUTH);
                }


                tabbedPane.addTab(tableName, tableContainer);
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
        SELECT d.id, d.naziv, d.placilo, d.prosta_mesta, del.ime_podjetja AS delodajalec, 
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

            // 🔽 SKRIJ STOLPEC "id"
            for (int i = columnNames.size() - 1; i >= 0; i--) {
                String colName = columnNames.get(i).toLowerCase();
                if (colName.equals("id")) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    column.setMinWidth(0);
                    column.setMaxWidth(0);
                    column.setPreferredWidth(0);
                    column.setResizable(false);
                }
            }

            JScrollPane scrollPane = new JScrollPane(table);
            panel.add(scrollPane, BorderLayout.CENTER);

            // Gumb za odjavo
            JButton btnOdjavi = new JButton("Odjavi se z izbranega dela");
            btnOdjavi.setBackground(new Color(231, 76, 60));
            btnOdjavi.setForeground(Color.WHITE);
            btnOdjavi.setFont(new Font("Segoe UI", Font.BOLD, 14));

            btnOdjavi.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int deloId = (int) table.getValueAt(selectedRow, 0);
                    odjaviIzDela(deloId);
                    refreshMyWorkPanel(panel);
                } else {
                    JOptionPane.showMessageDialog(this, "Izberi vrstico za odjavo.");
                }
            });

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.add(btnOdjavi);
            panel.add(bottomPanel, BorderLayout.SOUTH);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        panel.revalidate();
        panel.repaint();
    }


    private void refreshAllJobsPanel(JPanel panel) {
        panel.removeAll();
        try {
            String sql = """
            SELECT d.id, d.naziv, d.placilo, d.prosta_mesta, del.ime_podjetja AS delodajalec, 
                   n.stevilka AS napotnica, d.student_id
            FROM delo d
            JOIN delodajalec del ON d.delodajalec_id = del.id
            JOIN napotnica n ON d.napotnica_id = n.id
        """;

            PreparedStatement stmt = conn.prepareStatement(sql);
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

// Skrij stolpec 'id' (če obstaja)
            for (int i = columnNames.size() - 1; i >= 0; i--) {
                String colName = columnNames.get(i).toLowerCase();
                if (colName.equals("id")) {
                    TableColumn column = table.getColumnModel().getColumn(i);
                    column.setMinWidth(0);
                    column.setMaxWidth(0);
                    column.setPreferredWidth(0);
                    column.setResizable(false);
                }
            }

            styleTable(table);


            JButton btnPrijavi = new JButton("Prijavi se na izbrano delo");
            btnPrijavi.setBackground(new Color(46, 204, 113));
            btnPrijavi.setForeground(Color.WHITE);
            btnPrijavi.setFont(new Font("Segoe UI", Font.BOLD, 14));

            btnPrijavi.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int deloId = (int) table.getValueAt(selectedRow, 0);
                    prijaviNaDelo(deloId);
                    refreshAllJobsPanel(panel);
                } else {
                    JOptionPane.showMessageDialog(this, "Izberi vrstico za prijavo.");
                }
            });

            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.add(btnPrijavi);

            panel.add(new JScrollPane(table), BorderLayout.CENTER);
            panel.add(bottomPanel, BorderLayout.SOUTH);
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
            try (FileInputStream







                         fis = new FileInputStream(selectedFile)) {
                byte[] imageBytes = fis.readAllBytes();


                String sql = "UPDATE student SET osebna_slika = ? WHERE email = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setBytes(1, imageBytes);
                stmt.setString(2, uporabnikEmail);
                stmt.executeUpdate();

                osveziProfilnoSliko();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean jeUporabnikAdmin(Connection conn, String email) {
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT je_admin FROM student WHERE email = ?");
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("je_admin");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void prijaviNaDelo(int deloId) {
        try {
            // 1. Pridobimo ID študenta
            PreparedStatement getStudentId = conn.prepareStatement("SELECT id FROM student WHERE email = ?");
            getStudentId.setString(1, uporabnikEmail);
            ResultSet rs = getStudentId.executeQuery();
            if (rs.next()) {
                int studentId = rs.getInt("id");

                // 2. Preverimo, če je delo že zasedeno
                PreparedStatement checkIfTaken = conn.prepareStatement("SELECT student_id FROM delo WHERE id = ?");
                checkIfTaken.setInt(1, deloId);
                ResultSet takenRS = checkIfTaken.executeQuery();
                if (takenRS.next()) {
                    Integer assignedStudent = takenRS.getInt("student_id");
                    if (assignedStudent != 0) {
                        JOptionPane.showMessageDialog(this, "To delo je že zasedeno!", "Napaka", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 3. Preverimo, na koliko delih je že prijavljen
                PreparedStatement countJobs = conn.prepareStatement("SELECT COUNT(*) AS count FROM delo WHERE student_id = ?");
                countJobs.setInt(1, studentId);
                ResultSet countRS = countJobs.executeQuery();
                if (countRS.next()) {
                    int currentCount = countRS.getInt("count");
                    if (currentCount >= 2) {
                        JOptionPane.showMessageDialog(this, "Ne moreš biti prijavljen na več kot 2 dela!", "Omejitev", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // 4. Če ni zasedeno in ima dovolj prostora, ga prijavimo
                PreparedStatement updateDelo = conn.prepareStatement("UPDATE delo SET student_id = ? WHERE id = ?");
                updateDelo.setInt(1, studentId);
                updateDelo.setInt(2, deloId);
                updateDelo.executeUpdate();

                JOptionPane.showMessageDialog(this, "Uspešno si se prijavil na delo.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setMargin(new Insets(10, 15, 10, 15));
        return button;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setGridColor(new Color(230, 230, 230));
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(66, 133, 244));
        header.setForeground(Color.WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }
    private void odjaviIzDela(int deloId) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE delo SET student_id = NULL WHERE id = ?");
            stmt.setInt(1, deloId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Uspešno si se odjavil z dela.");
            } else {
                JOptionPane.showMessageDialog(this, "Napaka pri odjavi z dela.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}