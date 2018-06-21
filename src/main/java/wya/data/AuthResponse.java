package wya.data;


import org.jetbrains.annotations.Nullable;

public class AuthResponse {

    public boolean authenticated;
    public @Nullable String token;
    public final @Nullable WyaError wyaError;

    public AuthResponse(boolean authenticated, String token) {
        this.authenticated = authenticated;
        if (authenticated) {
            this.token = token;
        }
        this.wyaError = null;
    }

    public AuthResponse(boolean authenticated) {
        this.authenticated = authenticated;
        this.token = null;
        this.wyaError = null;
    }

    public AuthResponse(WyaError error) {
        this.authenticated = false;
        this.token = null;
        this.wyaError = error;
    }
}
