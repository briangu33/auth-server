package wya.data;

public class ActiveTimespan {
    private long from;
    private long to;

    public ActiveTimespan(long from, long to) {
        this.from = from;
        this.to = to;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }
}
