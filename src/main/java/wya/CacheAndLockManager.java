package wya;

import org.jetbrains.annotations.NotNull;
import wya.data.User;
import wya.auth.PersistenceLayer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CacheAndLockManager {

    private static final Object instanceLock = new Object();

    private static CacheAndLockManager instance;

    private final Object userCacheLock = new Object();
    private final List<User> userCache;
    private final Object monoLock = new Object();

    private CacheAndLockManager() throws SQLException, IOException {
        PersistenceLayer.setInitialState();

        userCache = PersistenceLayer.getAllUsers();
    }

    @NotNull
    public static CacheAndLockManager getInstance() throws SQLException, IOException {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new CacheAndLockManager();
            }

            return instance;
        }
    }

    public Object getUserCacheLock() {
        return this.userCacheLock;
    }

    public Object getMonoLock() {
        return this.monoLock;
    }

    public List<User> getUserCache() {
        return this.userCache;
    }

}
