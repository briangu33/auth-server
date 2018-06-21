package wya.data;


import org.jetbrains.annotations.Nullable;

public class ResendEmailResponse {

    public @Nullable String message;
    public boolean success; // did the user's pin get set + did the email get sent
    public final @Nullable WyaError wyaError;

    public ResendEmailResponse(String message, boolean success) {
        this.message = message;
        this.wyaError = null;
        this.success = success;
    }

    public ResendEmailResponse(WyaError error) {
        this.message = null;
        this.wyaError = error;
        this.success = false;
    }
}
