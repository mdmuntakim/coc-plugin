package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Building;
import plugin.clashofclans.model.Catalog;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Loot;
import plugin.clashofclans.model.Upgrade;

/** Village overview and the builder queue. */
public final class VillagePanel {

    private VillagePanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        List<Row> rows = new ArrayList<>();

        Screens.add(root, builderCard(a, st));
        Screens.add(root, upgradeCard(a, st, refresh, rows));
        Screens.add(root, summaryCard(a, st));

        Widgets.everySecond(root, () -> {
            long now = System.currentTimeMillis();
            boolean finished = false;
            for (Row r : rows) {
                long end = st.upgradeEnd(r.building);
                if (end > 0 && end <= now) finished = true;
                r.update(now);
            }
            if (finished) {
                st.settle();
                refresh.run();
            }
        });
        return root;
    }

    private static View builderCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Builders", GlyphView.TOOLS);
        card.action(Widgets.badge(a, st.buildersFree() + " / " + Store.BUILDERS + " FREE",
                st.buildersFree() > 0 ? Palette.GREEN : Palette.RED));

        LinearLayout slots = Widgets.row(a);
        for (int i = 0; i < Store.BUILDERS; i++) {
            boolean free = i < st.buildersFree();
            GlyphView g = Widgets.glyph(a, GlyphView.TOOLS,
                    free ? Palette.GREEN : 0x663A4552, 22);
            LinearLayout slot = Widgets.column(a);
            slot.setGravity(Gravity.CENTER);
            slot.setBackground(Skin.stroke(free ? 0x226FD98A : 0x22000000,
                    free ? 0x886FD98A : 0x33505B68, a, 10f, 1f));
            slot.setPadding(Skin.dp(a, 6), Skin.dp(a, 6), Skin.dp(a, 6), Skin.dp(a, 6));
            slot.addView(g);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p.rightMargin = Skin.dp(a, i == Store.BUILDERS - 1 ? 0 : 6);
            slot.setLayoutParams(p);
            slots.addView(slot);
        }
        card.add(slots);
        card.addGap();

        long next = 0;
        String nextName = "";
        for (Building b : Catalog.BUILDINGS) {
            long end = st.upgradeEnd(b);
            if (end > System.currentTimeMillis() && (next == 0 || end < next)) {
                next = end;
                nextName = b.name;
            }
        }
        if (next > 0) {
            card.add(Widgets.line(a, "Next builder free",
                    Fmt.duration(next - System.currentTimeMillis()) + " · " + nextName,
                    Palette.accent));
        } else {
            card.add(Widgets.line(a, "All builders", "Idle, pick an upgrade", Palette.GREEN));
        }
        return card;
    }

    private static View upgradeCard(Activity a, Store st, Runnable refresh, List<Row> rows) {
        Widgets.Card card = new Widgets.Card(a, "Upgrade queue", GlyphView.BOLT);

        List<Building> sorted = new ArrayList<>(Catalog.BUILDINGS);
        sorted.sort((x, y) -> {
            int c = x.category.compareTo(y.category);
            return c != 0 ? c : x.name.compareTo(y.name);
        });

        String category = "";
        for (Building b : sorted) {
            if (!category.equals(b.category)) {
                category = b.category;
                View header = Screens.sectionLabel(a, category);
                LinearLayout.LayoutParams hp = Widgets.wide();
                hp.topMargin = Skin.dp(a, 12);
                hp.bottomMargin = Skin.dp(a, 2);
                card.body.addView(header, hp);
            }
            Row row = new Row(a, st, b, refresh);
            rows.add(row);
            card.add(row.view);
        }
        return card;
    }

    private static View summaryCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Planner totals", GlyphView.CHECK);
        Map<String, Integer> levels = new HashMap<>();
        for (Building b : Catalog.BUILDINGS) levels.put(b.id, st.buildingLevel(b));

        int steps = 0;
        for (Building b : Catalog.BUILDINGS) steps += Math.max(0, b.maxLevel - st.buildingLevel(b));
        long seconds = Catalog.secondsToMax(levels);
        long gold = 0;
        for (Building b : Catalog.BUILDINGS) {
            if (b.currency != Building.GOLD) continue;
            for (int l = st.buildingLevel(b); l < b.maxLevel; l++) {
                gold += Upgrade.forBuilding(b, l).cost;
            }
        }

        card.add(Widgets.line(a, "Upgrade steps left", String.valueOf(steps), Palette.accent));
        card.add(Widgets.line(a, "Gold-only cost to max", Fmt.shortNumber(gold) + " gold", Palette.accent));
        card.add(Widgets.line(a, "Total build time", Fmt.durationSeconds(seconds), Palette.TEXT));
        card.add(Widgets.line(a, "With five builders",
                Fmt.durationSeconds(Loot.parallelSeconds(seconds, Store.BUILDERS)), Palette.TEXT));
        card.add(Widgets.line(a, "Maxed buildings",
                st.maxedBuildings() + " / " + Catalog.BUILDINGS.size(), Palette.GREEN));
        return card;
    }

    /** One building row that keeps its own countdown. */
    private static final class Row {
        final Building building;
        final Store store;
        final LinearLayout view;
        final TextView level;
        final TextView detail;
        final TextView action;
        final View meter;

        Row(Activity a, Store st, Building b, Runnable refresh) {
            this.building = b;
            this.store = st;
            view = Widgets.column(a);
            view.setPadding(0, Skin.dp(a, 7), 0, Skin.dp(a, 7));

            LinearLayout head = Widgets.row(a);
            TextView name = Widgets.text(a, b.name, 14.5f, Palette.TEXT, true);
            head.addView(name, Widgets.weight(1f));
            level = Widgets.badge(a, "LV 1", Palette.accent);
            head.addView(level);
            view.addView(head);

            LinearLayout body = Widgets.row(a);
            LinearLayout.LayoutParams bp = Widgets.wide();
            bp.topMargin = Skin.dp(a, 3);
            body.setLayoutParams(bp);

            detail = Widgets.text(a, "", 11f, Palette.TEXT_DIM, false);
            detail.setSingleLine(true);
            detail.setEllipsize(android.text.TextUtils.TruncateAt.END);
            body.addView(detail, Widgets.weight(1f));

            action = Widgets.pillButton(a, "Upgrade", true, () -> onUpgrade(a, st, b, refresh));
            LinearLayout.LayoutParams ap = Widgets.lp(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            ap.leftMargin = Skin.dp(a, 8);
            action.setLayoutParams(ap);
            body.addView(action);
            view.addView(body);

            meter = Widgets.meter(a, 0f);
            LinearLayout.LayoutParams mp = Widgets.wide();
            mp.topMargin = Skin.dp(a, 6);
            meter.setLayoutParams(mp);
            view.addView(meter);

            update(System.currentTimeMillis());
        }

        void onUpgrade(Activity a, Store st, Building b, Runnable refresh) {
            if (st.buildingLevel(b) >= b.maxLevel) {
                Chrome.toast(a, b.name + " is already at the maximum level");
                return;
            }
            if (st.isUpgrading(b)) {
                Chrome.toast(a, b.name + " is already upgrading");
                return;
            }
            if (st.buildersFree() <= 0) {
                Chrome.toast(a, "All five builders are busy. Finish an upgrade first.");
                return;
            }
            if (st.startUpgrade(b)) {
                Chrome.toast(a, b.name + " upgrade started");
                refresh.run();
            }
        }

        void update(long now) {
            int lv = store.buildingLevel(building);
            level.setText("LV " + lv + " / " + building.maxLevel);
            long end = store.upgradeEnd(building);
            long start = store.upgradeStart(building);
            boolean active = end > now;
            int target = store.upgradeTarget(building);

            if (active) {
                detail.setText("Upgrading to Lv " + target + "  ·  "
                        + Fmt.duration(end - now) + " left");
                action.setText("In progress");
                action.setEnabled(false);
                meter.setVisibility(View.VISIBLE);
                float p = (now - start) / (float) Math.max(1, end - start);
                Widgets.setMeter(meter, p);
            } else if (lv >= building.maxLevel) {
                detail.setText("Maximum level reached");
                action.setVisibility(View.GONE);
                meter.setVisibility(View.GONE);
            } else {
                Upgrade u = Upgrade.forBuilding(building, lv);
                detail.setText("Next Lv " + (lv + 1) + "  ·  " + Fmt.shortNumber(u.cost) + " "
                        + building.currencyName() + "  ·  " + Fmt.durationSeconds(u.seconds));
                action.setText("Upgrade");
                action.setEnabled(true);
                action.setVisibility(View.VISIBLE);
                meter.setVisibility(View.GONE);
            }
        }
    }
}
