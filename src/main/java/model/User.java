package model;

import java.time.Instant;

public final class User {

    private long id;
    private String login;
    private String passwordHash;
    private Instant createdAt;
}
