package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Player;

/** The real troop, spell, hero and pet levels returned for the account. */
public final class ArmyPanel {

    private ArmyPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        Live live = st.live();

        if (!live.hasPlayer()) {
            Screens.add(root, prompt(a, st, refresh));
            return root;
        }

        Player p = live.player;
        if (p.units.isEmpty()) {
            Widgets.Card card = new Widgets.Card(a, "Army", GlyphView.ARMY);
            card.action(Widgets.badge(a, "LIVE", Palette.GREEN));
            TextView t = Widgets.body(a,
                    "The server returned no unit levels for this tag. Builder Base only "
                            + "accounts and brand new players can answer with an empty list.");
            t.setTextSize(11.5f);
            t.setTextColor(Palette.TEXT_DIM);
            card.add(t);
            Screens.add(root, card);
            return root;
        }

        Screens.add(root, summaryCard(a, st, p));
        Screens.add(root, unitCard(a, st, p, "Troops", "troop"));
        if (!p.units("hero", "home").isEmpty()) {
            Screens.add(root, unitCard(a, st, p, "Heroes", "hero"));
        }
        if (!p.units("spell", "home").isEmpty()) {
            Screens.add(root, unitCard(a, st, p, "Spells", "spell"));
        }
        if (!p.units("pet", "home").isEmpty()) {
            Screens.add(root, unitCard(a, st, p, "Pets", "pet"));
        }
        List<Player.Unit> builder = new ArrayList<>();
        for (Player.Unit u : p.units) {
            if ("builderBase".equals(u.village)) builder.add(u);
        }
        if (!builder.isEmpty()) {
            Screens.add(root, builderCard(a, st, builder));
        }
        Screens.add(root, sourceNote(a, live));
        return root;
    }

    private static View prompt(Activity a, Store st, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Army", GlyphView.ARMY);
        card.action(Widgets.badge(a, "OFFLINE", Palette.RED));
        card.add(Widgets.text(a, "No account connected", 15f, Palette.TEXT, true));
        TextView t = Widgets.body(a,
                "Troop, spell, hero and pet levels are read from the player endpoint of the "
                        + "official API. Connect once and this screen fills itself.");
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

    private static View summaryCard(Activity a, Store st, Player p) {
        Widgets.Card card = new Widgets.Card(a, "Army totals", GlyphView.STAR);
        card.action(Screens.liveBadge(a, st));
        int maxed = p.maxedUnits();
        float progress = p.unitProgress(p.units);

        card.add(Screens.pair(a,
                Widgets.stat(a, "Units tracked", String.valueOf(p.units.size()), Palette.TEXT),
                Widgets.stat(a, "Maxed", maxed + " / " + p.units.size(), Palette.GREEN)));
        card.addGap();
        card.add(Widgets.line(a, "Total levels", percent(progress), Palette.accent));
        LinearLayout.LayoutParams mp = Widgets.wide();
        mp.topMargin = Skin.dp(a, 7);
        card.body.addView(Widgets.meter(a, progress), mp);
        card.addGap();
        card.add(Widgets.line(a, "Town Hall", "TH " + p.townHallLevel, Palette.accent));
        card.add(Widgets.line(a, "Builder Hall",
                p.builderHallLevel > 0 ? "BH " + p.builderHallLevel : "not unlocked",
                Palette.ELIXIR));
        card.add(Widgets.text(a,
                Fmt.percent(progress) + " of the levels the server reports are already reached",
                11f, Palette.TEXT_DIM, false));
        return card;
    }

    private static View unitCard(Activity a, Store st, Player p, String title, String kind) {
        Widgets.Card card = new Widgets.Card(a, title, icon(kind));
        card.action(Screens.liveBadge(a, st));

        Map<String, List<Player.Unit>> villages = new LinkedHashMap<>();
        for (Player.Unit u : p.units) {
            if (!u.kind.equals(kind)) continue;
            List<Player.Unit> list = villages.get(u.village);
            if (list == null) {
                list = new ArrayList<>();
                villages.put(u.village, list);
            }
            list.add(u);
        }

        for (Map.Entry<String, List<Player.Unit>> entry : villages.entrySet()) {
            if (villages.size() > 1) {
                View header = Screens.sectionLabel(a,
                        "builderBase".equals(entry.getKey()) ? "Builder Base" : "Home village");
                LinearLayout.LayoutParams hp = Widgets.wide();
                hp.topMargin = Skin.dp(a, 8);
                hp.bottomMargin = Skin.dp(a, 2);
                card.body.addView(header, hp);
            }
            for (Player.Unit u : entry.getValue()) {
                card.add(row(a, u));
            }
        }
        return card;
    }

    private static View builderCard(Activity a, Store st, List<Player.Unit> units) {
        Widgets.Card card = new Widgets.Card(a, "Builder Base", GlyphView.VILLAGE);
        card.action(Screens.liveBadge(a, st));
        for (Player.Unit u : units) card.add(row(a, u));
        return card;
    }

    private static View row(Activity a, Player.Unit u) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(0, Skin.dp(a, 5), 0, Skin.dp(a, 5));

        LinearLayout head = Widgets.row(a);
        head.addView(Widgets.text(a, u.name, 14f, Palette.TEXT, true), Widgets.weight(1f));
        head.addView(Widgets.badge(a, "LV " + u.level + " / " + u.maxLevel,
                u.maxed() ? Palette.GREEN : Palette.accent));
        box.addView(head);

        LinearLayout.LayoutParams mp = Widgets.wide();
        mp.topMargin = Skin.dp(a, 4);
        box.addView(Widgets.meter(a, u.progress()), mp);
        return box;
    }

    private static int icon(String kind) {
        switch (kind) {
            case "hero": return GlyphView.CREST;
            case "spell": return GlyphView.ELIXIR;
            case "pet": return GlyphView.STAR;
            default: return GlyphView.ARMY;
        }
    }

    private static String percent(float fraction) {
        return Math.round(fraction * 100f) + "%";
    }

    private static View sourceNote(Activity a, Live live) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 4), Skin.dp(a, 2), Skin.dp(a, 4), Skin.dp(a, 2));
        TextView t = Widgets.body(a,
                "Levels came from " + (live.host.isEmpty() ? "the API" : live.host)
                        + " at the last sync. The API does not return research timers, so the "
                        + "app no longer invents one - it shows the level the server knows.");
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        box.addView(t);
        return box;
    }
}
