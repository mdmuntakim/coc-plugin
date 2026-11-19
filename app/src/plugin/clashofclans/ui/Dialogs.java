package plugin.clashofclans.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.model.Tags;

/** The themed forms of the app. */
public final class Dialogs {

    private Dialogs() {}

    public static void editProfile(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 8));

        TextView title = Widgets.heading(a, "Chief profile");
        title.setTextSize(17f);
        title.setTextColor(Palette.accent);
        box.addView(title);
        TextView note = Widgets.body(a,
                "Everything is stored on this device. Nothing is read from or sent to the game.");
        note.setTextSize(11.5f);
        note.setTextColor(Palette.TEXT_DIM);
        LinearLayout.LayoutParams np = Widgets.wide();
        np.topMargin = Skin.dp(a, 4);
        box.addView(note, np);

        EditText name = Chrome.input(a, "Chief name", st.name(), InputType.TYPE_CLASS_TEXT);
        EditText tag = Chrome.input(a, "#PLAYERTAG", st.tag(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText th = Chrome.input(a, "Town Hall", String.valueOf(st.th()), InputType.TYPE_CLASS_NUMBER);
        EditText xp = Chrome.input(a, "Experience", String.valueOf(st.xp()), InputType.TYPE_CLASS_NUMBER);
        EditText trophies = number(a, "Trophies", st.trophies());
        EditText best = number(a, "Best trophies", st.bestTrophies());
        EditText builder = number(a, "Builder Base trophies", st.builderTrophies());
        EditText stars = number(a, "War stars", st.warStars());
        EditText donations = number(a, "Troops donated", st.donations());
        EditText received = number(a, "Troops received", st.received());
        EditText clan = Chrome.input(a, "Clan name", st.clanName(), InputType.TYPE_CLASS_TEXT);
        EditText clanTag = Chrome.input(a, "#CLANTAG", st.clanTag(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText clanLevel = number(a, "Clan level", st.clanLevel());
        EditText members = number(a, "Clan members", st.members());

        box.addView(Chrome.field(a, "Name", name));
        box.addView(Chrome.field(a, "Player tag", tag));
        box.addView(twoUp(a, Chrome.field(a, "Town Hall (1-17)", th),
                Chrome.field(a, "Experience", xp)));
        box.addView(twoUp(a, Chrome.field(a, "Trophies", trophies),
                Chrome.field(a, "Best trophies", best)));
        box.addView(twoUp(a, Chrome.field(a, "Builder Base", builder),
                Chrome.field(a, "War stars", stars)));
        box.addView(twoUp(a, Chrome.field(a, "Donated", donations),
                Chrome.field(a, "Received", received)));
        box.addView(Chrome.field(a, "Clan name", clan));
        box.addView(twoUp(a, Chrome.field(a, "Clan tag", clanTag),
                Chrome.field(a, "Clan level", clanLevel)));
        box.addView(Chrome.field(a, "Clan members", members));

        ScrollView scroll = new ScrollView(a);
        scroll.addView(box);

        AlertDialog d = new AlertDialog.Builder(a)
                .setView(scroll)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();
        d.show();
        Chrome.styleDialogTall(a, d);
        d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String normalized = Tags.normalize(tag.getText().toString());
            if (normalized.isEmpty()) {
                tag.setError("Tags use 0 2 8 9 P Y L Q G R J C U V");
                Chrome.toast(a, "That player tag is not valid");
                return;
            }
            st.setName(name.getText().toString());
            st.setTag(normalized);
            st.setTh(Chrome.parseInt(th.getText().toString(), st.th()));
            st.setXp(Chrome.parseLong(xp.getText().toString(), st.xp()));
            st.setTrophies(Chrome.parseInt(trophies.getText().toString(), st.trophies()));
            st.setBestTrophies(Chrome.parseInt(best.getText().toString(), st.bestTrophies()));
            st.setBuilderTrophies(Chrome.parseInt(builder.getText().toString(), st.builderTrophies()));
            st.setWarStars(Chrome.parseInt(stars.getText().toString(), st.warStars()));
            st.setDonations(Chrome.parseInt(donations.getText().toString(), st.donations()));
            st.setReceived(Chrome.parseInt(received.getText().toString(), st.received()));
            st.setClan(clan.getText().toString(), clanTag.getText().toString(),
                    Chrome.parseInt(clanLevel.getText().toString(), st.clanLevel()));
            st.setMembers(Chrome.parseInt(members.getText().toString(), st.members()));
            d.dismiss();
            done.run();
        });
    }

    private static EditText number(Activity a, String hint, int value) {
        return Chrome.input(a, hint, String.valueOf(value), InputType.TYPE_CLASS_NUMBER);
    }

    private static View twoUp(Activity a, View left, View right) {
        LinearLayout row = Widgets.row(a);
        row.setBaselineAligned(false);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp1.rightMargin = Skin.dp(a, 5);
        left.setLayoutParams(lp1);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp2.leftMargin = Skin.dp(a, 5);
        right.setLayoutParams(lp2);
        row.addView(left);
        row.addView(right);
        row.setLayoutParams(Widgets.wide());
        return row;
    }

    public static void newWar(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 8));
        TextView title = Widgets.heading(a, "Start a war");
        title.setTextColor(Palette.accent);
        title.setTextSize(17f);
        box.addView(title);

        EditText opponent = Chrome.input(a, "Opponent clan", "", InputType.TYPE_CLASS_TEXT);
        EditText hours = Chrome.input(a, "Duration in hours", "24", InputType.TYPE_CLASS_NUMBER);
        EditText theirStars = Chrome.input(a, "Their stars (optional)", "0", InputType.TYPE_CLASS_NUMBER);

        box.addView(Chrome.field(a, "Opponent", opponent));
        box.addView(twoUp(a, Chrome.field(a, "Hours", hours),
                Chrome.field(a, "Enemy stars", theirStars)));

        AlertDialog d = new AlertDialog.Builder(a)
                .setView(box)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Start", null)
                .create();
        d.show();
        Chrome.styleDialog(d, a);
        d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            int h = Math.max(1, Math.min(48, Chrome.parseInt(hours.getText().toString(), 24)));
            st.newWar(opponent.getText().toString(), h * 3600);
            st.setTheirStars(Chrome.parseInt(theirStars.getText().toString(), 0));
            d.dismiss();
            done.run();
        });
    }

    public static void recordAttack(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 14), Skin.dp(a, 16), Skin.dp(a, 10));
        TextView title = Widgets.heading(a, "Record an attack");
        title.setTextColor(Palette.accent);
        title.setTextSize(16f);
        box.addView(title);
        TextView note = Widgets.body(a,
                "Each member has two war attacks. The stars are added to the clan total.");
        note.setTextSize(11.5f);
        note.setTextColor(Palette.TEXT_DIM);
        box.addView(note);

        AlertDialog d = new AlertDialog.Builder(a)
                .setView(box)
                .setNegativeButton("Cancel", null)
                .create();
        d.show();
        Chrome.styleDialog(d, a);

        LinearLayout buttons = Widgets.row(a);
        LinearLayout.LayoutParams bp = Widgets.wide();
        bp.topMargin = Skin.dp(a, 12);
        buttons.setLayoutParams(bp);
        for (int stars = 1; stars <= 3; stars++) {
            final int s = stars;
            TextView b = Widgets.text(a, stars + " \u2605", 16f, 0xFF2A2013, true);
            b.setGravity(Gravity.CENTER);
            b.setPadding(Skin.dp(a, 10), Skin.dp(a, 12), Skin.dp(a, 10), Skin.dp(a, 12));
            b.setBackground(Skin.goldButton(a, 10f));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p.leftMargin = Skin.dp(a, stars == 1 ? 0 : 4);
            p.rightMargin = Skin.dp(a, stars == 3 ? 0 : 4);
            b.setLayoutParams(p);
            b.setOnClickListener(v -> {
                st.addAttack(s);
                d.dismiss();
                done.run();
            });
            buttons.addView(b);
        }
        box.addView(buttons);
    }

    public static void settings(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 6));

        TextView title = Widgets.heading(a, "Settings");
        title.setTextColor(Palette.accent);
        title.setTextSize(18f);
        box.addView(title);

        box.addView(Widgets.gap(a, 12));
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

        box.addView(Widgets.gap(a, 16));
        box.addView(Widgets.label(a, "Data"));
        TextView reset = Widgets.text(a, "Reset the local profile", 13f, Palette.RED, true);
        reset.setPadding(Skin.dp(a, 10), Skin.dp(a, 11), Skin.dp(a, 10), Skin.dp(a, 11));
        reset.setBackground(Skin.stroke(0x33E2635B, 0xAAE2635B, a, 9f, 1f));
        box.addView(reset);
        reset.setOnClickListener(v -> new AlertDialog.Builder(a)
                .setTitle("Reset profile?")
                .setMessage("Village levels, timers, war log and resources go back to the demo profile.")
                .setNegativeButton("Keep", null)
                .setPositiveButton("Reset", (dd, w) -> {
                    st.reset();
                    Palette.applyAccent(st.theme());
                    a.recreate();
                })
                .show());

        box.addView(Widgets.gap(a, 18));
        box.addView(Widgets.label(a, "About"));
        TextView about = Widgets.body(a, aboutText(a));
        about.setTextSize(11.5f);
        about.setTextColor(Palette.TEXT_DIM);
        box.addView(about);
        box.addView(Widgets.gap(a, 4));
        TextView legal = Widgets.body(a,
                "COCPlugin is an unofficial fan-made companion. It is not affiliated with, "
                        + "endorsed by or connected to Supercell. It does not read, modify or "
                        + "automate the game and ships no game artwork or fonts. Names of game "
                        + "content are used only to describe the planner fields.");
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
        String version = "1.0.0";
        try {
            version = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
        } catch (Exception ignored) {
        }
        return "COCPlugin " + version + "  ·  package plugin.clashofclans\n"
                + "Account status, village planner, laboratory queue, war tracker, loot and "
                + "season tools. Everything works offline.";
    }
}
