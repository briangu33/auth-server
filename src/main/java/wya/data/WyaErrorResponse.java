package wya.data;

import wya.data.WyaError;

public class WyaErrorResponse {
    public WyaError wyaError = null;
    public String wyaErrorMessage = "";
    public final boolean hasError = true;

    public WyaErrorResponse(WyaError wyaError) {
        this.wyaError = wyaError;
    }

    public WyaErrorResponse(WyaError wyaError, String wyaErrorMessage) {
        this.wyaError = wyaError;
        this.wyaErrorMessage = wyaErrorMessage;
    }
}
