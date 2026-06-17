package storage;

import config.DbConfig;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Доп. задание (вариант 3): real-time синхронизация между клиентами.
 *
 * Слушает изменения в БД через механизм PostgreSQL LISTEN/NOTIFY.
 * Держит ОТДЕЛЬНОЕ соединение (не то, через которое идут запросы UI),
 * подписывается на канал 'lab_changes' командой LISTEN и в фоновом
 * потоке ждёт уведомления. Уведомления присылает БД через триггеры
 * (см. schema.sql) при любом INSERT/UPDATE/DELETE.
 *
 * Когда уведомление приходит — вызываем callback onChange (снаружи он
 * обёрнут в Platform.runLater, чтобы обновить таблицы в потоке JavaFX).
 *
 * Почему отдельное соединение: JDBC Connection не потокобезопасен. Главное
 * соединение работает в потоке JavaFX, а LISTEN должен «висеть» в фоновом
 * потоке — поэтому у слушателя своё соединение.
 */
public final class DbChangeListener {

    /** Имя канала PostgreSQL, в который шлют NOTIFY триггеры из schema.sql. */
    private static final String CHANNEL = "lab_changes";

    /** Сколько ждать уведомление за один цикл (мс), потом цикл повторяется. */
    private static final int POLL_TIMEOUT_MS = 10_000;

    /** Пауза перед попыткой переподключения, если соединение оборвалось (мс). */
    private static final long RECONNECT_DELAY_MS = 3_000;

    private final DbConfig config;
    private final Runnable onChange;

    /** volatile — флаг читается из фонового потока, пишется из потока UI. */
    private volatile boolean running = false;
    private Connection connection;
    private Thread worker;

    public DbChangeListener(DbConfig config, Runnable onChange) {
        this.config = config;
        this.onChange = onChange;
    }

    /** Запускает фоновый поток-слушатель. */
    public void start() {
        running = true;
        worker = new Thread(this::listenLoop, "db-change-listener");
        worker.setDaemon(true);   // daemon: не мешает приложению завершиться
        worker.start();
    }

    /** Главный цикл фонового потока: подписка → ожидание уведомлений. */
    private void listenLoop() {
        while (running) {
            try {
                ensureSubscribed();
                PGConnection pg = connection.unwrap(PGConnection.class);

                // Блокируется до прихода уведомления или истечения таймаута.
                PGNotification[] notifications = pg.getNotifications(POLL_TIMEOUT_MS);

                if (running && notifications != null && notifications.length > 0) {
                    onChange.run();   // снаружи обёрнут в Platform.runLater(...)
                }
            } catch (SQLException e) {
                // БД могла отвалиться (например, её выключили) — переподключимся.
                closeQuietly();
                sleep(RECONNECT_DELAY_MS);
            }
        }
        closeQuietly();
    }

    /** Открывает соединение (если нужно) и выполняет LISTEN на канал. */
    private void ensureSubscribed() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    config.getUrl(), config.getUser(), config.getPassword());
            try (Statement st = connection.createStatement()) {
                st.execute("LISTEN " + CHANNEL);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();   // корректно реагируем на interrupt
        }
    }

    /** Останавливает слушатель и закрывает соединение. Вызывать при закрытии окна. */
    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();   // прерываем возможное ожидание/сон
        }
        closeQuietly();
    }

    /** Тихо закрывает соединение и обнуляет ссылку. Ошибки игнорируются. */
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
