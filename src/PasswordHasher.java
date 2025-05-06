import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Ustvari hashirano geslo (npr. pri registraciji)
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Preveri geslo uporabnika pri prijavi
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}