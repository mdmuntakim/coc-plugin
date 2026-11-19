package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Leagues;
import plugin.clashofclans.model.Xp;

/** Account status: the headline card every chief opens first. */
public final class AccountPanel {

    private AccountPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);

        Screens.add(root, hero(a, st, refresh));
        Screens.add(root, leagueCard(a, st));
        Screens.add(root, statsCard(a, st));
        Screens.add(root, clanCard(a, st));
        Screens.add(root, note(a));

        return root;
    }

    private static View hero(Activity a, Store st, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Account status", GlyphView.CREST);

        LinearLayout id = Widgets.row(a);
        id.addView(Screens.medallion(a, Palette.accent, GlyphView.CREST, 58));

        LinearLayout who = Widgets.column(a);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        wp.leftMargin = Skin.dp(a, 12);
        who.setLayoutParams(wp);

        TextView name = Widgets.text(a, st.name(), 22f, Palette.TEXT, true);
        name.setSingleLine(true);
        who.addView(name);

        TextView tag = Widgets.text(a, st.tag(), 12f, Palette.TEXT_DIM, true);
        tag.setLetterSpacing(0.12f);
        who.addView(tag);

        LinearLayout chips = Widgets.row(a);
        LinearLayout.LayoutParams cp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.topMargin = Skin.dp(a, 6);
        chips.setLayoutParams(cp);
        chips.addView(Widgets.badge(a, "TOWN HALL " + st.th(), Palette.accent));
        TextView lvl = Widgets.badge(a, "LEVEL " + st.level(), Palette.BLUE);
        LinearLayout.LayoutParams lp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Skin.dp(a, 6);
        lvl.setLayoutParams(lp);
        chips.addView(lvl);
        who.addView(chips);

        id.addView(who);
        card.add(id);

        card.addGap();
        LinearLayout xpLine = Widgets.row(a);
        xpLine.addView(Widgets.label(a, "Experience"), Widgets.weight(1f));
        TextView xpValue = Widgets.text(a, Fmt.number(st.xp()) + " XP", 12f, Palette.accent, true);
        xpLine.addView(xpValue);
        card.add(xpLine);

        LinearLayout.LayoutParams mp = Widgets.wide();
        mp.topMargin = Skin.dp(a, 6);
        View xpMeter = Widgets.meter(a, Xp.progress(st.xp()));
        card.body.addView(xpMeter, mp);
        TextView xpFoot = Widgets.text(a,
                Fmt.number(Xp.remaining(st.xp())) + " XP to level " + (st.level() + 1),
                10.5f, Palette.TEXT_DIM, false);
        LinearLayout.LayoutParams fp = Widgets.wide();
        fp.topMargin = Skin.dp(a, 4);
        card.body.addView(xpFoot, fp);

        card.add(Widgets.divider(a));

        LinearLayout actions = Widgets.row(a);
        TextView local = Widgets.label(a, "Stored on this device");
        actions.addView(local, Widgets.weight(1f));
        TextView edit = Widgets.pillButton(a, "Edit profile", true,
                () -> Dialogs.editProfile(a, st, refresh));
        actions.addView(edit);
        card.add(actions);

        return card;
    }

    private static View leagueCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "League ladder", GlyphView.STAR);
        Leagues.League current = Leagues.forTrophies(st.trophies());
        Leagues.League next = Leagues.next(st.trophies());

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
        TextView league = Widgets.text(a, current.name, 19f, current.color, true);
        info.addView(league);
        TextView trophies = Widgets.text(a, Fmt.number(st.trophies()) + " trophies", 12f,
                Palette.TEXT_DIM, false);
        info.addView(trophies);
        LinearLayout badgeRow = Widgets.row(a);
        LinearLayout.LayoutParams br = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        br.topMargin = Skin.dp(a, 5);
        badgeRow.setLayoutParams(br);
        badgeRow.addView(Widgets.badge(a, "BEST " + Fmt.number(st.bestTrophies()), Palette.accentDark));
        TextView bb = Widgets.badge(a, "BUILDER " + Fmt.number(st.builderTrophies()), Palette.ELIXIR);
        LinearLayout.LayoutParams bp = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.leftMargin = Skin.dp(a, 6);
        bb.setLayoutParams(bp);
        badgeRow.addView(bb);
        info.addView(badgeRow);
        top.addView(info);
        card.add(top);

        card.addGap();
        LinearLayout.LayoutParams mp = Widgets.wide();
        View meter = Widgets.meter(a, Leagues.progress(st.trophies()));
        card.body.addView(meter, mp);
        TextView nextLine = Widgets.text(a,
                next == null
                        ? "Top of the ladder. Legend bracket."
                        : Fmt.number(Math.max(0, next.minTrophies - st.trophies()))
                                + " trophies to " + next.name,
                10.5f, Palette.TEXT_DIM, false);
        LinearLayout.LayoutParams np = Widgets.wide();
        np.topMargin = Skin.dp(a, 4);
        card.body.addView(nextLine, np);

        card.add(Widgets.divider(a));
        card.add(Widgets.label(a, "Next brackets"));
        for (int i = 0; i < Leagues.LADDER.size() && i < 3; i++) {
            Leagues.League l = upcoming(st.trophies(), i);
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

    private static View statsCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Career", GlyphView.TROPHY);
        card.add(Screens.pair(a,
                Widgets.stat(a, "Trophies", Fmt.number(st.trophies()), Palette.accent),
                Widgets.stat(a, "Best", Fmt.number(st.bestTrophies()), Palette.accentLight)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Builder Base", Fmt.number(st.builderTrophies()), Palette.ELIXIR),
                Widgets.stat(a, "War stars", Fmt.number(st.warStars()), Palette.accent)));
        card.addGap();
        card.add(Screens.pair(a,
                Widgets.stat(a, "Donated", Fmt.number(st.donations()), Palette.GREEN),
                Widgets.stat(a, "Received", Fmt.number(st.received()), Palette.BLUE)));
        card.addGap();

        int ratio = st.received() == 0 ? st.donations()
                : Math.round(st.donations() / (float) st.received() * 100f);
        card.add(Widgets.line(a, "Donation ratio", ratio + "%", Palette.GREEN));
        int played = st.warPlayed();
        int rate = played == 0 ? 0 : Math.round(st.warWins() * 100f / played);
        card.add(Widgets.line(a, "War win rate", rate + "% of " + played, Palette.accent));
        return card;
    }

    private static View clanCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Clan", GlyphView.WAR);
        LinearLayout head = Widgets.row(a);
        head.addView(Screens.medallion(a, Palette.BLUE, GlyphView.WAR, 44));
        LinearLayout names = Widgets.column(a);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        np.leftMargin = Skin.dp(a, 10);
        names.setLayoutParams(np);
        names.addView(Widgets.text(a, st.clanName(), 16f, Palette.TEXT, true));
        TextView tag = Widgets.text(a, st.clanTag() + "  ·  level " + st.clanLevel(), 11f,
                Palette.TEXT_DIM, false);
        tag.setLetterSpacing(0.05f);
        names.addView(tag);
        head.addView(names);
        head.addView(Widgets.badge(a, st.members() + " / 50", Palette.BLUE));
        card.add(head);
        card.addGap();
        card.add(Widgets.line(a, "Clan war record",
                st.warWins() + "W " + st.warLosses() + "L " + st.warDraws() + "D", Palette.TEXT));
        card.add(Widgets.line(a, "Current war", st.opponent(),
                Palette.accent));
        return card;
    }

    private static View note(Activity a) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 4), Skin.dp(a, 2), Skin.dp(a, 4), Skin.dp(a, 2));
        TextView t = Widgets.body(a,
                "COCPlugin keeps a local record. It cannot query the live game, so the numbers "
                        + "are the ones you enter or start from the planner.");
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        box.addView(t);
        return box;
    }
}
