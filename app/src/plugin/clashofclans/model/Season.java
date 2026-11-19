package plugin.clashofclans.model;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Season / clan-games clock. Pure model (java.util only, no Android imports).
 *
 * <p>Season resets happen on the first day of each month at 05:00 UTC.
 */
public final class Season {

    public static final int RESET_HOUR_UTC = 5;

    private Season() {}

    public static long millisUntilReset(long nowMillis) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        c.setTimeInMillis(nowMillis);
        Calendar next = (Calendar) c.clone();
        next.set(Calendar.DAY_OF_MONTH, 1);
        next.set(Calendar.HOUR_OF_DAY, RESET_HOUR_UTC);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(c)) {
            next.add(Calendar.MONTH, 1);
        }
        return Math.max(0, next.getTimeInMillis() - nowMillis);
    }

    public static String label(long nowMillis) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        c.setTimeInMillis(nowMillis);
        return String.format(Locale.US, "%tB season", c);
    }
}
