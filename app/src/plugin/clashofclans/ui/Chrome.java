package plugin.clashofclans.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import plugin.clashofclans.data.Store;

/** Shared screen furniture: the top bar, the resource strip and the bottom tabs. */
public final class Chrome {

    public interface NavListener {
        void onSelect(int index);
    }

    /** The static top bar plus the resource values that change at runtime. */
    public static final class Header {
        public final LinearLayout view;
        private final TextView[] values;

        Header(LinearLayout view, TextView[] values) {
            this.view = view;
            this.values = values;
        }

        public void refresh(Store st) {
            long[] now = {st.gold(), st.elixir(), st.dark(), st.gems()};
            for (int i = 0; i < values.length && i < now.length; i++) {
                values[i].setText(plugin.clashofclans.model.Fmt.shortNumber(now[i]));
            }
        }
    }

    private Chrome() {}

    public static Header topBar(Activity a, Store st, Runnable onSettings) {
        LinearLayout bar = Widgets.column(a);
        bar.setBackground(Skin.vertical(0xFF1B232D, 0xFF11161D, a, 0f));
        bar.setPadding(0, Skin.dp(a, 10), 0, Skin.dp(a, 10));

        LinearLayout top = Widgets.row(a);
        top.setPadding(Skin.dp(a, 14), 0, Skin.dp(a, 12), 0);

        GlyphView crest = Widgets.glyph(a, GlyphView.CREST, Palette.accent, 34);
        top.addView(crest);

        LinearLayout titles = Widgets.column(a);
        LinearLayout.LayoutParams tp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.leftMargin = Skin.dp(a, 10);
        titles.setLayoutParams(tp);

        TextView title = Widgets.text(a, "COC PLUGIN", 20f, Palette.TEXT, true);
        title.setLetterSpacing(0.11f);
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        title.setShadowLayer(Skin.dp(a, 4), 0, Skin.dp(a, 1), 0xCC000000);
        titles.addView(title);
        TextView sub = Widgets.text(a, "UNOFFICIAL COMPANION  ·  OFFLINE", 9.5f,
                Palette.TEXT_DIM, true);
        sub.setLetterSpacing(0.13f);
        titles.addView(sub);
        top.addView(titles);

        top.addView(Widgets.hSpacer(a));

        GlyphView gear = Widgets.glyph(a, GlyphView.GEAR, Palette.TEXT_DIM, 22);
        gear.setPadding(Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8));
        gear.setBackground(Skin.round(0x22FFFFFF, a, 20f));
        gear.setOnClickListener(v -> onSettings.run());
        top.addView(gear);
        bar.addView(top);

        bar.addView(gap(a, 9));
        TextView[] values = new TextView[4];
        LinearLayout strip = Widgets.row(a);
        strip.setPadding(Skin.dp(a, 14), 0, Skin.dp(a, 14), 0);
        strip.setWeightSum(4f);
        strip.addView(resource(a, GlyphView.GOLD, Palette.accent, st.gold(), values, 0), weight());
        strip.addView(gapView(a, 6));
        strip.addView(resource(a, GlyphView.ELIXIR, Palette.ELIXIR, st.elixir(), values, 1), weight());
        strip.addView(gapView(a, 6));
        strip.addView(resource(a, GlyphView.ELIXIR, Palette.DARK_ELIXIR, st.dark(), values, 2), weight());
        strip.addView(gapView(a, 6));
        strip.addView(resource(a, GlyphView.GEM, Palette.GEM, st.gems(), values, 3), weight());
        bar.addView(strip);
        return new Header(bar, values);
    }

    private static LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private static View resource(Activity a, int glyph, int color, long value,
                                 TextView[] values, int slot) {
        LinearLayout box = Widgets.row(a);
        box.setBackground(Skin.chip(a, color));
        box.setPadding(Skin.dp(a, 7), Skin.dp(a, 6), Skin.dp(a, 8), Skin.dp(a, 6));
        GlyphView g = Widgets.glyph(a, glyph, color, 14);
        box.addView(g);
        TextView t = Widgets.text(a, plugin.clashofclans.model.Fmt.shortNumber(value),
                11.5f, Palette.TEXT, true);
        LinearLayout.LayoutParams p = Widgets.weight(1f);
        p.leftMargin = Skin.dp(a, 5);
        t.setLayoutParams(p);
        t.setSingleLine(true);
        box.addView(t);
        values[slot] = t;
        box.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return box;
    }

    private static View gapView(Activity a, int width) {
        View v = new View(a);
        v.setLayoutParams(Widgets.lp(Skin.dp(a, width), 1));
        return v;
    }

    private static View gap(Context c, int height) {
        View v = new View(c);
        v.setLayoutParams(Widgets.lp(1, Skin.dp(c, height)));
        return v;
    }

    public static View navBar(Activity a, int selected, NavListener listener) {
        LinearLayout nav = Widgets.row(a);
        nav.setBackground(Skin.navBar(a));
        nav.setPadding(0, 0, 0, 0);
        String[] labels = {"Account", "Village", "Army", "War", "Tools"};
        int[] icons = {GlyphView.TROPHY, GlyphView.VILLAGE, GlyphView.ARMY,
                GlyphView.WAR, GlyphView.TOOLS};
        List<View> items = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            LinearLayout item = Widgets.column(a);
            item.setGravity(Gravity.CENTER_HORIZONTAL);
            item.setPadding(0, Skin.dp(a, 8), 0, Skin.dp(a, 8));
            item.setLayoutParams(new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            View indicator = new View(a);
            indicator.setBackground(Skin.round(index == selected ? Palette.accent : 0x00000000,
                    a, 2f));
            LinearLayout.LayoutParams ip = Widgets.lp(Skin.dp(a, 26), Skin.dp(a, 3));
            ip.gravity = Gravity.CENTER_HORIZONTAL;
            indicator.setLayoutParams(ip);
            item.addView(indicator);

            GlyphView glyph = new GlyphView(a).kind(icons[i])
                    .color(index == selected ? Palette.accent : Palette.TEXT_DIM);
            LinearLayout.LayoutParams gp = Widgets.lp(Skin.dp(a, 22), Skin.dp(a, 22));
            gp.topMargin = Skin.dp(a, 5);
            gp.gravity = Gravity.CENTER_HORIZONTAL;
            glyph.setLayoutParams(gp);
            item.addView(glyph);

            TextView t = Widgets.text(a, labels[i], 9.5f,
                    index == selected ? Palette.accent : Palette.TEXT_DIM, true);
            t.setLetterSpacing(0.06f);
            t.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams tp = Widgets.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.topMargin = Skin.dp(a, 3);
            t.setLayoutParams(tp);
            item.addView(t);

            if (index == selected) {
                item.setBackground(Skin.vertical(0x22FFD98A, 0x00FFD98A, a, 0f));
            }
            item.setOnClickListener(v -> listener.onSelect(index));
            items.add(item);
            nav.addView(item);
        }
        return nav;
    }

    // ------------------------------------------------------------ dialogs
    public static EditText input(Context c, String hint, String value, int inputType) {
        EditText e = new EditText(c);
        e.setText(value);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(14f);
        e.setTextColor(Palette.TEXT);
        e.setHintTextColor(0x66A6B1BE);
        e.setInputType(inputType);
        e.setBackground(Skin.inset(c));
        e.setPadding(Skin.dp(c, 10), Skin.dp(c, 9), Skin.dp(c, 10), Skin.dp(c, 9));
        e.setLayoutParams(Widgets.wide());
        return e;
    }

    public static LinearLayout field(Context c, String label, View input) {
        LinearLayout box = Widgets.column(c);
        box.addView(Widgets.label(c, label));
        LinearLayout.LayoutParams p = Widgets.wide();
        p.topMargin = Skin.dp(c, 4);
        box.addView(input, p);
        LinearLayout.LayoutParams bp = Widgets.wide();
        bp.topMargin = Skin.dp(c, 10);
        box.setLayoutParams(bp);
        return box;
    }

    public static ScrollView scrollWrap(Context c, View content) {
        ScrollView sc = new ScrollView(c);
        sc.setFillViewport(true);
        sc.setClipToPadding(false);
        sc.setPadding(Skin.dp(c, 13), Skin.dp(c, 12), Skin.dp(c, 13), Skin.dp(c, 22));
        sc.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return sc;
    }

    public static void toast(Context c, String message) {
        Toast t = Toast.makeText(c, message, Toast.LENGTH_SHORT);
        t.show();
    }

    public static void hideKeyboard(Activity a, View view) {
        InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Tall dialog for the long forms. It is pinned to the top and follows the
     * visible frame, so the Save button stays above the soft keyboard instead
     * of being pushed behind it.
     */
    public static void styleDialogTall(Activity a, android.app.Dialog d) {
        styleDialog(d, a);
        final android.view.Window window = d.getWindow();
        if (window == null) return;
        final int screen = a.getResources().getDisplayMetrics().heightPixels;
        final int margin = Skin.dp(a, 10);
        final int preferred = Math.round(screen * 0.86f);

        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.height = preferred;
        window.setAttributes(lp);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        final View decor = window.getDecorView();
        decor.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visible = new Rect();
            decor.getWindowVisibleDisplayFrame(visible);
            int target = Math.max(Skin.dp(a, 260),
                    Math.min(preferred, visible.height() - margin));
            WindowManager.LayoutParams current = window.getAttributes();
            if (current.height != target) {
                current.height = target;
                window.setAttributes(current);
            }
        });
    }

    public static void styleDialog(android.app.Dialog d, Context c) {
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawable(
                    Skin.vertical(0xFF2A323D, 0xFF161C24, c, 14f));
            d.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            d.getWindow().setDimAmount(0.65f);
        }
    }

    public static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim().replace(",", "").replace(" ", ""));
        } catch (Exception e) {
            return fallback;
        }
    }

    public static long parseLong(String raw, long fallback) {
        try {
            String s = raw.trim().replace(",", "").replace(" ", "").toLowerCase();
            long mult = 1;
            if (s.endsWith("k")) { mult = 1_000; s = s.substring(0, s.length() - 1); }
            else if (s.endsWith("m")) { mult = 1_000_000; s = s.substring(0, s.length() - 1); }
            return (long) (Double.parseDouble(s) * mult);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static int inputNumber() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
    }
}
