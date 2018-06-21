package wya;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.ResponseTransformer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Util {
    // dw, this is thread safe since java 7: https://stackoverflow.com/questions/5819638/is-random-class-thread-safe#comment25645331_5819668
    public static final Random GLOBAL_RANDOM = new Random();
    // also thread safe - https://stackoverflow.com/a/10380856
    public static final Gson GLOBAL_GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<DateTime> serializer = (src, typeOfSrc, context) -> new JsonPrimitive(src.getMillis());
        gsonBuilder.registerTypeAdapter(DateTime.class, serializer);
        GLOBAL_GSON = gsonBuilder.create();
    }

    private Util() {

    }

    @NotNull
    public static String json(@Nullable Object obj) {
        return GLOBAL_GSON.toJson(obj);
    }

    public static <T> T fromRequest(Request request, Class<T> tClass) {
        return fromJson(request.body(), tClass);
    }

    public static <T> T fromJson(String json, Class<T> tClass) {
        if (nullOrEmpty(json)) {
            throw new IllegalStateException("cannot deserialize null or empty string");
        }

        return GLOBAL_GSON.fromJson(json, tClass);
    }

    public static ResponseTransformer json() {
        return Util::json;
    }

    @Nullable
    public static <T> T randomChoice(@NotNull List<T> items) {
        if (items.size() == 0) {
            throw new IllegalArgumentException("cannot choose a random item from 0 choices");
        }

        return items.get(GLOBAL_RANDOM.nextInt(items.size()));
    }

    @NotNull
    public static <T> T notNull(@Nullable T val) {
        //noinspection ConstantConditions
        return val;
    }

    public static boolean nullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static Process exec(String command) throws IOException {
        String[] cmd = {
                "/bin/sh",
                "-c",
                command
        };

        return Runtime.getRuntime().exec(cmd);
    }
}
