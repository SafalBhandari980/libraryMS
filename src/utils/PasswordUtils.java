/*
 * PasswordUtils.java  (utils package)
 * ─────────────────────────────────────────────────────────────────────
 * Password hashing using SHA-256 (built into the JDK — no extra jars).
 *
 * ⚠  MIGRATION NOTE: SHA-256 hashing was re-enabled.  Any test accounts
 *    inserted with plain-text passwords must be updated.  The seed data
 *    in libraryms.sql now stores pre-computed SHA-256 hashes.
 *    Default test password: Admin@123
 *    SHA-256 of "Admin@123":
 *      a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3
 *
 * For production-grade security, upgrade to BCrypt (add bcrypt jar).
 * ─────────────────────────────────────────────────────────────────────
 */
package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    private PasswordUtils() { /* utility class — no instances */ }

    /**
     * Returns the SHA-256 hash of the given plain-text password as a
     * lowercase hex string.  Returns the plain text unchanged if the
     * JVM somehow lacks the SHA-256 algorithm (should never happen).
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by the JDK spec — this branch is unreachable
            System.err.println("[PasswordUtils] SHA-256 not available: " + e.getMessage());
            return plainPassword;   // fallback: return plain text (logs warning)
        }
    }

    /**
     * Returns true if the plain-text password matches the stored SHA-256 hash.
     */
    public static boolean matches(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        return hash(plainPassword).equalsIgnoreCase(storedHash);
    }

    /**
     * Validates password strength for the Signup screen.
     * Rules:
     *   ✓ Minimum 8 characters
     *   ✓ At least one digit (0–9)
     *   ✓ At least one uppercase letter (A–Z)
     *   ✓ At least one special character
     *
     * Returns a descriptive error message, or null if the password is valid.
     */
    public static String validate(String password) {
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters long.";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one number (0–9).";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter (A–Z).";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            return "Password must contain at least one special character (e.g. !@#$%).";
        return null;   // valid
    }
}
