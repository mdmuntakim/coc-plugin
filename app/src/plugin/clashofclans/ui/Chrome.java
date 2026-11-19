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

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Fmt;

/** Shared screen furniture: the top bar, the live account strip and the tabs. */
public final class Chrome {

    public interface NavListener {
        void onSelect(int index);
    }

    /** The static top bar plus the live status line that changes at runtime. */
    public static final class Header {
        public final LinearLayout view;
        private final TextView subtitle;
        private final TextView name;
        private final TextView detail;
        private final TextView age;
        private final GlyphView signal;

        Header(LinearLayout view, TextView subtitle, TextView name, TextView detail,
               TextView age, GlyphView signal) {
            this.view = view;
            this.subtitle = subtitle;
            this.name = name;
            this.detail = detail;
            this.age = age;
            this.signal = signal;
        }

        public void refresh(Store st) {
            Live live = st.live();
            boolean connected = live.hasPlayer();
            int color = connected ? Palette.GREEN : (st.configured() ? Palette.accent : Palette.TEXT_DIM);
            signal.color(color);

            if (connected) {
                boolean stale = !st.lastError().isEmpty();
                subtitle.setText((stale ? "OFFLINE COPY  ·  " : "LIVE  ·  ")
                        + (live.host.isEmpty() ? st.host() : live.host));
                subtitle.setTextColor(stale ? Palette.RED : Palette.GREEN);
                name.setText(live.player.name);
                String line = live.player.tag;
                if (live.player.townHallLevel > 0) line += "  ·  TH " + live.player.townHallLevel;
                line += "  ·  " + Fmt.number(live.player.trophies) + " trophies";
                if (!live.player.leagueName.isEmpty()) line += "  ·  " + live.player.leagueName;
                detail.setText(line);
                age.setText(ago(System.currentTimeMillis() - live.fetchedAt));
                age.setVisibility(View.VISIBLE);
            } else if (st.configured()) {
                boolean failed = live.error != null;
                subtitle.setText((failed ? "NOT SYNCED  ·  " : "READY  ·  ") + st.host());
                subtitle.setTextColor(failed ? Palette.RED : Palette.accent);
                name.setText("Tap to sync " + st.tag());
                detail.setText(live.error == null
                        ? "Connected settings stored. No reply yet."
                        : live.error);
                age.setText("");
                age.setVisibility(View.GONE);
            } else {
                subtitle.setText("NOT CONNECTED");
                subtitle.setTextColor(Palette.TEXT_DIM);
                name.setText("Tap to connect your account");
                detail.setText("Official Clash of Clans API  ·  player tag + token");
                age.setText("");
                age.setVisibility(View.GONE);
            }
        }

        private static String ago(long millis) {
            if (millis < 5_000) return "now";
            return Fmt.duration(millis) + " ago";
        }
    }

    private Chrome() {}

    public static Header topBar(Activity a, Store st, Runnable onSettings,
                                Runnable onSync, Runnable onConnect) {
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
        TextView sub = Widgets.text(a, "NOT CONNECTED", 9.5f, Palette.TEXT_DIM, true);
        sub.setLetterSpacing(0.13f);
        titles.addView(sub);
        top.addView(titles);

        top.addView(Widgets.hSpacer(a));

        GlyphView sync = Widgets.glyph(a, GlyphView.SYNC, Palette.accent, 22);
        sync.setPadding(Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8));
        sync.setBackground(Skin.round(0x22FFD98A, a, 20f));
        sync.setOnClickListener(v -> onSync.run());
        top.addView(sync);

        GlyphView gear = Widgets.glyph(a, GlyphView.GEAR, Palette.TEXT_DIM, 22);
        gear.setPadding(Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8), Skin.dp(a, 8));
        gear.setBackground(Skin.round(0x22FFFFFF, a, 20f));
        LinearLayout.LayoutParams gp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        gp.leftMargin = Skin.dp(a, 8);
        gear.setLayoutParams(gp);
        gear.setOnClickListener(v -> onSettings.run());
        top.addView(gear);
        bar.addView(top);

        bar.addView(gap(a, 9));

        LinearLayout strip = Widgets.row(a);
        strip.setBackground(Skin.inset(a));
        strip.setPadding(Skin.dp(a, 10), Skin.dp(a, 8), Skin.dp(a, 10), Skin.dp(a, 8));
        LinearLayout.LayoutParams sp = Widgets.wide();
        sp.leftMargin = Skin.dp(a, 14);
        sp.rightMargin = Skin.dp(a, 14);
        strip.setLayoutParams(sp);

        GlyphView signal = Widgets.glyph(a, GlyphView.SIGNAL, Palette.TEXT_DIM, 20);
        strip.addView(signal);

        LinearLayout texts = Widgets.column(a);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tlp.leftMargin = Skin.dp(a, 9);
        texts.setLayoutParams(tlp);

        TextView name = Widgets.text(a, "", 13.5f, Palette.TEXT, true);
        name.setSingleLine(true);
        texts.addView(name);
        TextView detail = Widgets.text(a, "", 10f, Palette.TEXT_DIM, false);
        detail.setSingleLine(true);
        detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
        texts.addView(detail);
        strip.addView(texts);

        TextView age = Widgets.text(a, "", 9.5f, Palette.TEXT_DIM, true);
        age.setGravity(Gravity.END);
        strip.addView(age);

        strip.setOnClickListener(v -> onConnect.run());
        bar.addView(strip);

        Header header = new Header(bar, sub, name, detail, age, signal);
        header.refresh(st);
        return header;
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
            LinearLayout.LayoutParams gpp = Widgets.lp(Skin.dp(a, 22), Skin.dp(a, 22));
            gpp.topMargin = Skin.dp(a, 5);
            gpp.gravity = Gravity.CENTER_HORIZONTAL;
            glyph.setLayoutParams(gpp);
            item.addView(glyph);

            TextView t = Widgets.text(a, labels[i], 9.5f,
                    index == selected ? Palette.accent : Palette.TEXT_DIM, true);
            t.setLetterSpacing(0.06f);
            t.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams tlp = Widgets.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tlp.topMargin = Skin.dp(a, 3);
            t.setLayoutParams(tlp);
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
