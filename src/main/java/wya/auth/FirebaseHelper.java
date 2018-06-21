package wya.auth;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import wya.CacheAndLockManager;
import wya.RandomString;
import wya.WyaLogger;
import wya.data.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseHelper {

    private static Firestore db;
    private static Object monoLock = CacheAndLockManager.getInstance().getMonoLock();
    private static final RandomString stringGenerator = new RandomString();

    public static void init(Firestore db) {
        FirebaseHelper.db = db;
    }

    public static void setPin(String userId, String pin) throws ExecutionException, InterruptedException {
        Map<String, Object> update = new HashMap<>();
        update.put("pin", pin);
        synchronized (monoLock) {
            db.collection("EmailPinMap")
                    .document(userId)
                    .set(update, SetOptions.merge())
                    .get();
        }
    }

    public static boolean validatePin(String userId, String pin) throws ExecutionException, InterruptedException {
        DocumentReference docRef = db.collection("EmailPinMap").document(userId);
        synchronized (monoLock) {
            Map<String, Object> doc = docRef.get().get().getData();
            if (pin.equals(doc.get("pin"))) {
                // remove pin field
                Map<String, Object> updates = new HashMap<>();
                updates.put("pin", FieldValue.delete());
                docRef.update(updates).get();
                // create user if doesn't already exist
                createUserIfDoesntExist(userId, (String) doc.get("displayName"), (String) doc.get("email"));
                return true;
            }
            return false;
        }
    }

    public static UserAuth getAuthObjectWithEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference colRef = db.collection("EmailPinMap");
        Query query = colRef.whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        synchronized (monoLock) {
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            Map<String, Object> data = documents.get(0).getData();

            return new UserAuth((String) data.get("id"), (String) data.get("displayName"), (String) data.get("email"));
        }
    }

    public static void createAuthObjectIfDoesntExist(String displayName, String email) throws ExecutionException, InterruptedException {
        CollectionReference colRef = db.collection("EmailPinMap");
        Query query = colRef.whereEqualTo("email", email);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        synchronized (monoLock) {
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();
            if (documents.size() == 0) {
                String uid = stringGenerator.nextString();
                DocumentReference docRef = colRef.document(uid);
                Map<String, Object> data = new HashMap<>();
                data.put("id", uid);
                data.put("displayName", displayName);
                data.put("email", email);
                docRef.set(data).get();
            }
        }
    }

    private static void createUserIfDoesntExist(String uid, String displayName, String email) {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setUid(uid)
                .setEmail(email)
                .setDisplayName(displayName)
                .setEmailVerified(true);
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            return;
        } catch (FirebaseAuthException e) {
            // user with id already exists
            return;
        }
    }
}