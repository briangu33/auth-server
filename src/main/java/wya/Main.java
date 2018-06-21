package wya;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {
    public static void main(String[] args) {
        init(args);
        begin();
    }

    private static void begin() {
        Server server = new Server();

        try {
            server.startServer();
        } catch (Exception e) {
            System.out.println("FAILED TO START SERVER");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void init(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC"); // necessary for some fucking reason
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
    }
}