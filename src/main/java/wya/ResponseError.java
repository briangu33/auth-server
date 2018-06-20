package wya;

import spark.Response;

public class ResponseError {
    public final String errorMessage;

    public ResponseError(Response response, int httpCode, String errorMessage) {
        response.type("application/json");
        response.status(httpCode);
        this.errorMessage = errorMessage;
    }
}
