package wya;

import spark.Spark;
import wya.game.Game;
import wya.auth.PersistenceLayer;

import static spark.Spark.*;

public class Server {
    private final int PORT = 4000;
    private ApiController apiController;

    public void startServer() throws Exception {
        WyaLogger.d("starting server...");

        String staticDirectory = System.getProperty("user.dir") + "/http_static";
        WyaLogger.d(staticDirectory);

        Spark.externalStaticFileLocation(staticDirectory);

        PersistenceLayer.initDatabase();

        WyaLogger.d("starting on port: " + PORT);
        port(PORT);

        before((req, res) -> {
            String path = req.pathInfo();
            if (path.endsWith("/") && !path.equals("/")) {
                res.redirect(path.substring(0, path.length() - 1));
                return;
            }

            WyaLogger.d("[" + req.requestMethod().toUpperCase() + "] " + req.uri());
        });

        get("/", (req, res) -> "Hello World");

        internalServerError((req, res) -> {
            res.type("application/json");
            return "{\"errorMessage\":\"500: server error\"}";
        });

        apiController = new ApiController();

        Game.getInstance();
    }
}
