package service;

import model.User;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private final List<User> users = new ArrayList<>();
    private long nextId = 1;
    private User currentUser = null;

    public List<User> getUsers() {
        return users;
    }
}
