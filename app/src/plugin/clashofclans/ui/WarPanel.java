package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.War;

/** Live war board and the clan war log. */
public final class WarPanel {

    private WarPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        Screens.add(root, warCard(a, st, refresh));
        Screens.add(root, logCard(a, st));
        Screens.add(root, note(a));
        return root;
    }

    private static View warCard(Activity a, Store st, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Clan war", GlyphView.WAR);

        LinearLayout stateRow = Widgets.row(a);
        stateRow.addView(Widgets.badge(a, War.stateName(st.warState()).toUpperCase(),
                st.warState() == War.STATE_BATTLE ? Palette.RED : Palette.BLUE));
        stateRow.addView(Widgets.gap(a, 6), Widgets.lp(Skin.dp(a, 6), 1));
        stateRow.addView(Widgets.text(a, "vs " + st.opponent(), 13f, Palette.TEXT, true),
                Widgets.weight(1f));
        card.add(stateRow);

        TextView clock = Widgets.text(a, "", 12f, Palette.accent, true);
        LinearLayout.LayoutParams cp = Widgets.wide();
        cp.topMargin = Skin.dp(a, 5);
        card.body.addView(clock, cp);
        Runnable clockUpdate = () -> {
            long left = st.warEnd() - System.currentTimeMillis();
            if (st.warState() == War.STATE_ENDED) {
                clock.setText("War closed. Record the result in the log.");
            } else {
                clock.setText((st.warState() == War.STATE_PREP ? "Battle starts in " : "War ends in ")
                        + Fmt.duration(Math.max(0, left)));
            }
        };
        clockUpdate.run();
        Widgets.everySecond(card, clockUpdate);

        card.addGap();
        TextView ourStars = Widgets.text(a, String.valueOf(st.starsOur()), 38f, Palette.accent, true);
        TextView theirStars = Widgets.text(a, String.valueOf(st.starsTheir()), 38f, Palette.RED, true);
        card.add(Screens.pair(a, starColumn(a, "Our stars", ourStars, Palette.accent),
                starColumn(a, "Their stars", theirStars, Palette.RED)));

        card.addGap();
        card.add(Widgets.label(a, "Destruction"));
        LinearLayout.LayoutParams mp = Widgets.wide();
        mp.topMargin = Skin.dp(a, 5);
        card.body.addView(Widgets.meter(a,
                War.destruction(st.starsOur(), st.members() * 3)), mp);
        card.addGap();
        card.add(Widgets.line(a, "Our attacks used",
                st.attacksUsed() + " / " + (st.members() * 2), Palette.TEXT));
        card.add(Widgets.line(a, "Attacks left",
                String.valueOf(War.attacksLeft(st.members(), st.attacksUsed())), Palette.GREEN));

        card.add(Widgets.divider(a));
        LinearLayout actions = Widgets.row(a);
        actions.setBaselineAligned(false);
        TextView attack = Widgets.pillButton(a, "Record attack", true, () -> {
            if (st.warState() == War.STATE_ENDED) {
                Chrome.toast(a, "That war is over. Start a new one first.");
                return;
            }
            if (st.attacksUsed() >= st.members() * 2) {
                Chrome.toast(a, "Every attack of this war is already recorded");
                return;
            }
            Dialogs.recordAttack(a, st, refresh);
        });
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ap.rightMargin = Skin.dp(a, 4);
        attack.setLayoutParams(ap);
        actions.addView(attack);

        TextView finish = Widgets.pillButton(a, "Finish war", false, () -> {
            if (st.warState() == War.STATE_ENDED) {
                Chrome.toast(a, "This war is already in the log");
                return;
            }
            String result = st.finishWar();
            Chrome.toast(a, result + " " + st.starsOur() + " - " + st.starsTheir()
                    + " against " + st.opponent());
            refresh.run();
        });
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        fp.leftMargin = Skin.dp(a, 4);
        finish.setLayoutParams(fp);
        actions.addView(finish);
        card.add(actions);

        card.addGap();
        if (st.warState() == War.STATE_PREP) {
            TextView battle = Widgets.pillButton(a, "Start battle day", true, () -> {
                st.setWarPhase(War.STATE_BATTLE);
                Chrome.toast(a, "Battle day is live");
                refresh.run();
            });
            card.add(battle);
            card.addGap();
        }
        TextView newWar = Widgets.pillButton(a, "Start a new war", false,
                () -> Dialogs.newWar(a, st, refresh));
        card.add(newWar);
        return card;
    }

    private static View starColumn(Activity a, String label, TextView value, int color) {
        LinearLayout box = Widgets.column(a);
        box.setGravity(Gravity.CENTER);
        box.setBackground(Skin.inset(a));
        box.setPadding(Skin.dp(a, 8), Skin.dp(a, 10), Skin.dp(a, 8), Skin.dp(a, 10));
        box.addView(value);
        TextView l = Widgets.label(a, label);
        l.setTextColor(color);
        LinearLayout.LayoutParams p = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = Skin.dp(a, 2);
        l.setLayoutParams(p);
        box.addView(l);
        return box;
    }

    private static View logCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "War log", GlyphView.TROPHY);
        int played = st.warPlayed();
        int rate = played == 0 ? 0 : Math.round(st.warWins() * 100f / played);

        card.add(Screens.pair(a,
                Widgets.stat(a, "Wins", String.valueOf(st.warWins()), Palette.GREEN),
                Widgets.stat(a, "Losses", String.valueOf(st.warLosses()), Palette.RED)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Draws", String.valueOf(st.warDraws()), Palette.TEXT_DIM),
                Widgets.stat(a, "Win rate", rate + "%", Palette.accent)));
        card.addGap();
        TextView streak = Widgets.text(a, played + " wars recorded on this device", 11f,
                Palette.TEXT_DIM, false);
        card.add(streak);
        return card;
    }

    private static View note(Activity a) {
        TextView t = Widgets.body(a,
                "The board is manual: tap Record attack after a raid, or Finish war when the "
                        + "battle day is over. It never connects to the game server.");
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        return t;
    }
}
