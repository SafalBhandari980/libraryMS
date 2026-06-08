/*
 * OTPService.java  (utils package)
 * ─────────────────────────────────────────────────────────────────────
 * Generates and verifies one-time passwords for the Forgot Password flow.
 * OTPs are stored in memory (HashMap) keyed by email address.
 * They expire after OTP_EXPIRY_MS milliseconds (default 5 minutes).
 *
 * Pattern copied from the Fixly project's OTPService.
 * ─────────────────────────────────────────────────────────────────────
 */
package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OTPService {

    /** OTP validity window: 5 minutes. */
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000L;

    /** In-memory store: email → [otp, timestamp]. */
    private static final Map<String, long[]> otpStore = new HashMap<>();

    private OTPService() { /* utility class */ }

    /**
     * Generates a 6-digit OTP, stores it for the given email, and returns it.
     * The caller is responsible for sending it (via EmailSender).
     */
    public static String generateOTP(String email) {
        int otp = 100_000 + new Random().nextInt(900_000);  // 100000–999999
        otpStore.put(email.toLowerCase().trim(),
                     new long[]{ otp, System.currentTimeMillis() });
        System.out.println("OTPService: OTP generated for " + email + " → " + otp);
        return String.valueOf(otp);
    }

    /**
     * Verifies an OTP for the given email.
     * Returns true if the OTP is correct and has not expired.
     * Removes the OTP from the store on a successful verification.
     */
    public static boolean verifyOTP(String email, String enteredOtp) {
        String key = email.toLowerCase().trim();
        long[] stored = otpStore.get(key);
        if (stored == null) return false;

        long    storedOtp  = stored[0];
        long    storedTime = stored[1];
        boolean expired    = (System.currentTimeMillis() - storedTime) > OTP_EXPIRY_MS;
        boolean match      = String.valueOf(storedOtp).equals(enteredOtp.trim());

        if (match && !expired) {
            otpStore.remove(key);   // consume OTP — can only be used once
            return true;
        }
        if (expired) otpStore.remove(key);
        return false;
    }

    /** Manually invalidates any pending OTP for an email. */
    public static void clearOTP(String email) {
        otpStore.remove(email.toLowerCase().trim());
    }
}
