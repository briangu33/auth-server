package wya;

import org.jetbrains.annotations.NotNull;
import wya.data.UserAuth;
import wya.auth.FirebaseHelper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CacheAndLockManager {

    private static final Object instanceLock = new Object();

    private static CacheAndLockManager instance;

    private final Object monoLock = new Object();

    private CacheAndLockManager() {

    }

    @NotNull
    public static CacheAndLockManager getInstance() {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new CacheAndLockManager();
            }

            return instance;
        }
    }

    public Object getMonoLock() {
        return this.monoLock;
    }

}
