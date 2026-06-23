package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordUtil {

    private PasswordUtil() {}


    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Returns true if the plaintext password matches the stored hash. */
    public static boolean verify(String plaintext, String storedHash) {
        return hash(plaintext).equals(storedHash);
    }
}