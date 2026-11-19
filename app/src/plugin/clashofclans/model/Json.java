package plugin.clashofclans.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tiny, dependency-free JSON reader.
 *
 * <p>Objects become {@link LinkedHashMap}, arrays {@link ArrayList}, numbers
 * {@link Long} when they are integral and {@link Double} otherwise. The class
 * is pure Java, so the parser is exercised by the plain-JVM test suite as well
 * as on the device.
 */
public final class Json {

    private final String src;
    private int pos;

    private Json(String src) {
        this.src = src;
        this.pos = 0;
    }

    /** Parses one JSON document. Throws {@link IllegalArgumentException} on bad input. */
    public static Object parse(String text) {
        if (text == null) throw new IllegalArgumentException("empty JSON");
        Json p = new Json(text);
        p.skipWs();
        Object value = p.value();
        p.skipWs();
        if (p.pos < p.src.length()) throw p.error("trailing characters");
        return value;
    }

    // ------------------------------------------------------------- parsing
    private Object value() {
        if (pos >= src.length()) throw error("unexpected end of input");
        char c = src.charAt(pos);
        switch (c) {
            case '{':
                return object();
            case '[':
                return array();
            case '"':
                return string();
            case 't':
                expect("true");
                return Boolean.TRUE;
            case 'f':
                expect("false");
                return Boolean.FALSE;
            case 'n':
                expect("null");
                return null;
            default:
                return number();
        }
    }

    private Map<String, Object> object() {
        taken('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWs();
            if (peek() != '"') throw error("expected an object key");
            String key = string();
            skipWs();
            taken(':');
            skipWs();
            map.put(key, value());
            skipWs();
            char c = next();
            if (c == '}') return map;
            if (c != ',') throw error("expected , or }");
        }
    }

    private List<Object> array() {
        taken('[');
        List<Object> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            skipWs();
            list.add(value());
            skipWs();
            char c = next();
            if (c == ']') return list;
            if (c != ',') throw error("expected , or ]");
        }
    }

    private String string() {
        taken('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') return sb.toString();
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = next();
            switch (e) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                case 'u':
                    StringBuilder hex = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        char h = next();
                        if (Character.digit(h, 16) < 0) throw error("bad unicode escape");
                        hex.append(h);
                    }
                    sb.append((char) Integer.parseInt(hex.toString(), 16));
                    break;
                default:
                    throw error("bad escape \\" + e);
            }
        }
    }

    private Object number() {
        int start = pos;
        if (peek() == '-') pos++;
        boolean fractional = false;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c >= '0' && c <= '9') {
                pos++;
            } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                fractional = true;
                pos++;
            } else {
                break;
            }
        }
        String raw = src.substring(start, pos);
        if (raw.isEmpty() || "-".equals(raw)) throw error("expected a number");
        try {
            if (!fractional) return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            // too large for a long, fall through to a double
        }
        return Double.parseDouble(raw);
    }

    private void expect(String word) {
        if (!src.startsWith(word, pos)) throw error("expected " + word);
        pos += word.length();
    }

    private char peek() {
        return pos < src.length() ? src.charAt(pos) : '\0';
    }

    private char next() {
        if (pos >= src.length()) throw error("unexpected end of input");
        return src.charAt(pos++);
    }

    private void taken(char expected) {
        if (next() != expected) throw error("expected " + expected);
    }

    private void skipWs() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') pos++;
            else break;
        }
    }

    private IllegalArgumentException error(String why) {
        return new IllegalArgumentException(why + " at offset " + pos);
    }

    // -------------------------------------------------------------- helpers
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map) return (Map<String, Object>) value;
        return Collections.emptyMap();
    }

    public static List<Object> asList(Object value) {
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            return list;
        }
        return Collections.emptyList();
    }

    /** The nested object at {@code key}, never null. */
    public static Map<String, Object> object(Map<String, Object> map, String key) {
        return asObject(map.get(key));
    }

    /** The nested list at {@code key}, never null. */
    public static List<Object> list(Map<String, Object> map, String key) {
        return asList(map.get(key));
    }

    public static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    public static String str(Map<String, Object> map, String key, String fallback) {
        String v = str(map, key);
        return v.isEmpty() ? fallback : v;
    }

    public static boolean bool(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Boolean) return (Boolean) v;
        if (v instanceof Number) return ((Number) v).doubleValue() != 0;
        return "true".equalsIgnoreCase(String.valueOf(v));
    }

    public static int integer(Map<String, Object> map, String key) {
        return (int) Math.round(dbl(map, key, 0));
    }

    public static int integer(Map<String, Object> map, String key, int fallback) {
        return (int) Math.round(dbl(map, key, fallback));
    }

    public static long number(Map<String, Object> map, String key, long fallback) {
        Object v = map.get(key);
        if (v instanceof Number) return Math.round(((Number) v).doubleValue());
        if (v instanceof String) {
            try {
                return Long.parseLong(((String) v).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    public static double dbl(Map<String, Object> map, String key, double fallback) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String) {
            try {
                return Double.parseDouble(((String) v).trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }
}
