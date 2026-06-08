/*
 * PasswordUtils.java  (utils package)
 * ─────────────────────────────────────────────────────────────────────
 * Lightweight password hashing using SHA-256.
 * No external dependency needed — java.security is in the JDK.
 *
 * For production-grade security, replace with BCrypt (add bcrypt jar).
 * ─────────────────────────────────────────────────────────────────────
 */
package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    private PasswordUtils() { /* utility class */ }

    /**
     * Returns the SHA-256 hex digest of the given plain-text password.
     * Store this hash in the database — never store plain text.
     */
    public static String hash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present in every JRE; this should never happen
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Returns true if the plain-text password matches the stored hash.
     */
    public static boolean matches(String plainPassword, String storedHash) {
        return hash(plainPassword).equalsIgnoreCase(storedHash);
    }

    /**
     * Validates password strength for the Signup screen.
     * Rules shown in Signup_Page.java:
     *   ✓ Minimum 8 characters
     *   ✓ At least one digit (0–9)
     *   ✓ At least one uppercase letter
     *   ✓ At least one special character
     * Returns an error message, or null if the password is valid.
     */
    public static String validate(String password) {
        if (password == null || password.length() < 8)
            return "Password must be at least 8 characters long.";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one number (0-9).";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
            return "Password must contain at least one special character.";
        return null;   // valid
    }
}
