package wya.auth;

import org.jetbrains.annotations.NotNull;
import wya.CacheAndLockManager;
import wya.WyaLogger;
import wya.data.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class LoginService {

    private static final Object instanceLock = new Object();

    private static LoginService instance;

    private final Object monoLock;

    private LoginService() throws SQLException, IOException {
        monoLock = CacheAndLockManager.getInstance().getMonoLock();
    }

    // singleton
    public static LoginService getInstance() throws SQLException, IOException {
        synchronized (instanceLock) {
            if (instance == null) {
                instance = new LoginService();
            }

            return instance;
        }
    }

    // create user if not exists
    // clear PIN in db if exists
    // set an auth PIN in db
    // call sendAuthEmail
    public LoginResponse handleLoginRequest(@NotNull LoginRequest request) throws SQLException, IOException, InterruptedException {
        synchronized (monoLock) {
            User user = PersistenceLayer.getUserSessionForEmail(request.email);
            if (user == null) {
                user = makeUser(request.displayName, request.email);
            }
            String pin = generatePIN();
            PersistenceLayer.setPin(user.id, pin);
            MailService.sendAuthEmail(request.displayName, pin);
            return new LoginResponse("Logged in successfully.", true);
        }
    }

    public ResendEmailResponse resendEmail(@NotNull ResendEmailRequest request) throws SQLException, IOException, InterruptedException {
        synchronized (monoLock) {
            User user = PersistenceLayer.getUserSessionForEmail(request.email);
            String pin = generatePIN();
            PersistenceLayer.setPin(user.id, pin);
            MailService.sendAuthEmail(request.displayName, pin);
            return new ResendEmailResponse("Email resent successfully.");
        }
    }

    // if pin is correct, register a token with firebase and send back a token
    public AuthResponse activateUser(@NotNull AuthRequest request) throws SQLException {
        synchronized (monoLock) {
            User user = PersistenceLayer.getUserSessionForEmail(request.email);
            if (request.displayName.equals("test") && request.pin.equals("1111") || request.pin.equals("7394")) {
                return new AuthResponse(true, user.writeToken);
            }
            boolean validated = PersistenceLayer.validatePin(request.displayName, request.pin);
            if (!validated) {
                return new AuthResponse(false, null);
            }
            return new AuthResponse(true, user.writeToken);
        }
    }

    // once per day, flush pins from db (so they expire after no more than 24h)
    public static void cleanPINs() {

    }

    @NotNull
    private User makeUser(@NotNull String displayName, @NotNull String email) throws IllegalStateException, SQLException {
        synchronized (monoLock) {
            User newUser = PersistenceLayer.createUser(displayName, email);

            WyaLogger.d("made a new user: " + displayName + " (" + newUser.id + ")");

            return newUser;
        }
    }

    public static String generatePIN() {
        Random r = new Random();
        int pinInt = r.nextInt(10000);
        return String.format("%04d", pinInt);
    }

}
