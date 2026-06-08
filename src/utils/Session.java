/*
 * Session.java  (utils package)
 * ─────────────────────────────────────────────────────────────────────
 * Holds the currently logged-in member's data for the whole application
 * session (static fields, reset on logout).
 *
 * This is the same single-session approach used in the Fixly project.
 * ─────────────────────────────────────────────────────────────────────
 */
package utils;

public class Session {

    private static int    memberId       = -1;
    private static String email          = "";
    private static String fullName       = "";
    private static String membershipId   = "";
    private static String membershipType = "";  // "student" | "faculty" | "public"
    private static String role           = "";  // "member" | "admin"

    private Session() { /* utility class – no instances */ }

    // ── Setters ──────────────────────────────────────────────────────
    public static void setMemberId(int id)                       { memberId = id; }
    public static void setEmail(String e)                        { email = e; }
    public static void setFullName(String name)                  { fullName = name; }
    public static void setMembershipId(String mid)               { membershipId = mid; }
    public static void setMembershipType(String type)            { membershipType = type; }
    public static void setRole(String r)                         { role = r; }

    // ── Getters ──────────────────────────────────────────────────────
    public static int    getMemberId()       { return memberId; }
    public static String getEmail()          { return email; }
    public static String getFullName()       { return fullName; }
    public static String getMembershipId()   { return membershipId; }
    public static String getMembershipType() { return membershipType; }
    public static String getRole()           { return role; }

    /** Returns true if a member is currently logged in. */
    public static boolean isLoggedIn()       { return memberId > 0; }

    /** Clears all session data (called on logout). */
    public static void clear() {
        memberId       = -1;
        email          = "";
        fullName       = "";
        membershipId   = "";
        membershipType = "";
        role           = "";
    }
}
