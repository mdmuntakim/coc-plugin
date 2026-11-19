package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.ClanWar;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.WarLog;

/** The clan war as the server reports it, plus the public war log. */
public final class WarPanel {

    private WarPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        Live live = st.live();

        if (!live.hasPlayer()) {
            Screens.add(root, prompt(a, st, refresh, "No account connected",
                    "Connect a player tag to read the real war board."));
            return root;
        }
        if (!live.hasClan()) {
            Screens.add(root, prompt(a, st, refresh, "No clan found",
                    "The player card did not include a clan, or the clan could not be read."));
            return root;
        }

        Screens.add(root, warCard(a, st, live, refresh));
        Screens.add(root, logCard(a, live));
        Screens.add(root, sourceNote(a, st, live));
        return root;
    }

    private static View prompt(Activity a, Store st, Runnable refresh, String title, String body) {
        Widgets.Card card = new Widgets.Card(a, "Clan war", GlyphView.WAR);
        card.action(Widgets.badge(a, "OFFLINE", Palette.RED));
        card.add(Widgets.text(a, title, 15f, Palette.TEXT, true));
        TextView t = Widgets.body(a, body);
        t.setTextSize(11.5f);
        t.setTextColor(Palette.TEXT_DIM);
        card.addGap();
        card.add(t);
        card.addGap();
        card.add(Widgets.pillButton(a, st.configured() ? "Sync now" : "Connect account", true,
                () -> {
                    if (st.configured()) refresh.run();
                    else ConnectDialog.show(a, st, refresh);
                }));
        return card;
    }

    private static View warCard(Activity a, Store st, Live live, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Clan war", GlyphView.WAR);
        ClanWar war = live.war;
        boolean running = war != null && war.inWar();
        String state = running ? war.stateName() : "Idle";
        int color = war != null && ClanWar.IN_WAR.equals(war.state)
                ? Palette.RED : (war != null && ClanWar.PREPARATION.equals(war.state)
                ? Palette.BLUE : Palette.TEXT_DIM);
        card.action(Widgets.badge(a, state.toUpperCase(), color));

        if (running) {
            LinearLayout stateRow = Widgets.row(a);
            stateRow.addView(Widgets.text(a,
                    "vs " + (war.opponent == null ? "unknown clan" : war.opponent.name),
                    14f, Palette.TEXT, true), Widgets.weight(1f));
            stateRow.addView(Widgets.badge(a, war.teamSize + "v" + war.teamSize, Palette.TEXT_DIM));
            card.add(stateRow);

            TextView clock = Widgets.text(a, "", 12f, Palette.accent, true);
            LinearLayout.LayoutParams cp = Widgets.wide();
            cp.topMargin = Skin.dp(a, 5);
            card.body.addView(clock, cp);
            Runnable tick = () -> clock.setText(clockText(war));
            tick.run();
            Widgets.everySecond(card, tick);

            card.addGap();
            int ourStars = war.clan == null ? 0 : war.clan.stars;
            int theirStars = war.opponent == null ? 0 : war.opponent.stars;
            card.add(Screens.pair(a, starColumn(a, "Our stars", String.valueOf(ourStars),
                            Palette.accent),
                    starColumn(a, "Their stars", String.valueOf(theirStars), Palette.RED)));

            card.addGap();
            card.add(Widgets.label(a, "Destruction"));
            TextView ours = Widgets.text(a, "", 11f, Palette.accent, true);
            TextView theirs = Widgets.text(a, "", 11f, Palette.RED, true);
            LinearLayout meters = Widgets.row(a);
            LinearLayout.LayoutParams mp = Widgets.wide();
            mp.topMargin = Skin.dp(a, 5);
            meters.setLayoutParams(mp);
            LinearLayout left = Widgets.column(a);
            left.setLayoutParams(Widgets.weight(1f));
            left.addView(ours);
            left.addView(Widgets.meter(a, destruction(war.clan)));
            LinearLayout right = Widgets.column(a);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            rp.leftMargin = Skin.dp(a, 10);
            right.setLayoutParams(rp);
            right.addView(theirs);
            right.addView(Widgets.meter(a, destruction(war.opponent)));
            meters.addView(left);
            meters.addView(right);
            card.add(meters);
            ours.setText(percent(war.clan) + " destruction");
            theirs.setText(percent(war.opponent) + " destruction");

            card.addGap();
            card.add(Widgets.line(a, "Our attacks used",
                    (war.clan == null ? 0 : war.clan.attacks) + " / " + war.totalAttacks(),
                    Palette.TEXT));
            card.add(Widgets.line(a, "Attacks left", String.valueOf(war.attacksLeft()),
                    Palette.GREEN));
            card.add(Widgets.line(a, "Attacks per member",
                    String.valueOf(war.attacksPerMember), Palette.TEXT_DIM));

            if (ClanWar.WAR_ENDED.equals(war.state)) {
                card.add(Widgets.divider(a));
                card.add(Widgets.line(a, "Result", war.result(), resultColor(war.result())));
            }

            card.add(Widgets.divider(a));
            LinearLayout actions = Widgets.row(a);
            actions.addView(Widgets.label(a, "Updated " + ago(live.fetchedAt)),
                    Widgets.weight(1f));
            actions.addView(Widgets.pillButton(a, "Sync", true, refresh));
            card.add(actions);
        } else {
            card.add(Widgets.text(a, "No war is running", 15f, Palette.TEXT, true));
            TextView t = Widgets.body(a,
                    "The current war endpoint answered notInWar for "
                            + live.clan.name + ". Wars start on the clan's schedule; press Sync "
                            + "while the war is on, or check the war log below.");
            t.setTextSize(11.5f);
            t.setTextColor(Palette.TEXT_DIM);
            card.addGap();
            card.add(t);
            card.addGap();
            card.add(Widgets.pillButton(a, "Sync now", true, refresh));
        }
        return card;
    }

    private static String clockText(ClanWar war) {
        long now = System.currentTimeMillis();
        if (war.inWar() && war.end > 0 && now > war.end) {
            // The endpoint still says inWar while the timestamp is behind us.
            return "The window has passed; press Sync for the server's next word";
        }
        switch (war.state) {
            case ClanWar.PREPARATION:
                return "Battle day starts in " + Fmt.duration(war.millisLeftAt(now));
            case ClanWar.IN_WAR:
                return "War ends in " + Fmt.duration(war.millisLeftAt(now));
            case ClanWar.WAR_ENDED:
                return "War ended " + Fmt.duration(now - war.end) + " ago";
            default:
                return "No war running";
        }
    }

    private static int resultColor(String result) {
        if ("Victory".equals(result)) return Palette.GREEN;
        if ("Defeat".equals(result)) return Palette.RED;
        return Palette.TEXT_DIM;
    }

    private static float destruction(ClanWar.Side side) {
        if (side == null) return 0f;
        return Math.max(0f, Math.min(1f, (float) (side.destruction / 100d)));
    }

    private static String percent(ClanWar.Side side) {
        return side == null ? "0%" : Math.round(side.destruction) + "%";
    }

    private static View starColumn(Activity a, String label, String value, int color) {
        LinearLayout box = Widgets.column(a);
        box.setGravity(Gravity.CENTER);
        box.setBackground(Skin.inset(a));
        box.setPadding(Skin.dp(a, 8), Skin.dp(a, 10), Skin.dp(a, 8), Skin.dp(a, 10));
        TextView v = Widgets.text(a, value, 38f, color, true);
        box.addView(v);
        TextView l = Widgets.label(a, label);
        l.setTextColor(color);
        LinearLayout.LayoutParams p = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = Skin.dp(a, 2);
        l.setLayoutParams(p);
        box.addView(l);
        return box;
    }

    private static View logCard(Activity a, Live live) {
        Widgets.Card card = new Widgets.Card(a, "War log", GlyphView.TROPHY);
        WarLog log = live.warLog;

        if (log == null || log.played() == 0) {
            TextView t = Widgets.body(a, live.clan == null || live.clan.warLogPublic
                    ? "The war log came back empty. That is normal for a clan that has not "
                    + "finished a war yet."
                    : "This clan keeps its war log private, so the API returns nothing here.");
            t.setTextSize(11.5f);
            t.setTextColor(Palette.TEXT_DIM);
            card.add(t);
            return card;
        }

        card.add(Screens.pair(a,
                Widgets.stat(a, "Wins", String.valueOf(log.wins()), Palette.GREEN),
                Widgets.stat(a, "Losses", String.valueOf(log.losses()), Palette.RED)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Draws", String.valueOf(log.draws()), Palette.TEXT_DIM),
                Widgets.stat(a, "Win rate", log.winRate() + "%", Palette.accent)));
        card.addGap();
        card.add(Widgets.text(a, log.played() + " public wars returned by the API",
                11f, Palette.TEXT_DIM, false));

        card.add(Widgets.divider(a));
        int shown = 0;
        for (WarLog.Entry e : log.entries) {
            if (shown++ >= 8) break;
            LinearLayout row = Widgets.row(a);
            row.setPadding(0, Skin.dp(a, 5), 0, Skin.dp(a, 5));

            LinearLayout who = Widgets.column(a);
            who.setLayoutParams(Widgets.weight(1f));
            who.addView(Widgets.text(a, e.opponentName, 13f, Palette.TEXT, true));
            who.addView(Widgets.text(a,
                    e.teamSize + "v" + e.teamSize + "  ·  "
                            + Math.round(e.opponentDestruction) + "% against  ·  "
                            + ago(e.endTime),
                    10f, Palette.TEXT_DIM, false));
            row.addView(who);

            LinearLayout nums = Widgets.column(a);
            nums.setGravity(Gravity.END);
            TextView score = Widgets.text(a, e.clanStars + " - " + e.opponentStars, 13f,
                    resultColor(e.won() ? "Victory" : ("lose".equalsIgnoreCase(e.result)
                            ? "Defeat" : "Draw")), true);
            score.setGravity(Gravity.END);
            nums.addView(score);
            TextView tag = Widgets.text(a, e.result, 9.5f, Palette.TEXT_DIM, true);
            tag.setGravity(Gravity.END);
            nums.addView(tag);
            row.addView(nums);

            card.add(row);
        }
        return card;
    }

    private static String ago(long at) {
        if (at <= 0) return "unknown";
        return Fmt.duration(Math.max(0, System.currentTimeMillis() - at)) + " ago";
    }

    private static View sourceNote(Activity a, Store st, Live live) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 4), Skin.dp(a, 2), Skin.dp(a, 4), Skin.dp(a, 2));
        String failure = st.lastError();
        String text = "War board and log are read from "
                + (live.host.isEmpty() ? "the API" : live.host)
                + " at the last sync. The app cannot record an attack; only the game server can."
                + (failure.isEmpty() ? "" : "  Last refresh failed: " + failure);
        TextView t = Widgets.body(a, text);
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        box.addView(t);
        return box;
    }
}
