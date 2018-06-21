package wya;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import spark.Spark;
import wya.auth.FirebaseHelper;

import java.io.FileInputStream;
import java.io.InputStream;

import static spark.Spark.*;

public class Server {
    private final int PORT = 4000;
    private ApiController apiController;

    public void startServer() throws Exception {
        WyaLogger.d("starting server...");

        String staticDirectory = System.getProperty("user.dir") + "/http_static";
        WyaLogger.d(staticDirectory);

        Spark.externalStaticFileLocation(staticDirectory);

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

        WyaLogger.d("initializing Firebase SDK...");
        String keyPath = System.getProperty("user.dir") + "/wya-mit-firebase-adminsdk-kx6a7-3f0e9200f6.json";
        InputStream serviceAccount = new FileInputStream(keyPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build();
        FirebaseApp.initializeApp(options);

        Firestore db = FirestoreClient.getFirestore();

        FirebaseHelper.init(db);

        apiController = new ApiController();
    }
}
