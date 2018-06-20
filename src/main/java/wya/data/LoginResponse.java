package wya.data;


import org.jetbrains.annotations.Nullable;

public class LoginResponse {

    public @Nullable String message;
    public boolean success; // did the user's pin get set + did the email get sent
    public @Nullable WyaError wyaError;

    public LoginResponse(String message, boolean success) {
        this.message = message;
        this.wyaError = null;
        this.success = success;
    }

    public LoginResponse(WyaError error) {
        this.message = null;
        this.wyaError = error;
        this.success = false;
    }
}
