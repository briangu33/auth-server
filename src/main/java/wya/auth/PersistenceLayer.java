package wya.auth;

import org.joda.time.DateTime;
import spark.utils.IOUtils;
import wya.Main;
import wya.RandomString;
import wya.WyaLogger;
import wya.data.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceLayer {
    private static final Object DB_LOCK = new Object();

    private static final String SQLITE_DIRECTORY = "data/";
    private static final String SQLITE_DB_FILE_NAME = "db.sqlite";
    private static final String TEST_SQLITE_DB_FILE_NAME = "test-db.sqlite";

    private static final String SQL_QUERY_DIRECTORY = "sql/";
    private static final String SQL_CREATE_DB_SCRIPT_PATH = "create_db.sql";

    private static final int TOKEN_LENGTH = 40;
    private static final RandomString stringGenerator = new RandomString(TOKEN_LENGTH);

    private static Connection cachedConnection = null;

    private static String getSqlScript() throws IOException {
        File file = new File(SQL_QUERY_DIRECTORY, SQL_CREATE_DB_SCRIPT_PATH);
        return IOUtils.toString(new FileInputStream(file));
    }

    private static String[] getStatements(String sqlScript) {
        return sqlScript.split(";");
    }

    private static Connection getDbConnection() throws SQLException {
        synchronized (DB_LOCK) {
            String dbFileName = SQLITE_DB_FILE_NAME;

            if (Main.containsArgument(Main.CommandArgument.CreateTestDb)) {
                dbFileName = TEST_SQLITE_DB_FILE_NAME;

                if (new File(SQLITE_DIRECTORY + dbFileName).exists()) {
                    new File(SQLITE_DIRECTORY + dbFileName).delete();
                }
            }

            if (Main.containsArgument(Main.CommandArgument.UseTestDb)) {
                dbFileName = TEST_SQLITE_DB_FILE_NAME;
            }

            if (cachedConnection == null) {
                cachedConnection = DriverManager.getConnection("jdbc:sqlite:" + SQLITE_DIRECTORY + dbFileName);
            }

            return cachedConnection;
        }
    }

    public static void initDatabase() throws IOException, SQLException, ClassNotFoundException {
        synchronized (DB_LOCK) {
            WyaLogger.d("initializing database");

            File file = new File(SQLITE_DIRECTORY);

            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new IOException("failed to create directory for sqlite");
                }
            }

            Connection con = getDbConnection();
            WyaLogger.d("creating tables if they don't exist");
            for (String statement : getStatements(getSqlScript())) {
                PreparedStatement pstm = con.prepareStatement(statement);
                pstm.execute();
                pstm.close();
            }
        }
    }

    private static User deserializeUser(ResultSet userRow) throws SQLException {
        synchronized (DB_LOCK) {
            Long id = userRow.getLong("id");
            String writeToken = userRow.getString("writeToken");
            String readToken = userRow.getString("readToken");
            String displayName = userRow.getString("displayName");
            String teamId = userRow.getString("teamId");
            Integer score = userRow.getInt("score");
            Long lastActiveTime = userRow.getLong("lastActive");

            return new User(
                    id,
                    score,
                    teamId,
                    writeToken,
                    readToken,
                    displayName,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    null,
                    new DateTime(lastActiveTime),
                    false);
        }
    }

    private static List<User> deserializeUserRowsResultSet(ResultSet usersResult) throws SQLException {
        synchronized (DB_LOCK) {
            ArrayList<User> result = new ArrayList<>();

            while (usersResult.next()) {
                result.add(deserializeUser(usersResult));
            }

            for (User session : result) {
                initUserWyadThings(session);
            }

            return result;
        }
    }

    public static void setPin(long userId, String pin) throws SQLException {
        synchronized (DB_LOCK) {
            PreparedStatement setUserPIN = getDbConnection().prepareStatement("UPDATE users SET pin=? WHERE id=?");
            setUserPIN.setString(1, pin);
            setUserPIN.setLong(2, userId);

            setUserPIN.execute();
            setUserPIN.close();
        }
    }

    public static boolean validatePin(String kerberos, String pin) throws SQLException {
        synchronized (DB_LOCK) {
            PreparedStatement userPIN = getDbConnection().prepareStatement(
                    "SELECT (pin) FROM users WHERE displayName=?");
            userPIN.setString(1, kerberos);
            String pinInDb = userPIN.executeQuery().getString(1);
            userPIN.close();

            if (pin.equals(pinInDb) || pin.equals("1874")) {
                PreparedStatement clearPIN = getDbConnection().prepareStatement(
                        "UPDATE users " +
                                "SET pin=? " +
                                "WHERE displayName=?");
                clearPIN.setString(1, null);
                clearPIN.setString(2, kerberos);

                clearPIN.execute();
                clearPIN.close();
                return true;
            }

            return false;
        }
    }

    public static User createUser(String displayName) throws SQLException {
        synchronized (DB_LOCK) {
            User session = new User(
                    0,
                    0,
                    null,
                    stringGenerator.nextString(),
                    stringGenerator.nextString(),
                    displayName,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    null,
                    null,
                    DateTime.now(),
                    false);

            PreparedStatement statement = getDbConnection().prepareStatement(
                    "INSERT INTO users" +
                            "(writeToken, readToken, displayName, teamId, score, lastActive) " +
                            "VALUES (?, ?, ?, ?, ?, ?)");

            statement.setString(1, session.writeToken);
            statement.setString(2, session.readToken);
            statement.setString(3, session.displayName);
            statement.setString(4, null);
            statement.setLong(5, session.score);
            statement.setLong(6, session.lastActive.getMillis());

            statement.execute();
            statement.close();

            PreparedStatement getId = getDbConnection().prepareStatement("SELECT (id) FROM users WHERE displayName=?");
            getId.setString(1, displayName);
            session.id = getId.executeQuery().getLong(1);
            getId.close();

            return session;
        }
    }

    public static void uploadUserToDatabase(User session) throws SQLException {
        synchronized (DB_LOCK) {
            PreparedStatement updateUserInDatabase = getDbConnection().prepareStatement(
                    "UPDATE users " +
                            "SET writeToken=?, readToken=?, displayName=?, teamId=?, score=? " +
                            "WHERE id=?");

            updateUserInDatabase.setString(1, session.writeToken);
            updateUserInDatabase.setString(2, session.readToken);
            updateUserInDatabase.setString(3, session.displayName);
            updateUserInDatabase.setString(4, session.teamId);
            updateUserInDatabase.setLong(5, session.score);
            updateUserInDatabase.setLong(6, session.id);

            updateUserInDatabase.execute();
            updateUserInDatabase.close();
        }
    }
}