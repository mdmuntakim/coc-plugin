package plugin.clashofclans.model;

import java.util.Locale;

/** Player tag handling. Pure model. */
public final class Tags {

    /** The characters a player tag may contain in the game. */
    private static final String ALPHABET = "0289PYLQGRJCUV";

    private Tags() {}

    /** "# 2p0lyq8gr " -> "#2P0LYQ8GR". Returns an empty string when invalid. */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String tag = raw.trim().toUpperCase(Locale.US).replace(" ", "");
        if (!tag.startsWith("#")) tag = "#" + tag;
        if (!isValid(tag)) return "";
        return tag;
    }

    public static boolean isValid(String tag) {
        if (tag == null) return false;
        String t = tag.toUpperCase(Locale.US);
        if (!t.startsWith("#")) return false;
        String body = t.substring(1);
        if (body.length() < 3 || body.length() > 12) return false;
        for (int i = 0; i < body.length(); i++) {
            if (ALPHABET.indexOf(body.charAt(i)) < 0) return false;
        }
        return true;
    }

    /** A tag with a friendlier input error, or the normalized tag. */
    public static String require(String raw) {
        String tag = normalize(raw);
        if (tag.isEmpty()) throw new IllegalArgumentException("That is not a player tag");
        return tag;
    }
}
