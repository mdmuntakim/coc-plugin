package plugin.clashofclans.ui;

import android.app.Activity;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Building;
import plugin.clashofclans.model.Catalog;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Loot;
import plugin.clashofclans.model.Season;

/** Season clock, loot calculator and the planner totals. */
public final class ToolsPanel {

    private ToolsPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        Screens.add(root, seasonCard(a));
        Screens.add(root, lootCard(a, st, refresh));
        Screens.add(root, plannerCard(a, st));
        Screens.add(root, note(a));
        return root;
    }

    private static View seasonCard(Activity a) {
        Widgets.Card card = new Widgets.Card(a, "Season clock", GlyphView.CLOCK);
        TextView label = Widgets.text(a, Season.label(System.currentTimeMillis()), 15f,
                Palette.TEXT, true);
        card.add(label);
        TextView reset = Widgets.text(a, "", 24f, Palette.accent, true);
        LinearLayout.LayoutParams p = Widgets.wide();
        p.topMargin = Skin.dp(a, 6);
        card.body.addView(reset, p);
        card.add(Widgets.text(a, "until the next season reset at 05:00 UTC", 11f,
                Palette.TEXT_DIM, false));
        Runnable tick = () -> {
            long now = System.currentTimeMillis();
            reset.setText(Fmt.duration(Season.millisUntilReset(now)));
            label.setText(Season.label(now));
        };
        tick.run();
        Widgets.everySecond(card, tick);
        return card;
    }

    private static View lootCard(Activity a, Store st, Runnable refresh) {
        Widgets.Card card = new Widgets.Card(a, "Loot calculator", GlyphView.GOLD);

        EditText gold = Chrome.input(a, "Gold per base", "800k",
                InputType.TYPE_CLASS_TEXT);
        EditText elixir = Chrome.input(a, "Elixir per base", "600k",
                InputType.TYPE_CLASS_TEXT);
        EditText percent = Chrome.input(a, "Destruction %", "70", InputType.TYPE_CLASS_NUMBER);
        EditText attacks = Chrome.input(a, "Attacks", "4", InputType.TYPE_CLASS_NUMBER);

        card.add(Screens.pair(a,
                Chrome.field(a, "Gold per base", gold),
                Chrome.field(a, "Elixir per base", elixir)));
        card.add(Screens.pair(a,
                Chrome.field(a, "Destruction %", percent),
                Chrome.field(a, "Attacks", attacks)));

        TextView result = Widgets.text(a, "Fill the numbers and calculate.", 12f,
                Palette.TEXT_DIM, false);
        LinearLayout.LayoutParams rp = Widgets.wide();
        rp.topMargin = Skin.dp(a, 8);
        card.body.addView(result, rp);

        LinearLayout actions = Widgets.row(a);
        LinearLayout.LayoutParams ap = Widgets.wide();
        ap.topMargin = Skin.dp(a, 8);
        actions.setLayoutParams(ap);

        TextView calc = Widgets.pillButton(a, "Calculate", true, () -> {
            int pct = Math.max(0, Math.min(100, Chrome.parseInt(percent.getText().toString(), 70)));
            int n = Math.max(0, Math.min(200, Chrome.parseInt(attacks.getText().toString(), 4)));
            long g = Loot.fromRaids(Chrome.parseLong(gold.getText().toString(), 0), pct, n);
            long e = Loot.fromRaids(Chrome.parseLong(elixir.getText().toString(), 0), pct, n);
            result.setTextColor(Palette.TEXT);
            result.setText(n + " raids at " + pct + "%: " + Fmt.shortNumber(g) + " gold  ·  "
                    + Fmt.shortNumber(e) + " elixir  ·  " + Fmt.shortNumber(g + e) + " total");
        });
        calc.setLayoutParams(Widgets.wide());
        actions.addView(calc);
        card.add(actions);

        card.add(Widgets.divider(a));
        card.add(Widgets.text(a,
                "Raid math uses the destruction share only, before the star bonus and the "
                        + "loot cart. Handy for a quick farm estimate.",
                10.5f, Palette.TEXT_DIM, false));
        return card;
    }

    private static View plannerCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Upgrade planner", GlyphView.BOLT);
        Map<String, Integer> levels = new HashMap<>();
        for (Building b : Catalog.BUILDINGS) levels.put(b.id, st.buildingLevel(b));

        int steps = 0;
        for (Building b : Catalog.BUILDINGS) steps += Math.max(0, b.maxLevel - st.buildingLevel(b));
        long gold = 0;
        for (Building b : Catalog.BUILDINGS) {
            if (b.currency != Building.GOLD) continue;
            for (int l = st.buildingLevel(b); l < b.maxLevel; l++) {
                gold += plugin.clashofclans.model.Upgrade.forBuilding(b, l).cost;
            }
        }
        long seconds = Catalog.secondsToMax(levels);

        card.add(Widgets.line(a, "Town Hall", String.valueOf(st.townHall()), Palette.accent));
        card.add(Widgets.line(a, "Upgrade steps left", String.valueOf(steps), Palette.TEXT));
        card.add(Widgets.line(a, "Gold-only cost", Fmt.shortNumber(gold) + " gold", Palette.accent));
        card.add(Widgets.line(a, "Builder time", Fmt.durationSeconds(seconds), Palette.TEXT));
        card.add(Widgets.line(a, "Five builders in parallel",
                Fmt.durationSeconds(Loot.parallelSeconds(seconds, Store.BUILDERS)), Palette.GREEN));
        card.add(Widgets.line(a, "Builders free",
                st.buildersFree() + " / " + Store.BUILDERS, Palette.GREEN));
        card.add(Widgets.line(a, "Upgrades known",
                String.valueOf(st.maxedBuildings()) + " maxed", Palette.TEXT));

        long target = 5_000_000L;
        long perRaid = Loot.fromAttack(800_000, 70);
        card.add(Widgets.line(a, "Raids for 5M gold",
                Loot.raidsNeeded(target, perRaid) + " at 70% of 800k", Palette.TEXT));
        return card;
    }

    private static View note(Activity a) {
        TextView t = Widgets.body(a,
                "Planner numbers are estimates built from the catalogue in the app. The live "
                        + "figures on Account, Army and War come from the official API; this tab "
                        + "does arithmetic, it does not claim to predict an exact in-game price.");
        t.setTextSize(10.5f);
        t.setTextColor(0x88A6B1BE);
        return t;
    }
}
