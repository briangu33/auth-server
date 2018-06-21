package wya;

import spark.Route;
import wya.auth.LoginService;
import wya.auth.MailService;
import wya.data.*;

import static spark.Spark.get;
import static spark.Spark.post;

public class ApiController {
    public ApiController() {
        post("api/echo", wrapApiErrors((request, response) -> request.body()));

        post("api/email", wrapApiErrors((request, response) -> {

            EmailTestRequest emailRequest = Util.fromRequest(request, EmailTestRequest.class);

            MailService.sendEmail(emailRequest.emailAddress, emailRequest.subject, emailRequest.content);

            return "nice";
        }), Util.json());

        post("/api/login", wrapApiErrors((request, response) -> {
            try {
                response.type("application/json");
                return LoginService.getInstance().handleLoginRequest(Util.fromRequest(request, LoginRequest.class));
            } catch (Exception ex) {
                return new WyaErrorResponse(WyaError.LoginError, "login error " + ex.getMessage());
            }
        }), Util.json());

        post("/api/resend-email", wrapApiErrors((request, response) -> {
            try {
                response.type("application/json");
                return LoginService.getInstance().resendEmail(Util.fromRequest(request, ResendEmailRequest.class));
            } catch (Exception ex) {
                return new WyaErrorResponse(WyaError.LoginError, "resend email error");
            }
        }), Util.json());

        post("/api/auth", wrapApiErrors((request, response) -> {
            try {
                response.type("application/json");
                return LoginService.getInstance().activateUser(Util.fromRequest(request, AuthRequest.class));
            } catch (Exception ex) {
                return new WyaErrorResponse(WyaError.LoginError, "authentication error");
            }
        }), Util.json());

        get("/api/test", (request, response) -> {
            response.type("application/json");
            return "{\"key1\": \"value1\", \"key2\": \"value2\"}";
        });
    }

    private Route instrument(Route route) {
        return (request, response) -> {
            long startTime = System.nanoTime();
            Object o = route.handle(request, response);
            long finishTime = System.nanoTime();
            long timeDeltaMs = (finishTime - startTime) / 1000000;
            StatsReporter.recordResponseTime(request.pathInfo(), timeDeltaMs);
            return o;
        };
    }

    private Route wrapApiErrors(Route route) {
        return (request, response) -> {
            try {
                return route.handle(request, response);
            } catch (Exception ex) {
                ex.printStackTrace();
                return new WyaErrorResponse(WyaError.GenericError);
            }
        };
    }
}
