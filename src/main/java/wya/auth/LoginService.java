package wya.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.jetbrains.annotations.NotNull;
import wya.CacheAndLockManager;
import wya.WyaLogger;
import wya.data.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class LoginService {

    private static final Object instanceLock = new Object();

    private static LoginService instance;

    private LoginService() {
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
    public LoginResponse handleLoginRequest(@NotNull LoginRequest request) throws IOException, InterruptedException, ExecutionException {
        FirebaseHelper.createAuthObjectIfDoesntExist(request.displayName, request.email);
        UserAuth userAuth = FirebaseHelper.getAuthObjectWithEmail(request.email);
        String pin = generatePIN();
        FirebaseHelper.setPin(userAuth.id, pin);
        MailService.sendAuthEmail(userAuth.displayName, userAuth.email, pin);

        return new LoginResponse("Logged in successfully.", true);

    }

    public ResendEmailResponse resendEmail(@NotNull ResendEmailRequest request) throws IOException, InterruptedException, ExecutionException {
        UserAuth userAuth = FirebaseHelper.getAuthObjectWithEmail(request.email);
        String pin = generatePIN();
        FirebaseHelper.setPin(userAuth.id, pin);
        MailService.sendAuthEmail(userAuth.displayName, userAuth.email, pin);
        return new ResendEmailResponse("Email resent successfully.", true);
    }

    // if pin is correct, register a token with firebase and send back a token
    public AuthResponse activateUser(@NotNull AuthRequest request) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserAuth userAuth = FirebaseHelper.getAuthObjectWithEmail(request.email);
        boolean validated = FirebaseHelper.validatePin(userAuth.id, request.pin);
        if (!validated) {
            return new AuthResponse(false, null);
        }
        String firebaseToken = FirebaseAuth.getInstance().createCustomToken(userAuth.id);
        return new AuthResponse(true, firebaseToken);
    }

    public static String generatePIN() {
        Random r = new Random();
        int pinInt = r.nextInt(10000);
        return String.format("%04d", pinInt);
    }

}
