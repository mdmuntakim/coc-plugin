package plugin.clashofclans.ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Catalog;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Troop;
import plugin.clashofclans.model.Upgrade;

/** Laboratory queue and troop levels. */
public final class ArmyPanel {

    private ArmyPanel() {}

    public static View build(Activity a, Store st, Runnable refresh) {
        LinearLayout root = Widgets.column(a);
        List<Row> rows = new ArrayList<>();

        Screens.add(root, labCard(a, st));
        Screens.add(root, troopCard(a, st, refresh, rows));
        Screens.add(root, armySummary(a, st));

        Widgets.everySecond(root, () -> {
            long now = System.currentTimeMillis();
            boolean finished = false;
            long end = st.labEnd();
            if (end > 0 && end <= now) finished = true;
            for (Row r : rows) r.update(now);
            if (finished) {
                st.settle();
                refresh.run();
            }
        });
        return root;
    }

    private static View labCard(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Laboratory", GlyphView.ELIXIR);
        boolean active = st.labActive();
        Troop current = st.labTroopObject();

        card.action(Widgets.badge(a, active ? "RESEARCHING" : "IDLE",
                active ? Palette.ELIXIR : Palette.GREEN));

        if (active && current != null) {
            TextView name = Widgets.text(a, current.name, 17f, Palette.TEXT, true);
            card.add(name);
            TextView timer = Widgets.text(a, "", 12.5f, Palette.ELIXIR, true);
            card.body.addView(timer);

            LinearLayout.LayoutParams mp = Widgets.wide();
            mp.topMargin = Skin.dp(a, 7);
            View meter = Widgets.meter(a, 0f);
            card.body.addView(meter, mp);

            Widgets.everySecond(card, () -> {
                long end = st.labEnd();
                long start = st.labStart();
                long now = System.currentTimeMillis();
                timer.setText("Level " + st.troopLevel(current) + " \u2192 " + st.labTarget()
                        + "  ·  " + Fmt.duration(Math.max(0, end - now)) + " left");
                float p = (now - start) / (float) Math.max(1, end - start);
                Widgets.setMeter(meter, p);
            });
        } else {
            card.add(Widgets.body(a,
                    "The lab is free. Pick a troop below and start its next level; the timer "
                            + "keeps running while the app is closed."));
        }
        return card;
    }

    private static View troopCard(Activity a, Store st, Runnable refresh, List<Row> rows) {
        Widgets.Card card = new Widgets.Card(a, "Troop levels", GlyphView.ARMY);
        String house = "";
        for (Troop t : Catalog.TROOPS) {
            if (!house.equals(t.house)) {
                house = t.house;
                View header = Screens.sectionLabel(a, house);
                LinearLayout.LayoutParams hp = Widgets.wide();
                hp.topMargin = Skin.dp(a, 12);
                hp.bottomMargin = Skin.dp(a, 2);
                card.body.addView(header, hp);
            }
            Row r = new Row(a, st, t, refresh);
            rows.add(r);
            card.add(r.view);
        }
        return card;
    }

    private static View armySummary(Activity a, Store st) {
        Widgets.Card card = new Widgets.Card(a, "Army totals", GlyphView.STAR);
        int levels = 0;
        int maxed = 0;
        for (Troop t : Catalog.TROOPS) {
            int lv = st.troopLevel(t);
            levels += lv;
            if (lv >= t.maxLevel) maxed++;
        }
        int max = 0;
        for (Troop t : Catalog.TROOPS) max += t.maxLevel;
        float pct = max == 0 ? 0 : levels / (float) max;

        card.add(Widgets.line(a, "Troops unlocked", String.valueOf(Catalog.TROOPS.size()), Palette.TEXT));
        card.add(Widgets.line(a, "Maxed troops", maxed + " / " + Catalog.TROOPS.size(), Palette.GREEN));
        card.add(Widgets.line(a, "Total research level", levels + " / " + max, Palette.accent));
        LinearLayout.LayoutParams mp = Widgets.wide();
        mp.topMargin = Skin.dp(a, 7);
        card.body.addView(Widgets.meter(a, pct), mp);
        card.addGap();
        card.add(Widgets.text(a, Fmt.percent(pct) + " of the lab ladder complete",
                11f, Palette.TEXT_DIM, false));
        return card;
    }

    private static final class Row {
        final Troop troop;
        final Store store;
        final LinearLayout view;
        final TextView level;
        final TextView detail;
        final TextView action;
        final View meter;

        Row(Activity a, Store st, Troop t, Runnable refresh) {
            this.troop = t;
            this.store = st;
            view = Widgets.column(a);
            view.setPadding(0, Skin.dp(a, 7), 0, Skin.dp(a, 7));

            LinearLayout head = Widgets.row(a);
            head.addView(Widgets.text(a, t.name, 14.5f, Palette.TEXT, true), Widgets.weight(1f));
            level = Widgets.badge(a, "LV 1", t.dark ? Palette.DARK_ELIXIR : Palette.ELIXIR);
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

            action = Widgets.pillButton(a, "Research", true, () -> {
                if (store.labActive()) {
                    Chrome.toast(a, "The laboratory is busy");
                    return;
                }
                if (store.troopLevel(t) >= t.maxLevel) {
                    Chrome.toast(a, t.name + " is fully researched");
                    return;
                }
                if (store.startLab(t)) {
                    Chrome.toast(a, "Research started: " + t.name);
                    refresh.run();
                }
            });
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

        void update(long now) {
            int lv = store.troopLevel(troop);
            level.setText("LV " + lv + " / " + troop.maxLevel);
            boolean active = store.labActive()
                    && troop.id.equals(store.labTroop());
            long end = store.labEnd();

            if (active) {
                detail.setText("Researching Lv " + store.labTarget() + "  ·  "
                        + Fmt.duration(Math.max(0, end - now)) + " left");
                action.setText("Researching");
                action.setEnabled(false);
                meter.setVisibility(View.VISIBLE);
                float p = (now - store.labStart())
                        / (float) Math.max(1, end - store.labStart());
                Widgets.setMeter(meter, p);
            } else if (lv >= troop.maxLevel) {
                detail.setText("Maximum level reached");
                action.setVisibility(View.GONE);
                meter.setVisibility(View.GONE);
            } else {
                Upgrade u = Upgrade.forTroop(troop, lv);
                detail.setText("Next Lv " + (lv + 1) + "  ·  " + Fmt.shortNumber(u.cost) + " "
                        + troop.currencyName() + "  ·  " + Fmt.durationSeconds(u.seconds));
                action.setText("Research");
                action.setEnabled(true);
                action.setVisibility(View.VISIBLE);
                meter.setVisibility(View.GONE);
            }
        }
    }
}
