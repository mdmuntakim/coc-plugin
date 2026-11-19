package plugin.clashofclans.net;

/**
 * A failed request to the Clash of Clans API. The status code and the
 * {@code reason} field of the server response are kept, so the UI can explain
 * exactly what went wrong instead of a generic "network error".
 */
public class ApiException extends Exception {

    public final int code;
    public final String reason;

    public ApiException(int code, String reason, String message) {
        super(message);
        this.code = code;
        this.reason = reason == null ? "" : reason;
    }

    /** A short sentence a chief can act on. */
    public String friendly() {
        switch (code) {
            case 400:
                if ("invalidTag".equals(reason)) return "That tag is not valid for this server.";
                return "The server rejected the request (" + reason + ").";
            case 401:
                return "The API token was rejected. Check that you pasted the whole token.";
            case 403:
                if ("accessDenied".equals(reason) || "notAllowed".equals(reason)) {
                    return "The token is not allowed from this IP. Create it with a wider "
                            + "IP range, or route through a proxy base URL.";
                }
                return "The token is not allowed to read this resource.";
            case 404:
                return "Nothing found for that tag. Check the spelling, tags are case sensitive.";
            case 429:
                return "Too many requests. Wait a few seconds and sync again.";
            case 500:
            case 502:
            case 503:
            case 504:
                return "The Clash of Clans API is down or in maintenance. Try again later.";
            default:
                return "The API answered " + code + (reason.isEmpty() ? "" : " (" + reason + ")") + ".";
        }
    }

    public boolean isAuthProblem() {
        return code == 401 || code == 403;
    }

    public boolean isNotFound() {
        return code == 404;
    }

    @Override
    public String toString() {
        return "HTTP " + code + (reason.isEmpty() ? "" : " " + reason) + ": " + getMessage();
    }
}
