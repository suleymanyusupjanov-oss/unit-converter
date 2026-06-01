package service;

import model.User;
import storage.DbErrors;
import storage.DbStorage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class    UserManager {

    private final DbStorage db;
    private final List<User> users = new ArrayList<>();
    private User currentUser = null;

    public UserManager(DbStorage db) {
        this.db = db;
    }

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

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(User.hashPassword(password));
        try {
            db.insertUser(user); // id и createdAt проставит БД
        } catch (SQLException e) {
            throw new RuntimeException("Регистрация: " + DbErrors.humanize(e), e);
        }
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

    public java.util.Optional<User> findById(long id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }

    /** Загружает всех пользователей из БД в in-memory кэш. Вызывать при старте. */
    public void loadFromDb() {
        try {
            users.clear();
            users.addAll(db.findAllUsers());
        } catch (SQLException e) {
            throw new RuntimeException("Загрузка пользователей: " + DbErrors.humanize(e), e);
        }
    }
}
