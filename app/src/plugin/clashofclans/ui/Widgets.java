package plugin.clashofclans.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

/** Small factory of the themed building blocks the screens are made of. */
public final class Widgets {

    private Widgets() {}

    // ------------------------------------------------------------- text
    public static TextView text(Context c, CharSequence s, float sizeSp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sizeSp);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        if (bold) {
            t.setTypeface(android.graphics.Typeface.create("sans-serif-condensed",
                    android.graphics.Typeface.BOLD));
        } else {
            t.setTypeface(android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL));
        }
        t.setLineSpacing(Skin.dp(c, 2), 1f);
        return t;
    }

    public static TextView heading(Context c, CharSequence s) {
        TextView t = text(c, s.toString().toUpperCase(Locale.US), 15f, Palette.accent, true);
        t.setLetterSpacing(0.06f);
        t.setShadowLayer(Skin.dp(c, 2), 0, Skin.dp(c, 1), 0x99000000);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        return t;
    }

    public static TextView label(Context c, CharSequence s) {
        TextView t = text(c, s.toString().toUpperCase(Locale.US), 10.5f, Palette.TEXT_DIM, true);
        t.setLetterSpacing(0.09f);
        return t;
    }

    public static TextView value(Context c, CharSequence s, int color) {
        return text(c, s, 17f, color, true);
    }

    public static TextView body(Context c, CharSequence s) {
        TextView t = text(c, s, 13f, Palette.TEXT, false);
        t.setLineSpacing(Skin.dp(c, 3), 1f);
        return t;
    }

    // ---------------------------------------------------------- layout
    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout.LayoutParams lp(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams wide() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public static LinearLayout.LayoutParams weight(float w) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w);
    }

    /**
     * Flexible horizontal spacer for a row.
     *
     * <p>It needs a fixed height: a bare View with WRAP_CONTENT height reports
     * the whole AT_MOST size, which makes a row grab the full screen.
     */
    public static View hSpacer(Context c) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(0, Math.max(1, Skin.dp(c, 1)), 1f));
        return v;
    }

    public static void margins(View v, int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams raw = v.getLayoutParams();
        if (!(raw instanceof ViewGroup.MarginLayoutParams)) return;
        ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) raw;
        p.setMargins(left, top, right, bottom);
        v.setLayoutParams(p);
    }

    public static void pad(View v, int l, int t, int r, int b) {
        v.setPadding(l, t, r, b);
    }

    public static View gap(Context c, float dp) {
        View v = new View(c);
        v.setLayoutParams(lp(1, Skin.dp(c, dp)));
        return v;
    }

    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(0x22FFFFFF);
        LinearLayout.LayoutParams p = lp(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Skin.dp(c, 1)));
        p.topMargin = Skin.dp(c, 8);
        p.bottomMargin = Skin.dp(c, 8);
        v.setLayoutParams(p);
        return v;
    }

    public static Button button(Context c, CharSequence label, boolean primary, Runnable onClick) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13f);
        b.setTypeface(android.graphics.Typeface.create("sans-serif-condensed",
                android.graphics.Typeface.BOLD));
        b.setTextColor(primary ? 0xFF2A2013 : Palette.TEXT);
        b.setBackground(primary ? Skin.goldButton(c, 9f) : Skin.stoneButton(c, 9f));
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(Skin.dp(c, 14), Skin.dp(c, 9), Skin.dp(c, 14), Skin.dp(c, 9));
        b.setStateListAnimator(null);
        b.setOnClickListener(v -> onClick.run());
        return b;
    }

    /** Compact action pill used inside list rows. */
    public static TextView pillButton(Context c, CharSequence label, boolean primary, Runnable onClick) {
        TextView t = text(c, label, 11.5f, primary ? 0xFF2A2013 : Palette.TEXT, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(Skin.dp(c, 11), Skin.dp(c, 7), Skin.dp(c, 11), Skin.dp(c, 7));
        t.setBackground(primary ? Skin.goldButton(c, 8f) : Skin.stoneButton(c, 8f));
        t.setOnClickListener(v -> onClick.run());
        return t;
    }

    /** A rounded badge with a short label, e.g. the Town Hall level. */
    public static TextView badge(Context c, CharSequence label, int color) {
        TextView t = text(c, label, 11f, 0xFF202730, true);
        t.setGravity(Gravity.CENTER);
        t.setPadding(Skin.dp(c, 9), Skin.dp(c, 4), Skin.dp(c, 9), Skin.dp(c, 4));
        t.setBackground(Skin.round(color, c, 20f));
        return t;
    }

    public static GlyphView glyph(Context c, int kind, int color, int sizeDp) {
        GlyphView g = new GlyphView(c).kind(kind).color(color);
        g.setLayoutParams(lp(Skin.dp(c, sizeDp), Skin.dp(c, sizeDp)));
        return g;
    }

    /** Horizontal resource chip: icon plus a short value. */
    public static LinearLayout chip(Context c, int glyph, int color, String value) {
        LinearLayout box = row(c);
        box.setBackground(Skin.chip(c, color));
        box.setPadding(Skin.dp(c, 8), Skin.dp(c, 5), Skin.dp(c, 10), Skin.dp(c, 5));
        box.addView(glyph(c, glyph, color, 14));
        TextView t = text(c, value, 12f, Palette.TEXT, true);
        LinearLayout.LayoutParams p = lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.leftMargin = Skin.dp(c, 5);
        t.setLayoutParams(p);
        box.addView(t);
        return box;
    }

    // ---------------------------------------------------------- panels
    /** Stone card with a gold header ribbon. */
    public static final class Card extends LinearLayout {
        public final LinearLayout body;
        private final LinearLayout header;

        public Card(Context c, String title, int icon) {
            super(c);
            setOrientation(VERTICAL);
            setBackground(Skin.panel(c));

            header = row(c);
            header.setBackground(Skin.header(c));
            header.setPadding(Skin.dp(c, 12), Skin.dp(c, 9), Skin.dp(c, 10), Skin.dp(c, 9));
            header.addView(glyph(c, icon, 0xFF2A2013, 18));
            TextView t = heading(c, title);
            t.setTextColor(0xFF2A2013);
            t.setShadowLayer(0, 0, 0, 0);
            LinearLayout.LayoutParams tp = lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.leftMargin = Skin.dp(c, 8);
            t.setLayoutParams(tp);
            header.addView(t, tp);
            header.addView(hSpacer(c));
            addView(header, wide());

            body = column(c);
            body.setPadding(Skin.dp(c, 12), Skin.dp(c, 10), Skin.dp(c, 12), Skin.dp(c, 12));
            addView(body, wide());
        }

        public Card action(View v) {
            LinearLayout.LayoutParams p = lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            p.leftMargin = Skin.dp(getContext(), 6);
            header.addView(v, p);
            return this;
        }

        public void add(View v) {
            // Keep a view's own fixed size (a divider is one pixel high). A
            // bare View measured with WRAP_CONTENT would otherwise swallow the
            // whole remaining height.
            ViewGroup.LayoutParams own = v.getLayoutParams();
            body.addView(v, own == null ? wide() : own);
        }

        public void addGap() {
            body.addView(gap(getContext(), 10));
        }
    }

    /** A small inset tile: uppercase label above a bold value. */
    public static LinearLayout stat(Context c, String label, String value, int color) {
        LinearLayout box = column(c);
        box.setBackground(Skin.inset(c));
        box.setPadding(Skin.dp(c, 10), Skin.dp(c, 8), Skin.dp(c, 10), Skin.dp(c, 8));
        box.addView(label(c, label));
        TextView v = value(c, value, color);
        v.setTextSize(16f);
        box.addView(v);
        return box;
    }

    /** Label on the left, value on the right. */
    public static LinearLayout line(Context c, String name, String val, int color) {
        LinearLayout r = row(c);
        r.setPadding(0, Skin.dp(c, 4), 0, Skin.dp(c, 4));
        r.addView(label(c, name), weight(1f));
        r.addView(text(c, val, 13.5f, color, true), lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return r;
    }

    private static final class Meter extends View {
        private float progress;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        Meter(Context c) {
            super(c);
        }

        void set(float p) {
            progress = Math.max(0f, Math.min(1f, p));
            invalidate();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int h = Skin.dp(getContext(), 14);
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float h = getHeight();
            float r = h / 3f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xFF12171D);
            rect.set(0, 0, getWidth(), h);
            canvas.drawRoundRect(rect, r, r, paint);

            if (progress > 0.001f) {
                paint.setShader(new LinearGradient(0, 0, 0, h,
                        new int[]{Palette.accentLight, Palette.accent, Palette.accentDark},
                        null, Shader.TileMode.CLAMP));
                rect.set(1, 1, Math.max(h, getWidth() * progress), h - 1);
                canvas.drawRoundRect(rect, r, r, paint);
                paint.setShader(null);
            }

            paint.setColor(0x22000000);
            for (int i = 1; i < 4; i++) {
                float x = getWidth() * i / 4f;
                canvas.drawRect(x, 0, x + 1, h, paint);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, Skin.dp(getContext(), 1)));
            paint.setColor(0x55FFFFFF);
            rect.set(0.5f, 0.5f, getWidth() - 0.5f, h - 0.5f);
            canvas.drawRoundRect(rect, r, r, paint);
        }
    }

    public static View meter(Context c, float progress) {
        Meter m = new Meter(c);
        m.set(progress);
        m.setLayoutParams(wide());
        return m;
    }

    public static void setMeter(View meter, float progress) {
        if (meter instanceof Meter) ((Meter) meter).set(progress);
    }

    // ----------------------------------------------------------- clock
    /** Runs {@code tick} once a second while the root view is attached. */
    public static void everySecond(View root, Runnable tick) {
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable[] loop = new Runnable[1];
        loop[0] = () -> {
            if (!root.isAttachedToWindow()) return;
            tick.run();
            handler.postDelayed(loop[0], 1000L);
        };
        root.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                handler.removeCallbacks(loop[0]);
                handler.post(loop[0]);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                handler.removeCallbacks(loop[0]);
            }
        });
    }
}
