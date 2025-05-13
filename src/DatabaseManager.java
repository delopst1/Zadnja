import java.sql.*;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://zaklucni-delopst-3439.c.aivencloud.com:21525/defaultdb?sslmode=require";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_28Co5bU1SYV6F4LHOPy";

    // Povezava z bazo
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    public static boolean registrirajUporabnika(
            String ime, String priimek, String email, String telefon,
            String emso, String davcna, String bančniRačun, int krajId,
            String hashedPassword
    ) {
        String sql = "INSERT INTO student (ime, priimek, email, telefon, datum_rojstva, emso, davcna_stevilka, " +
                "stevilka_bancnega_racuna, kraj_id, je_admin, geslo) " +
                "VALUES (?, ?, ?, ?, CURRENT_DATE, ?, ?, ?, ?, false, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ime);
            stmt.setString(2, priimek);
            stmt.setString(3, email);
            stmt.setString(4, telefon);
            stmt.setString(5, emso);
            stmt.setString(6, davcna);
            stmt.setString(7, bančniRačun);
            stmt.setInt(8, krajId);
            stmt.setString(9, hashedPassword);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Preveri email in geslo (preko hash)
    public static User prijaviUporabnika(String email, String plainPassword) {
        String query = "SELECT * FROM student WHERE email = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("geslo");

                // Preveri geslo z uporabo bcrypt
                if (PasswordHasher.verifyPassword(plainPassword, hashedPasswordFromDB)) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("ime"),
                            rs.getString("priimek"),
                            rs.getString("email"),
                            hashedPasswordFromDB
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Uporabnik ni najden ali geslo napačno
    }
    public static boolean dodajDelodajalca(String imePodjetja, int stDelavcev) {
        String sql = "{ call dodaj_delodajalca(?, ?) }";  // klic funkcije, ne vstavljaj ID

        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, imePodjetja);
            stmt.setInt(2, stDelavcev);
            stmt.execute();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
