package wya.data;

import org.jetbrains.annotations.NotNull;

public class User {
    public @NotNull String id;
    public @NotNull String displayName;
    public @NotNull String email;

    public User(
            String id,
            String displayName,
            String email) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
    }

    @NotNull
    public String toLogId() {
        return "user " + displayName + " (" + id + ")";
    }
}
