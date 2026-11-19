package plugin.clashofclans.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Building;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Tags;

/** The themed forms of the app. */
public final class Dialogs {

    private Dialogs() {}

    // ------------------------------------------------------------ planner
    public static void setBuildingLevel(Activity a, Store st, Building b, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 8));

        TextView title = Widgets.heading(a, b.name);
        title.setTextSize(17f);
        title.setTextColor(Palette.accent);
        box.addView(title);
        TextView note = Widgets.body(a,
                "Type the level you really have in the game. The API has no building endpoint, "
                        + "so the planner trusts this number. Maximum is " + b.maxLevel + ".");
        note.setTextSize(11.5f);
        note.setTextColor(Palette.TEXT_DIM);
        LinearLayout.LayoutParams np = Widgets.wide();
        np.topMargin = Skin.dp(a, 4);
        box.addView(note, np);

        EditText level = Chrome.input(a, "Level 1-" + b.maxLevel,
                String.valueOf(st.buildingLevel(b)), InputType.TYPE_CLASS_NUMBER);
        box.addView(Chrome.field(a, "Current level", level));

        AlertDialog d = new AlertDialog.Builder(a)
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();
        d.show();
        Chrome.styleDialog(d, a);
        d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            int value = Chrome.parseInt(level.getText().toString(), st.buildingLevel(b));
            st.setBuildingLevel(b, value);
            d.dismiss();
            done.run();
        });
    }

    public static void setTownHall(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 8));
        TextView title = Widgets.heading(a, "Town Hall level");
        title.setTextColor(Palette.accent);
        title.setTextSize(17f);
        box.addView(title);
        TextView note = Widgets.body(a,
                "When a player is synced the Town Hall level comes from the server; this field "
                        + "is the fallback for a planner used without a connection.");
        note.setTextSize(11.5f);
        note.setTextColor(Palette.TEXT_DIM);
        box.addView(note);

        EditText level = Chrome.input(a, "0 to unset",
                String.valueOf(st.townHall()), InputType.TYPE_CLASS_NUMBER);
        box.addView(Chrome.field(a, "Town Hall", level));

        AlertDialog d = new AlertDialog.Builder(a)
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();
        d.show();
        Chrome.styleDialog(d, a);
        d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            st.setTownHall(Chrome.parseInt(level.getText().toString(), st.townHall()));
            d.dismiss();
            done.run();
        });
    }

    // ----------------------------------------------------------- settings
    public static void settings(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 6));

        TextView title = Widgets.heading(a, "Settings");
        title.setTextColor(Palette.accent);
        title.setTextSize(18f);
        box.addView(title);

        // ------------------------------------------------------- connection
        box.addView(Widgets.gap(a, 12));
        box.addView(Widgets.label(a, "Live account"));
        Live live = st.live();
        LinearLayout server = Widgets.row(a);
        server.addView(Widgets.text(a, live.hasPlayer() ? live.player.name : "not connected",
                13f, live.hasPlayer() ? Palette.GREEN : Palette.TEXT_DIM, true), Widgets.weight(1f));
        server.addView(Widgets.text(a, live.hasPlayer() ? live.player.tag : st.tag(),
                11f, Palette.TEXT_DIM, true));
        box.addView(server);
        box.addView(Widgets.line(a, "Server", st.host(), Palette.TEXT_DIM));
        box.addView(Widgets.line(a, "Last sync",
                live.fetchedAt == 0 ? "never"
                        : Fmt.duration(System.currentTimeMillis() - live.fetchedAt) + " ago",
                Palette.TEXT_DIM));
        TextView connect = Widgets.pillButton(a, live.hasPlayer()
                ? "Connection settings" : "Connect account", true,
                () -> ConnectDialog.show(a, st, done));
        LinearLayout.LayoutParams cp = Widgets.wide();
        cp.topMargin = Skin.dp(a, 8);
        box.addView(connect, cp);

        // ------------------------------------------------------------ theme
        box.addView(Widgets.gap(a, 16));
        box.addView(Widgets.label(a, "Crown accent"));
        LinearLayout themes = Widgets.row(a);
        themes.setLayoutParams(Widgets.wide());
        final int[] preview = {0xFFF3C65A, 0xFFC88BFF, 0xFF6FD3F0};
        for (int i = 0; i < 3; i++) {
            final int index = i;
            LinearLayout option = Widgets.column(a);
            option.setGravity(Gravity.CENTER);
            option.setPadding(Skin.dp(a, 6), Skin.dp(a, 8), Skin.dp(a, 6), Skin.dp(a, 8));
            option.setBackground(st.theme() == i
                    ? Skin.stroke(0x22202020, preview[i], a, 10f, 2f)
                    : Skin.inset(a));
            View dot = new View(a);
            dot.setBackground(Skin.round(preview[i], a, 20f));
            dot.setLayoutParams(new LinearLayout.LayoutParams(Skin.dp(a, 22), Skin.dp(a, 22)));
            option.addView(dot);
            TextView name = Widgets.text(a, Palette.accentName(i), 10.5f,
                    st.theme() == i ? Palette.TEXT : Palette.TEXT_DIM, true);
            name.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams np = Widgets.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            np.topMargin = Skin.dp(a, 5);
            name.setLayoutParams(np);
            option.addView(name);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            op.leftMargin = i == 0 ? 0 : Skin.dp(a, 4);
            op.rightMargin = i == 2 ? 0 : Skin.dp(a, 4);
            option.setLayoutParams(op);
            option.setOnClickListener(v -> {
                st.setTheme(index);
                Palette.applyAccent(index);
                a.recreate();
            });
            themes.addView(option);
        }
        box.addView(themes);

        // ---------------------------------------------------------- planner
        box.addView(Widgets.gap(a, 16));
        box.addView(Widgets.label(a, "Planner"));
        TextView reset = Widgets.text(a, "Clear the local building levels", 13f, Palette.RED, true);
        reset.setPadding(Skin.dp(a, 10), Skin.dp(a, 11), Skin.dp(a, 10), Skin.dp(a, 11));
        reset.setBackground(Skin.stroke(0x33E2635B, 0xAAE2635B, a, 9f, 1f));
        box.addView(reset);
        reset.setOnClickListener(v -> new AlertDialog.Builder(a)
                .setTitle("Clear the planner?")
                .setMessage("Building levels and running upgrade timers go back to level 1. "
                        + "The live account and the token are not touched.")
                .setNegativeButton("Keep", null)
                .setPositiveButton("Clear", (dd, w) -> {
                    st.resetPlanner();
                    done.run();
                    a.recreate();
                })
                .show());

        // ------------------------------------------------------------ about
        box.addView(Widgets.gap(a, 18));
        box.addView(Widgets.label(a, "About"));
        TextView about = Widgets.body(a, aboutText(a));
        about.setTextSize(11.5f);
        about.setTextColor(Palette.TEXT_DIM);
        box.addView(about);
        box.addView(Widgets.gap(a, 4));
        TextView legal = Widgets.body(a,
                "COCPlugin is an unofficial fan-made companion. It is not affiliated with, "
                        + "endorsed by or connected to Supercell. It reads the public developer "
                        + "API with the token you supply, cannot log into the game, cannot change "
                        + "an account and ships no game artwork or fonts. Clash of Clans is a "
                        + "trademark of Supercell.");
        legal.setTextSize(10.5f);
        legal.setTextColor(0x99A6B1BE);
        box.addView(legal);

        ScrollView scroll = new ScrollView(a);
        scroll.addView(box);
        AlertDialog d = new AlertDialog.Builder(a).setView(scroll)
                .setPositiveButton("Close", null).create();
        d.show();
        Chrome.styleDialog(d, a);
        d.setOnDismissListener(x -> done.run());
    }

    private static String aboutText(Activity a) {
        String version = "2.0.0";
        try {
            version = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        return "COCPlugin " + version + "  ·  package plugin.clashofclans\n"
                + "Live account, clan, war and unit data from the official Clash of Clans API, "
                + "plus a local building planner.";
    }

}
