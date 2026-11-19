package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Clan;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Leagues;
import plugin.clashofclans.model.Player;

/** Account status, straight from the API: the headline card every chief opens first. */
public final class AccountPanel {

    private AccountPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        Live live = st.live();

        if (!live.hasPlayer()) {
            Screens.add(root, connectCard(a, st, refresh));
            Screens.add(root, offlineNote(a, live));
            return root;
        }

        Player p = live.player;
        Screens.add(root, hero(a, st, p, live, refresh));
        Screens.add(root, leagueCard(a, p, live));
        Screens.add(root, statsCard(a, p));
        if (live.hasClan()) Screens.add(root, clanCard(a, live.clan, refresh));
        if (live.hasClan() && !live.clan.memberList.isEmpty()) {
            Screens.add(root, membersCard(a, live.clan));
        }
        Screens.add(root, sourceNote(a, st, live));
        return root;
    }

    // --------------------------------------------------------- disconnected
    private static View connectCard(Activity a, Store st, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Live account", GlyphView.SIGNAL);
        card.action(Widgets.badge(a, "OFFLINE", Palette.RED));

        LinearLayout head = Widgets.row(a);
        head.addView(Screens.medallion(a, Palette.accent, GlyphView.SIGNAL, 52));
        LinearLayout words = Widgets.column(a);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        wp.leftMargin = Skin.dp(a, 12);
        words.setLayoutParams(wp);
        words.addView(Widgets.text(a, "No account connected yet", 16f, Palette.TEXT, true));
        TextView sub = Widgets.text(a, st.configured()
                        ? ("Settings for " + st.tag() + " are saved. Sync to fetch it.")
                        : "Add your player tag and the API token from developer.clashofclans.com.",
                11f, Palette.TEXT_DIM, false);
        words.addView(sub);
        head.addView(words);
        card.add(head);

        card.addGap();
        TextView button = Widgets.pillButton(a, st.configured() ? "Sync now" : "Connect account",
                true, () -> {
                    if (st.configured()) refresh.run();
                    else ConnectDialog.show(a, st, refresh);
                });
        button.setGravity(Gravity.CENTER);
        card.add(button);

        card.add(Widgets.divider(a));
        card.add(Widgets.line(a, "Server", st.host(), Palette.TEXT_DIM));
        card.add(Widgets.line(a, "Player tag", st.tag().isEmpty() ? "not set" : st.tag(),
                st.tag().isEmpty() ? Palette.TEXT_DIM : Palette.accent));
        card.add(Widgets.line(a, "Token", st.token().isEmpty() ? "not set" : "stored on device",
                st.token().isEmpty() ? Palette.TEXT_DIM : Palette.GREEN));

        if (st.configured()) {
            card.addGap();
            TextView edit = Widgets.pillButton(a, "Edit connection", false,
                    () -> ConnectDialog.show(a, st, refresh));
            card.add(edit);
        }
        return card;
    }

    private static View offlineNote(Activity a, Live live) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 4), Skin.dp(a, 2), Skin.dp(a, 4), Skin.dp(a, 2));
        String text = live.error == null
                ? "The app has no invented profile any more. Name, tag, trophies, clan, war and "
                        + "unit levels are all read from the official API, so this screen stays "
                        + "empty until a tag and token are entered."
                : live.error;
        TextView t = Widgets.body(a, text);
        t.setTextSize(10.5f);
        t.setTextColor(live.error == null ? 0x88A6B1BE : Palette.RED);
        box.addView(t);
        return box;
    }

    // -------------------------------------------------------------- account
    private static View hero(Activity a, Store st, Player p, Live live, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Account status", GlyphView.CREST);
        card.action(Screens.liveBadge(a, st));

        LinearLayout id = Widgets.row(a);
        id.addView(Screens.medallion(a, Palette.accent, GlyphView.CREST, 58));

        LinearLayout who = Widgets.column(a);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        wp.leftMargin = Skin.dp(a, 12);
        who.setLayoutParams(wp);

        TextView name = Widgets.text(a, p.name, 22f, Palette.TEXT, true);
        name.setSingleLine(true);
        who.addView(name);

        TextView tag = Widgets.text(a, p.tag + (p.role.isEmpty() ? "" : "  ·  " + p.role),
                12f, Palette.TEXT_DIM, true);
        tag.setLetterSpacing(0.12f);
        who.addView(tag);

        LinearLayout chips = Widgets.row(a);
        LinearLayout.LayoutParams cp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.topMargin = Skin.dp(a, 6);
        chips.setLayoutParams(cp);
        chips.addView(Widgets.badge(a, "TOWN HALL " + p.townHallLevel, Palette.accent));
        TextView lvl = Widgets.badge(a, "LEVEL " + p.expLevel, Palette.BLUE);
        LinearLayout.LayoutParams lp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Skin.dp(a, 6);
        lvl.setLayoutParams(lp);
        chips.addView(lvl);
        who.addView(chips);

        id.addView(who);
        card.add(id);

        card.addGap();
        card.add(Widgets.line(a, "Trophies", Fmt.number(p.trophies) + "  \u2605", Palette.accent));
        card.add(Widgets.line(a, "League",
                p.leagueName.isEmpty() ? Leagues.forTrophies(p.trophies).name : p.leagueName,
                Palette.TEXT));
        if (p.townHallWeaponLevel > 0) {
            card.add(Widgets.line(a, "Weapon level", String.valueOf(p.townHallWeaponLevel),
                    Palette.TEXT_DIM));
        }
        card.add(Widgets.line(a, "Clan",
                p.inClan() ? p.clanName + "  (" + p.clanTag + ")" : "no clan", Palette.BLUE));

        card.add(Widgets.divider(a));
        LinearLayout actions = Widgets.row(a);
        actions.addView(Widgets.label(a, "Synced " + ago(live.fetchedAt)), Widgets.weight(1f));
        actions.addView(Widgets.pillButton(a, "Sync", true, refresh));
        actions.addView(Widgets.gap(a, 6), Widgets.lp(Skin.dp(a, 6), 1));
        actions.addView(Widgets.pillButton(a, "Connection", false,
                () -> ConnectDialog.show(a, st, refresh)));
        card.add(actions);
        return card;
    }

    private static String ago(long at) {
        if (at <= 0) return "never";
        return Fmt.duration(System.currentTimeMillis() - at) + " ago";
    }

    private static View leagueCard(Activity a, Player p, Live live) {
        Widgets.Card card = new Widgets.Card(a, "League ladder", GlyphView.STAR);
        Leagues.League current = Leagues.forTrophies(p.trophies);
        Leagues.League next = Leagues.next(p.trophies);

        LinearLayout top = Widgets.row(a);
        LinearLayout medal = Widgets.column(a);
        medal.setGravity(Gravity.CENTER);
        medal.setBackground(Skin.stroke(Skin.withAlpha(current.color, 45), current.color, a, 28f, 2f));
        medal.setLayoutParams(Widgets.lp(Skin.dp(a, 56), Skin.dp(a, 56)));
        medal.addView(Widgets.glyph(a, GlyphView.STAR, current.color, 26));
        top.addView(medal);

        LinearLayout info = Widgets.column(a);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.leftMargin = Skin.dp(a, 12);
        info.setLayoutParams(ip);
        TextView league = Widgets.text(a,
                p.leagueName.isEmpty() ? current.name : p.leagueName, 19f, current.color, true);
        info.addView(league);
        TextView trophies = Widgets.text(a, Fmt.number(p.trophies) + " trophies", 12f,
                Palette.TEXT_DIM, false);
        info.addView(trophies);
        LinearLayout badgeRow = Widgets.row(a);
        LinearLayout.LayoutParams br = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        br.topMargin = Skin.dp(a, 5);
        badgeRow.setLayoutParams(br);
        badgeRow.addView(Widgets.badge(a, "BEST " + Fmt.number(p.bestTrophies), Palette.accentDark));
        if (p.builderBaseTrophies > 0) {
            TextView bb = Widgets.badge(a, "BUILDER " + Fmt.number(p.builderBaseTrophies),
                    Palette.ELIXIR);
            LinearLayout.LayoutParams bp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            bp.leftMargin = Skin.dp(a, 6);
            bb.setLayoutParams(bp);
            badgeRow.addView(bb);
        }
        info.addView(badgeRow);
        top.addView(info);
        card.add(top);

        card.addGap();
        LinearLayout.LayoutParams mp = Widgets.wide();
        View meter = Widgets.meter(a, Leagues.progress(p.trophies));
        card.body.addView(meter, mp);
        TextView nextLine = Widgets.text(a,
                next == null
                        ? "Top of the ladder. Legend bracket."
                        : Fmt.number(Math.max(0, next.minTrophies - p.trophies))
                                + " trophies to " + next.name,
                10.5f, Palette.TEXT_DIM, false);
        LinearLayout.LayoutParams np = Widgets.wide();
        np.topMargin = Skin.dp(a, 4);
        card.body.addView(nextLine, np);

        card.add(Widgets.divider(a));
        card.add(Widgets.label(a, "Next brackets"));
        for (int i = 0; i < Leagues.LADDER.size() && i < 3; i++) {
            Leagues.League l = upcoming(p.trophies, i);
            if (l == null) break;
            card.add(Widgets.line(a, l.name, Fmt.number(l.minTrophies) + " \u2605", l.color));
        }
        return card;
    }

    private static Leagues.League upcoming(int trophies, int offset) {
        int found = 0;
        for (Leagues.League l : Leagues.LADDER) {
            if (l.minTrophies > trophies) {
                if (found == offset) return l;
                found++;
            }
        }
        return null;
    }

    private static View statsCard(Activity a, Player p) {
        Widgets.Card card = new Widgets.Card(a, "Career (from the API)", GlyphView.TROPHY);
        card.add(Screens.pair(a,
                Widgets.stat(a, "Trophies", Fmt.number(p.trophies), Palette.accent),
                Widgets.stat(a, "Best", Fmt.number(p.bestTrophies), Palette.accentLight)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Builder Base", Fmt.number(p.builderBaseTrophies), Palette.ELIXIR),
                Widgets.stat(a, "Best builder", Fmt.number(p.bestBuilderBaseTrophies),
                        Palette.ELIXIR)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "War stars", Fmt.number(p.warStars), Palette.accent),
                Widgets.stat(a, "Capital gold",
                        Fmt.shortNumber(p.clanCapitalContributions), Palette.GOLD)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Donated", Fmt.number(p.donations), Palette.GREEN),
                Widgets.stat(a, "Received", Fmt.number(p.donationsReceived), Palette.BLUE)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Attack wins", Fmt.number(p.attackWins), Palette.TEXT),
                Widgets.stat(a, "Defence wins", Fmt.number(p.defenseWins), Palette.TEXT)));
        card.addGap();
        card.add(Widgets.line(a, "Donation ratio", p.donationRatio() + "%", Palette.GREEN));
        card.add(Widgets.line(a, "Attacks",
                p.attackWins + "W " + p.attackLosses + "L", Palette.TEXT));
        return card;
    }

    private static View clanCard(Activity a, Clan c, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Clan", GlyphView.WAR);
        LinearLayout head = Widgets.row(a);
        head.addView(Screens.medallion(a, Palette.BLUE, GlyphView.WAR, 44));
        LinearLayout names = Widgets.column(a);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        np.leftMargin = Skin.dp(a, 10);
        names.setLayoutParams(np);
        names.addView(Widgets.text(a, c.name, 16f, Palette.TEXT, true));
        TextView tag = Widgets.text(a, c.tag + "  ·  level " + c.level, 11f,
                Palette.TEXT_DIM, false);
        tag.setLetterSpacing(0.05f);
        names.addView(tag);
        head.addView(names);
        head.addView(Widgets.badge(a, c.members + " / 50", Palette.BLUE));
        card.add(head);
        card.addGap();
        card.add(Widgets.line(a, "Clan war record", c.warRecord(), Palette.TEXT));
        card.add(Widgets.line(a, "War win rate", c.winRate() + "% of " + c.warPlayed(),
                Palette.accent));
        card.add(Widgets.line(a, "Clan points", Fmt.number(c.points), Palette.TEXT));
        if (!c.warLeagueName.isEmpty()) {
            card.add(Widgets.line(a, "War league", c.warLeagueName, Palette.accent));
        }
        if (!c.locationName.isEmpty()) {
            card.add(Widgets.line(a, "Location", c.locationName, Palette.TEXT_DIM));
        }
        if (!c.warFrequency.isEmpty()) {
            card.add(Widgets.line(a, "War frequency", c.warFrequency, Palette.TEXT_DIM));
        }
        card.add(Widgets.line(a, "War log", c.warLogPublic ? "public" : "private",
                c.warLogPublic ? Palette.GREEN : Palette.TEXT_DIM));
        if (!c.description.isEmpty()) {
            card.addGap();
            TextView d = Widgets.body(a, c.description);
            d.setTextSize(11.5f);
            d.setTextColor(Palette.TEXT_DIM);
            card.add(d);
        }
        return card;
    }

    private static View membersCard(Activity a, Clan c) {
        Widgets.Card card = new Widgets.Card(a, "Members", GlyphView.ARMY);
        List<Clan.Member> list = new ArrayList<>(c.memberList);
        list.sort((x, y) -> Integer.compare(x.rank == 0 ? 999 : x.rank,
                y.rank == 0 ? 999 : y.rank));
        int shown = 0;
        for (Clan.Member m : list) {
            if (shown++ >= 10) break;
            LinearLayout row = Widgets.row(a);
            row.setPadding(0, Skin.dp(a, 5), 0, Skin.dp(a, 5));

            LinearLayout who = Widgets.column(a);
            who.setLayoutParams(Widgets.weight(1f));
            who.addView(Widgets.text(a, m.name, 13f, Palette.TEXT, true));
            TextView sub = Widgets.text(a,
                    m.tag + (m.role.isEmpty() ? "" : "  ·  " + m.role) + "  ·  lvl " + m.level,
                    10f, Palette.TEXT_DIM, false);
            sub.setSingleLine(true);
            sub.setEllipsize(android.text.TextUtils.TruncateAt.END);
            who.addView(sub);
            row.addView(who);

            LinearLayout nums = Widgets.column(a);
            nums.setGravity(Gravity.END);
            TextView tro = Widgets.text(a, Fmt.number(m.trophies) + " \u2605", 12f,
                    Palette.accent, true);
            tro.setGravity(Gravity.END);
            nums.addView(tro);
            TextView don = Widgets.text(a, "+" + m.donations + " / -" + m.donationsReceived,
                    9.5f, Palette.TEXT_DIM, false);
            don.setGravity(Gravity.END);
            nums.addView(don);
            row.addView(nums);

            card.add(row);
        }
        if (list.size() > 10) {
            card.add(Widgets.text(a, "+ " + (list.size() - 10) + " more members",
                    10.5f, Palette.TEXT_DIM, false));
        }
        return card;
    }

    private static View sourceNote(Activity a, Store st, Live live) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 4), Skin.dp(a, 2), Skin.dp(a, 4), Skin.dp(a, 2));
        String failure = st.lastError();
        String text = "Every figure above came from " + (live.host.isEmpty() ? "the API" : live.host)
                + " at the last sync" + (live.warning == null || live.warning.isEmpty()
                ? "." : ". " + live.warning)
                + (failure.isEmpty() ? "" : "  Last refresh failed: " + failure);
        TextView t = Widgets.body(a, text);
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        box.addView(t);
        return box;
    }
}
