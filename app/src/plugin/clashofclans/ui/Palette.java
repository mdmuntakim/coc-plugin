package plugin.clashofclans.ui;

/**
 * The COC-inspired palette of the app. Warm gold on weathered stone, with the
 * resource colours of the game world (elixir, dark elixir, gems).
 *
 * <p>All of it is drawn from scratch for this project; no artwork, font or
 * sprite of the game is bundled.
 */
public final class Palette {

    public static final int BG = 0xFF0D1116;
    public static final int BG_2 = 0xFF161C24;
    public static final int PANEL_TOP = 0xFF39424E;
    public static final int PANEL_BOTTOM = 0xFF1F262F;
    public static final int PANEL_DEEP = 0xFF171D25;
    public static final int BROWN = 0xFF3B2C1A;
    public static final int TEXT = 0xFFF3EBDD;
    public static final int TEXT_DIM = 0xFFA6B1BE;
    public static final int GREEN = 0xFF6FD98A;
    public static final int RED = 0xFFE2635B;
    public static final int ELIXIR = 0xFFD45FD0;
    public static final int DARK_ELIXIR = 0xFF8B6BE0;
    public static final int GEM = 0xFF57D68A;
    public static final int BLUE = 0xFF63B8E8;
    public static final int PARCHMENT = 0xFFF0DFBC;
    public static final int GOLD = 0xFFF3C65A;

    /** The active accent, changed by the settings screen. */
    public static int accent = 0xFFF3C65A;
    public static int accentLight = 0xFFFFE7A6;
    public static int accentDark = 0xFFA9761F;

    private static final int[][] THEMES = {
            {0xFFF3C65A, 0xFFFFE7A6, 0xFFA9761F}, // royal gold
            {0xFFC88BFF, 0xFFE9D6FF, 0xFF6C3BBF}, // dark elixir
            {0xFF6FD3F0, 0xFFCFF3FF, 0xFF2C7C97}, // ice
    };

    private Palette() {}

    public static void applyAccent(int index) {
        int i = Math.max(0, Math.min(THEMES.length - 1, index));
        accent = THEMES[i][0];
        accentLight = THEMES[i][1];
        accentDark = THEMES[i][2];
    }

    public static String accentName(int index) {
        switch (index) {
            case 1: return "Dark Elixir";
            case 2: return "Ice Crown";
            default: return "Royal Gold";
        }
    }

    public static int currencyColor(int currency) {
        switch (currency) {
            case 1: return ELIXIR;
            case 2: return DARK_ELIXIR;
            default: return accent;
        }
    }
}
