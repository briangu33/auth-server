package wya.data;

import com.google.gson.annotations.SerializedName;

public enum WyaError {
    @SerializedName("youDontExist") YouDontExist("youDontExist"),
    @SerializedName("targetDoesntExist") TargetDoesntExist("targetDoesntExist"),
    @SerializedName("genericError") GenericError("genericError"),
    @SerializedName("LoginError") LoginError("LoginError"),
    @SerializedName("cannotWyaThisThing") CannotWyaThisThing("cannotWyaThisThing"),
    @SerializedName("teamDoesntExist") TeamDoesntExist("teamDoesntExist");

    String wyaErrorString;

    WyaError(String wyaErrorString) {
        this.wyaErrorString = wyaErrorString;
    }
}
