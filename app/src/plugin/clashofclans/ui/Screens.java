package plugin.clashofclans.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Layout glue shared by the five screens. */
public final class Screens {

    private Screens() {}

    /** Adds a card to a column with the standard 12dp gap. */
    public static View add(LinearLayout root, View card) {
        LinearLayout.LayoutParams p = Widgets.wide();
        p.bottomMargin = Skin.dp(root.getContext(), 12);
        root.addView(card, p);
        return card;
    }

    public static LinearLayout pair(Context c, View left, View right) {
        LinearLayout row = Widgets.row(c);
        row.setBaselineAligned(false);
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        l.rightMargin = Skin.dp(c, 4);
        LinearLayout.LayoutParams r = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        r.leftMargin = Skin.dp(c, 4);
        row.addView(left, l);
        row.addView(right, r);
        return row;
    }

    /** Rounded league / stat medallion. */
    public static LinearLayout medallion(Context c, int color, int glyph, int sizeDp) {
        LinearLayout box = Widgets.column(c);
        box.setGravity(Gravity.CENTER);
        box.setBackground(Skin.stroke(Skin.withAlpha(color, 45), color, c, sizeDp, 1.5f));
        box.setLayoutParams(Widgets.lp(Skin.dp(c, sizeDp), Skin.dp(c, sizeDp)));
        box.addView(Widgets.glyph(c, glyph, color, Math.round(sizeDp * 0.5f)));
        return box;
    }

    /** Uppercase name on the left, a tinted value on the right. */
    public static LinearLayout fact(Context c, String name, String value, int color) {
        return Widgets.line(c, name, value, color);
    }

    public static TextView sectionLabel(Context c, String text) {
        return Widgets.label(c, text);
    }
}
