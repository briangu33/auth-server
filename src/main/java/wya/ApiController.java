package wya;

import spark.Route;
import wya.auth.LoginService;
import wya.auth.MailService;
import wya.data.*;
import wya.game.Game;

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

        post("/api/team-select", wrapApiErrors((request, response) -> {
            try {
                response.type("application/json");
                return Game.getInstance().handleTeamSelect(Util.fromRequest(request, TeamSelectRequest.class));
            } catch (Exception ex) {
                return new WyaErrorResponse(WyaError.TeamDoesntExist, "team select error");
            }
        }), Util.json());

        post("/api/tick", instrument(wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().handleGameUpdate(Util.fromRequest(request, MonoApiRequest.class));
        })), Util.json());

        get("/api/initial-state", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().handleGetInitialStateRequest();
        }), Util.json());

        get("/api/debug", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().getDebugInfo();
        }), Util.json());

        get("/api/notification", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().getNotification();
        }), Util.json());

        post("/api/notification", wrapApiErrors((request, response) -> {
            response.type("application/json");
            Game.getInstance().updateNotification(Util.fromRequest(request, NotificationData.class));

            return "nice";
        }), Util.json());

        post("/api/admin/game-on", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().gameOn(Util.fromRequest(request, GameOnRequest.class));
        }), Util.json());

        post("/api/admin/game-off", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return Game.getInstance().gameOff(Util.fromRequest(request, GameOffRequest.class));
        }), Util.json());

        post("/api/admin/set-auth-require", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return LoginService.getInstance().setRequireAuth(Util.fromRequest(request, RequireAuthRequest.class));
        }), Util.json());

        post("/api/isWyaHour", wrapApiErrors((request, response) -> {
            response.type("application/json");
            return new OldIsWyaHourResponse();
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
