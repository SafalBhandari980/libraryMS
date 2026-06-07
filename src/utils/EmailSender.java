/*
 * EmailSender.java  (utils package)
 * ─────────────────────────────────────────────────────────────────────
 * Sends OTP emails via Gmail SMTP using the JavaMail API.
 *
 * CONFIGURATION: set SENDER_EMAIL and SENDER_APP_PASSWORD below.
 * For Gmail, generate an App Password at myaccount.google.com/apppasswords
 * (requires 2-Step Verification enabled on the sending account).
 *
 * Pattern adapted from the Fixly project's EmailSender.
 * ─────────────────────────────────────────────────────────────────────
 */
package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    // ── CONFIGURATION ────────────────────────────────────────────────
    private static final String SENDER_EMAIL        = "libraryms.noreply@gmail.com"; // change
    private static final String SENDER_APP_PASSWORD = "your_app_password_here";      // change
    private static final String SMTP_HOST           = "smtp.gmail.com";
    private static final String SMTP_PORT           = "587";
    // ─────────────────────────────────────────────────────────────────

    private EmailSender() { /* utility class */ }

    /**
     * Generates an OTP via OTPService and sends it to the given email address.
     * Throws MessagingException on SMTP failure.
     */
    public static void sendOTP(String recipientEmail) throws MessagingException {
        String otp = OTPService.generateOTP(recipientEmail);
        String subject = "LibraryMS – Your OTP Code";
        String body =
            "Hello,\n\n" +
            "You requested a password reset for your LibraryMS account.\n\n" +
            "Your One-Time Password (OTP) is:\n\n" +
            "    " + otp + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "If you did not request this, please ignore this email.\n\n" +
            "– The LibraryMS Team";
        send(recipientEmail, subject, body);
    }

    /**
     * Core send method.
     * Uses TLS on port 587.
     */
    private static void send(String to, String subject, String body)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            SMTP_PORT);

        javax.mail.Session session = javax.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
        System.out.println("EmailSender: OTP email sent to " + to);
    }
}
