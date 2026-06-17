package storage;

import config.DbConfig;
import model.ConversionRule;
import model.Unit;
import model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Единый класс доступа к БД (вариант 2 ЛР3).
 * Заменяет XmlStorage и UserXmlStorage.
 */
public final class DbStorage {

    /** Таймаут (сек) на проверку «живости» соединения через Connection.isValid(). */
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DbConfig config;
    private Connection connection;

    public DbStorage(DbConfig config) {
        this.config = config;
    }

    /** Открывает соединение с PostgreSQL. */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(
                config.getUrl(),
                config.getUser(),
                config.getPassword()
        );
    }


    private Connection conn() throws SQLException {
        if (!isAlive(connection)) {
            closeQuietly();
            connect();
        }
        return connection;
    }

    /** true, если соединение есть, не закрыто и отвечает на ping-проверку. */
    private static boolean isAlive(Connection c) {
        if (c == null) return false;
        try {
            return !c.isClosed() && c.isValid(VALIDATION_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return false;
        }
    }

    // ==================== USERS ====================

    /**
     * Вставляет нового пользователя в БД.
     * ID генерируется PostgreSQL через SERIAL — возвращаем его обратно в объект.
     *
     * @throws SQLException если login уже занят (unique violation) или другая ошибка БД
     */
    public void insertUser(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPasswordHash());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        }
    }

    /** Находит пользователя по логину. Используется при входе. */
    public Optional<User> findUserByLogin(String login) throws SQLException {
        String sql = "SELECT id, login, password_hash, created_at FROM users WHERE login = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(readUser(rs));
                }
                return Optional.empty();
            }
        }
    }

    /** Возвращает всех пользователей (для подгрузки в память при старте). */
    public List<User> findAllUsers() throws SQLException {
        String sql = "SELECT id, login, password_hash, created_at FROM users";
        List<User> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readUser(rs));
            }
        }
        return result;
    }

    /** Маппинг строки результата в объект User. */
    private User readUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setLogin(rs.getString("login"));
        u.setPasswordHash(rs.getString("password_hash"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toInstant());
        return u;
    }

    // ==================== UNITS ====================

    /**
     * Вставляет единицу измерения.
     * ID и timestamps (created_at, updated_at) генерируются БД.
     * Возвращённые значения пишутся обратно в объект.
     */
    public void insertUnit(Unit unit) throws SQLException {
        String sql = "INSERT INTO units (code, name, owner_id) VALUES (?, ?, ?) " +
                     "RETURNING id, created_at, updated_at";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, unit.getCode());
            ps.setString(2, unit.getName());
            ps.setLong(3, unit.getOwnerId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unit.setId(rs.getLong("id"));
                    unit.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    unit.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        }
    }

    /**
     * Обновляет code и name. updated_at БД проставит сама через NOW().
     */
    public void updateUnit(Unit unit) throws SQLException {
        String sql = "UPDATE units SET code = ?, name = ?, updated_at = NOW() WHERE id = ? " +
                     "RETURNING updated_at";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, unit.getCode());
            ps.setString(2, unit.getName());
            ps.setLong(3, unit.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unit.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        }
    }

    /** Удаляет юнит по id. */
    public void deleteUnit(long id) throws SQLException {
        String sql = "DELETE FROM units WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Возвращает все юниты (для загрузки в память при старте). */
    public List<Unit> findAllUnits() throws SQLException {
        String sql = "SELECT id, code, name, owner_id, created_at, updated_at FROM units";
        List<Unit> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readUnit(rs));
            }
        }
        return result;
    }

    /** Маппинг строки результата в объект Unit. */
    private Unit readUnit(ResultSet rs) throws SQLException {
        Unit u = new Unit();
        u.setId(rs.getLong("id"));
        u.setCode(rs.getString("code"));
        u.setName(rs.getString("name"));
        u.setOwnerId(rs.getLong("owner_id"));
        u.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        u.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return u;
    }

    // ==================== CONVERSION RULES ====================

    /** Вставляет правило конверсии. id/created_at/updated_at пишутся обратно. */
    public void insertRule(ConversionRule rule) throws SQLException {
        String sql = "INSERT INTO conversion_rules " +
                     "(from_unit_code, to_unit_code, factor, owner_id) " +
                     "VALUES (?, ?, ?, ?) " +
                     "RETURNING id, created_at, updated_at";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, rule.getFromUnitCode());
            ps.setString(2, rule.getToUnitCode());
            ps.setDouble(3, rule.getFactor());
            ps.setLong(4, rule.getOwnerId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rule.setId(rs.getLong("id"));
                    rule.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                    rule.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        }
    }

    /** Обновляет factor у правила. Менеджер другие поля менять не разрешает. */
    public void updateRule(ConversionRule rule) throws SQLException {
        String sql = "UPDATE conversion_rules SET factor = ?, updated_at = NOW() WHERE id = ? " +
                     "RETURNING updated_at";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, rule.getFactor());
            ps.setLong(2, rule.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rule.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                }
            }
        }
    }

    /** Удаляет правило по id. */
    public void deleteRule(long id) throws SQLException {
        String sql = "DELETE FROM conversion_rules WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Возвращает все правила (для загрузки в память при старте). */
    public List<ConversionRule> findAllRules() throws SQLException {
        String sql = "SELECT id, from_unit_code, to_unit_code, factor, owner_id, " +
                     "created_at, updated_at FROM conversion_rules";
        List<ConversionRule> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(readRule(rs));
            }
        }
        return result;
    }

    /** Маппинг строки результата в объект ConversionRule. */
    private ConversionRule readRule(ResultSet rs) throws SQLException {
        ConversionRule r = new ConversionRule();
        r.setId(rs.getLong("id"));
        r.setFromUnitCode(rs.getString("from_unit_code"));
        r.setToUnitCode(rs.getString("to_unit_code"));
        r.setFactor(rs.getDouble("factor"));
        r.setOwnerId(rs.getLong("owner_id"));
        r.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        r.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return r;
    }

    /** Закрывает соединение. Безопасно вызывать даже если не было connect(). */
    public void close() {
        closeQuietly();
    }

    /** Тихо закрывает текущее соединение и обнуляет ссылку. Ошибки игнорируются. */
    private void closeQuietly() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // при закрытии игнорируем
            }
            connection = null;
        }
    }
}
