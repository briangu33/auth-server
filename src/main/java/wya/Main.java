package wya;

import wya.data.User;
import wya.auth.PersistenceLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.port;

public class Main {
    public static List<CommandArgument> commandArguments;

    public static void main(String[] args) {
        init(args);
        begin();
    }

    private static void begin() {
        if (containsArgument(CommandArgument.CreateTestDb)) {
            int amountOfUsers = 1000;

            try {
                PersistenceLayer.initDatabase();
                for (int i = 0; i < amountOfUsers; i++) {
                    String name = "test_" + i;
                    PersistenceLayer.createUser(name);
                }

                List<User> users = PersistenceLayer.getAllUsers();

                System.out.println();
                System.out.print("display_names = [");

                for (User user : users) {
                    System.out.print("'" + user.displayName + "', ");
                }

                System.out.println("]");

                System.out.print("write_tokens = [ ");

                for (User user : users) {
                    System.out.print("'" + user.writeToken + "', ");
                }

                System.out.println("]");

                System.out.print("read_tokens = [ ");

                for (User user : users) {
                    System.out.print("'" + user.readToken + "', ");
                }

                System.out.println("]");


            } catch (Exception e) {
                System.out.println("ERROR");
                e.printStackTrace();
            }

            return;
        }

        Server server = new Server();

        try {

            server.startServer();
        } catch (Exception e) {
            System.out.println("FAILED TO START SERVER");
            e.printStackTrace();
        }
    }

    private static void init(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC"); // necessary for some fucking reason
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Map<String, CommandArgument> commandArgumentMap = new HashMap<>();
        commandArgumentMap.put("--create-test-db", CommandArgument.CreateTestDb);
        commandArgumentMap.put("--use-test-db", CommandArgument.UseTestDb);

        List<CommandArgument> arguments = new ArrayList<>();

        for (String argStr : args) {
            if (commandArgumentMap.containsKey(argStr)) {
                arguments.add(commandArgumentMap.get(argStr));
            }
        }

        commandArguments = arguments;
    }

    public static boolean containsArgument(CommandArgument arg) {
        return commandArguments.stream().anyMatch(a -> a.equals(arg));
    }

    public enum CommandArgument {
        CreateTestDb,
        UseTestDb,
    }
}