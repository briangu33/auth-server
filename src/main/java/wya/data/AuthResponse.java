package wya.data;


import org.jetbrains.annotations.Nullable;

public class AuthResponse {

    public boolean authenticated;
    public @Nullable String writeToken;
    public final @Nullable WyaError wyaError;

    public AuthResponse(boolean authenticated, String writeToken) {
        this.authenticated = authenticated;
        if (authenticated) {
            this.writeToken = writeToken;
        }
        this.wyaError = null;
    }

    public AuthResponse(boolean authenticated) {
        this.authenticated = authenticated;
        this.writeToken = null;
        this.wyaError = null;
    }

    public AuthResponse(WyaError error) {
        this.authenticated = false;
        this.writeToken = null;
        this.wyaError = error;
    }
}
