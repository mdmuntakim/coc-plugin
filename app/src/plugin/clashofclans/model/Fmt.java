package plugin.clashofclans.model;

import java.util.Locale;

/** Human readable numbers and durations. Pure model. */
public final class Fmt {

    private Fmt() {}

    public static String number(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    /** 3725000 -> "3.7M", 12500 -> "12.5K". */
    public static String shortNumber(long value) {
        if (value >= 1_000_000_000L) return trim(value / 1_000_000_000d) + "B";
        if (value >= 1_000_000L) return trim(value / 1_000_000d) + "M";
        if (value >= 1_000L) return trim(value / 1_000d) + "K";
        return Long.toString(value);
    }

    private static String trim(double v) {
        if (v >= 100 || v == Math.rint(v)) return String.format(Locale.US, "%.0f", v);
        return String.format(Locale.US, "%.1f", v);
    }

    /** Duration in a game-like short form: "3d 4h", "12m 05s", "0s". */
    public static String duration(long millis) {
        if (millis <= 0) return "0s";
        long seconds = millis / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + (minutes < 10 ? "0" : "") + minutes + "m";
        if (minutes > 0) return minutes + "m " + (secs < 10 ? "0" : "") + secs + "s";
        return secs + "s";
    }

    public static String durationSeconds(long seconds) {
        return duration(seconds * 1000L);
    }

    public static String percent(float fraction) {
        return Math.round(fraction * 100f) + "%";
    }
}
