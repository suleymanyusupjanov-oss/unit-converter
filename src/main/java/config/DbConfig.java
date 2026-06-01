package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Конфигурация подключения к PostgreSQL.
 * Источники (приоритет — сверху вниз):
 *   1) Переменные окружения: DB_URL, DB_USER, DB_PASSWORD
 *   2) Файл db.properties в рабочей директории
 *
 * Требование этапа 6: пароль НЕ должен быть зашит в коде.
 */
public final class DbConfig {

    private static final String CONFIG_FILE = "db.properties";

    private final String url;
    private final String user;
    private final String password;

    private DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl()      { return url; }
    public String getUser()     { return user; }
    public String getPassword() { return password; }

    /**
     * Загружает конфиг. Если файла нет — пытается взять из env vars.
     * Если и там нет — кидает понятную ошибку.
     */
    public static DbConfig load() {
        Properties props = new Properties();
        Path file = Path.of(CONFIG_FILE);

        if (Files.exists(file)) {
            try (FileInputStream in = new FileInputStream(file.toFile())) {
                props.load(in);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Не удалось прочитать " + CONFIG_FILE + ": " + e.getMessage(), e);
            }
        }

        String url      = resolve("DB_URL",      props.getProperty("db.url"));
        String user     = resolve("DB_USER",     props.getProperty("db.user"));
        String password = resolve("DB_PASSWORD", props.getProperty("db.password"));

        if (url == null || user == null || password == null) {
            throw new RuntimeException(
                    "Не заданы настройки БД. Создайте " + CONFIG_FILE +
                    " или установите переменные окружения DB_URL, DB_USER, DB_PASSWORD.");
        }
        return new DbConfig(url, user, password);
    }

    /** Env var имеет приоритет над значением из файла. */
    private static String resolve(String envName, String fileValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) return envValue;
        if (fileValue != null && !fileValue.isBlank()) return fileValue;
        return null;
    }
}
