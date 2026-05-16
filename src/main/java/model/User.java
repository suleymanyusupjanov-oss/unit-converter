package model;

import java.time.Instant;

public final class User {

    private long id;
    private String login;
    private String passwordHash;
    private Instant createdAt;

    public User() {}

    public User(long id, String login, String passwordHash, Instant createdAt) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }
}
