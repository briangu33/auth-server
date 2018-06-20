package wya;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

public class StatsReporter {
    private static final StatsDClient statsd = new NonBlockingStatsDClient(
        "wya",
        "localhost",
        8125
    );

    public static void recordGameUpdate() {
        statsd.incrementCounter("gameUpdate");
    }

    public static void recordResponseTime(String endpoint, long timeDeltaMs) {
        statsd.recordExecutionTime(endpoint, timeDeltaMs);
    }


}
