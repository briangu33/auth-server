package wya;

import com.google.gson.*;
import org.joda.time.DateTime;
import wya.data.LatLong;
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

    /**
     * Calculate getDistance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * <p>
     * Shamelessly stolen and slightly modified from the solution posted here:
     * https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     *
     * @returns Distance in Feet
     */
    public static double getDistance(LatLong first, LatLong second) {
        final int R = 6371; // Radius of the earth

        double latDistance = toRadians(second.latitude - first.latitude);
        double lonDistance = toRadians(second.longitude - first.longitude);
        double a = sin(latDistance / 2) * sin(latDistance / 2)
                + cos(toRadians(first.latitude)) * cos(toRadians(second.latitude))
                * sin(lonDistance / 2) * sin(lonDistance / 2);
        double c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return Math.abs(distance * 3.28084);
    }

    // stolen from here: https://github.com/grumlimited/geocalc/blob/master/src/main/java/com/grum/geocalc/EarthCalc.java#L214
    public static double getBearing(LatLong standPoint, LatLong forePoint) {
        /**
         * Formula: θ = atan2( 	sin(Δlong).cos(lat2), cos(lat1).sin(lat2) − sin(lat1).cos(lat2).cos(Δlong) )
         */

        double y = sin(toRadians(forePoint.longitude - standPoint.longitude)) * cos(toRadians(forePoint.latitude));
        double x = cos(toRadians(standPoint.latitude)) * sin(toRadians(forePoint.latitude))
                - sin(toRadians(standPoint.latitude)) * cos(toRadians(forePoint.latitude)) * cos(toRadians(forePoint.longitude - standPoint.longitude));

        double bearing = (atan2(y, x) + 2 * PI) % (2 * PI);

        return toDegrees(bearing);
    }

    /**
     * Determine whether a LatLong pair is within a polygon determined by its hull.
     * Note that this method is based on casting a ray from the point horizontally.
     * It is only accurate for polygons on a Euclidean plane, while the surface of the
     * Earth is curved. This is thus just an approximation.
     * Minimally modified from https://stackoverflow.com/a/38675842.
     *
     * @param p
     * @param hull
     * @return whether the point is in the hull
     */
    public static boolean pointInHull(LatLong p, List<LatLong> hull) {
        int intersections = 0;

        LatLong prev = hull.get(hull.size() - 1);
        for (LatLong next : hull) {
            if ((prev.longitude <= p.longitude && p.longitude < next.longitude) || (prev.longitude >= p.longitude && p.longitude > next.longitude)) {
                double dy = next.longitude - prev.longitude;
                double dx = next.latitude - prev.latitude;
                double x = (p.longitude - prev.longitude) / dy * dx + prev.latitude;
                if (x > p.latitude) {
                    intersections++;
                }
            }
            prev = next;
        }

        return intersections % 2 == 1;
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
