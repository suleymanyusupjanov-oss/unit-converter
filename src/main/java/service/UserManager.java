package service;

import model.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private final List<User> users = new ArrayList<>();
    private long nextId = 1;
    private User currentUser = null;

    public List<User> getUsers() {
        return users;
    }

    public User register(String login, String password) {
        if (login == null || login.isBlank())
            throw new IllegalArgumentException("Логин не может быть пустым");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Пароль не может быть пустым");
        for (User u : users) {
            if (u.getLogin().equals(login))
                throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }
        User user = new User(nextId++, login, User.hashPassword(password), Instant.now());
        users.add(user);
        return user;
    }

    public User login(String login, String password) {
        for (User u : users) {
            if (u.getLogin().equals(login) && u.checkPassword(password)) {
                currentUser = u;
                return u;
            }
        }
        throw new IllegalArgumentException("Неверный логин или пароль");
    }

    public User getCurrentUser() { return currentUser; }

    public boolean isLoggedIn() { return currentUser != null; }

    public void logout() { currentUser = null; }

    public void setUsers(List<User> loaded) {
        users.clear();
        users.addAll(loaded);
        long maxId = loaded.stream().mapToLong(User::getId).max().orElse(0);
        nextId = maxId + 1;
    }
}
