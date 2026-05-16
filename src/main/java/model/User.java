package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

public final class User {

    private long id;
    private String login;
    private String passwordHash;
    private Instant createdAt;

    public User() {}

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 недоступен", e);
        }
    }

    public boolean checkPassword(String plainPassword) {
        return passwordHash.equals(hashPassword(plainPassword));
    }

    public User(long id, String login, String passwordHash, Instant createdAt) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}
