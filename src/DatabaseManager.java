import java.sql.*;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://zaklucni-delopst-3439.c.aivencloud.com:21525/defaultdb?sslmode=require";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_28Co5bU1SYV6F4LHOPy";


    // Povezava z bazo
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Preverjanje uporabnika
    public static User prijaviUporabnika(String email, String davcna) {
        String query = "SELECT * FROM student WHERE email = ? AND davcna_stevilka = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, davcna);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("ime"),
                        rs.getString("priimek"),
                        rs.getString("email"),
                        rs.getString("davcna_stevilka")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Uporabnik ni najden
    }
}
