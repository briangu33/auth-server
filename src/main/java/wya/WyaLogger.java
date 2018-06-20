package wya;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;

import java.util.Date;

public class WyaLogger {
    private static final ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
            .foreground(Ansi.FColor.GREEN)
            .build();

    private WyaLogger() {

    }

    public static void d(String log) {
        cp.println((new Date().toString()) + " " + log);
    }
}
