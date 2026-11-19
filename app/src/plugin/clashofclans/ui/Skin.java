package plugin.clashofclans.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

/** Drawables and measurements of the themed chrome. */
public final class Skin {

    private Skin() {}

    public static int dp(Context c, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, c.getResources().getDisplayMetrics()));
    }

    public static GradientDrawable round(int color, Context c, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    public static GradientDrawable stroke(int fill, int stroke, Context c,
                                          float radiusDp, float strokeDp) {
        GradientDrawable d = round(fill, c, radiusDp);
        d.setStroke(dp(c, strokeDp), stroke);
        return d;
    }

    public static GradientDrawable vertical(int top, int bottom, Context c, float radiusDp) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{top, bottom});
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    /** Stone panel with a forged gold rim. */
    public static Drawable panel(Context c) {
        GradientDrawable rim = vertical(Palette.accent, Palette.accentDark, c, 13f);
        GradientDrawable body = vertical(Palette.PANEL_TOP, Palette.PANEL_BOTTOM, c, 11f);
        LayerDrawable layers = new LayerDrawable(new Drawable[]{rim, body});
        int inset = dp(c, 2);
        layers.setLayerInset(1, inset, inset, inset, inset);
        return layers;
    }

    /** Header strip of a panel, square at the bottom, rounded at the top. */
    public static Drawable header(Context c) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Palette.accentDark, Palette.accent, Palette.accentDark});
        float r = dp(c, 10);
        d.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return d;
    }

    public static Drawable goldButton(Context c, float radiusDp) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = vertical(Palette.accentDark, Palette.accent, c, radiusDp);
        pressed.setStroke(dp(c, 1.5f), 0xFF3C2A0E);
        GradientDrawable normal = vertical(Palette.accentLight, Palette.accent, c, radiusDp);
        normal.setStroke(dp(c, 1.5f), 0xFF7A5417);
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, normal);
        states.addState(new int[0], vertical(0xFF4B535E, 0xFF333A44, c, radiusDp));
        return states;
    }

    public static Drawable stoneButton(Context c, float radiusDp) {
        StateListDrawable states = new StateListDrawable();
        GradientDrawable pressed = vertical(0xFF232A33, 0xFF39424E, c, radiusDp);
        pressed.setStroke(dp(c, 1.2f), Palette.accentDark);
        GradientDrawable normal = vertical(0xFF4A5563, 0xFF2A323C, c, radiusDp);
        normal.setStroke(dp(c, 1.2f), 0xFF7C8794);
        states.addState(new int[]{android.R.attr.state_pressed}, pressed);
        states.addState(new int[]{android.R.attr.state_enabled}, normal);
        states.addState(new int[0], vertical(0xFF3B434E, 0xFF262D36, c, radiusDp));
        return states;
    }

    public static GradientDrawable chip(Context c, int color) {
        GradientDrawable d = round(withAlpha(color, 38), c, 20f);
        d.setStroke(dp(c, 1), withAlpha(color, 190));
        return d;
    }

    public static GradientDrawable inset(Context c) {
        GradientDrawable d = round(0x66000000, c, 8f);
        d.setStroke(dp(c, 1), 0x337C8794);
        return d;
    }

    /** The app backdrop: a cold stone gradient with a gold glow at the top. */
    public static Drawable backdrop(Context c) {
        GradientDrawable base = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF18202A, Palette.BG, 0xFF080B0F});
        GradientDrawable glow = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{withAlpha(Palette.accent, 46), Color.TRANSPARENT});
        LayerDrawable layers = new LayerDrawable(new Drawable[]{base, glow});
        layers.setLayerHeight(1, dp(c, 260));
        layers.setLayerGravity(1, android.view.Gravity.TOP);
        return layers;
    }

    public static Drawable navBar(Context c) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF161D25, 0xFF0B0F14});
        d.setStroke(dp(c, 1), 0xFF2C3540);
        return d;
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((Math.max(0, Math.min(255, alpha)) & 0xFF) << 24);
    }
}
