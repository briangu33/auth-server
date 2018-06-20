package wya.data;


import org.jetbrains.annotations.Nullable;

public class ResendEmailResponse {

    public @Nullable String message;
    public final @Nullable WyaError wyaError;

    public ResendEmailResponse(String message) {
        this.message = message;
        this.wyaError = null;
    }

    public ResendEmailResponse(WyaError error) {
        this.message = null;
        this.wyaError = error;
    }
}
