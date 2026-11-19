package plugin.clashofclans.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import plugin.clashofclans.data.Live;
import plugin.clashofclans.data.Store;
import plugin.clashofclans.data.Sync;
import plugin.clashofclans.model.Fmt;
import plugin.clashofclans.model.Tags;
import plugin.clashofclans.net.CocApi;

/**
 * The connection sheet. It collects the three things the official API needs -
 * player tag, developer token and the base URL - and tests them against the
 * real server before anything is saved.
 */
public final class ConnectDialog {

    private ConnectDialog() {}

    public static void show(Activity a, Store st, Runnable done) {
        LinearLayout box = Widgets.column(a);
        box.setPadding(Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 16), Skin.dp(a, 10));

        TextView title = Widgets.heading(a, "Live account");
        title.setTextColor(Palette.accent);
        title.setTextSize(18f);
        box.addView(title);

        TextView intro = Widgets.body(a,
                "COCPlugin now reads your real account from the official Clash of Clans API "
                        + "(api.clashofclans.com). Enter the player tag and a developer token; "
                        + "the token stays on this device.");
        intro.setTextSize(11.5f);
        intro.setTextColor(Palette.TEXT_DIM);
        LinearLayout.LayoutParams ip = Widgets.wide();
        ip.topMargin = Skin.dp(a, 5);
        box.addView(intro, ip);

        Live live = st.live();
        if (live.hasPlayer()) {
            TextView now = Widgets.text(a,
                    "Now showing " + live.player.name + " " + live.player.tag + "  ·  synced "
                            + Fmt.duration(System.currentTimeMillis() - live.fetchedAt) + " ago",
                    11f, Palette.GREEN, true);
            LinearLayout.LayoutParams lp = Widgets.wide();
            lp.topMargin = Skin.dp(a, 7);
            box.addView(now, lp);
        }

        EditText tag = Chrome.input(a, "#PLAYERTAG", st.tag(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText token = Chrome.input(a, "developer token", st.token(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        token.setTypeface(Typeface.MONOSPACE);
        EditText base = Chrome.input(a, CocApi.OFFICIAL_BASE, st.baseUrl(),
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        base.setTypeface(Typeface.MONOSPACE);

        box.addView(Chrome.field(a, "Player tag", tag));
        box.addView(Chrome.field(a, "API token", token));
        box.addView(Chrome.field(a, "API base URL (proxy optional)", base));

        TextView status = Widgets.text(a, "", 11.5f, Palette.TEXT_DIM, false);
        LinearLayout.LayoutParams sp = Widgets.wide();
        sp.topMargin = Skin.dp(a, 8);
        box.addView(status, sp);

        LinearLayout actions = Widgets.row(a);
        LinearLayout.LayoutParams ap = Widgets.wide();
        ap.topMargin = Skin.dp(a, 10);
        actions.setLayoutParams(ap);

        TextView test = Widgets.pillButton(a, "Test", false, () -> {
            String t = Tags.normalize(tag.getText().toString());
            if (t.isEmpty()) {
                status.setTextColor(Palette.RED);
                status.setText("That player tag is not valid.");
                return;
            }
            String url = base.getText().toString().trim();
            if (url.isEmpty()) url = CocApi.OFFICIAL_BASE;
            status.setTextColor(Palette.accent);
            status.setText("Asking the server…");
            Sync.probe(url, token.getText().toString(), t, (player, error) -> {
                if (error == null && player != null) {
                    status.setTextColor(Palette.GREEN);
                    status.setText("OK  " + player.name + "  ·  " + player.tag
                            + "  ·  TH " + player.townHallLevel
                            + "  ·  " + Fmt.number(player.trophies) + " trophies");
                } else {
                    status.setTextColor(Palette.RED);
                    status.setText(error == null ? "No reply" : error);
                }
            });
        });
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tp.rightMargin = Skin.dp(a, 4);
        test.setLayoutParams(tp);
        actions.addView(test);

        TextView connect = Widgets.pillButton(a, "Save and sync", true, null);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f);
        cp.leftMargin = Skin.dp(a, 4);
        connect.setLayoutParams(cp);
        actions.addView(connect);
        box.addView(actions);

        box.addView(Widgets.divider(a));
        TextView help = Widgets.body(a,
                "How to get a token\n"
                        + "1. Open developer.clashofclans.com and sign in with a Supercell ID.\n"
                        + "2. My Account → Create New API Key. Name it COCPlugin and describe it.\n"
                        + "3. Copy the token and paste it above, then press Test.\n\n"
                        + "The token is locked to the IP address it was created for. Mobile data "
                        + "changes that address, and a mismatch answers 403. Either create the key "
                        + "with a wide IP range, or point the base URL at the small proxy in "
                        + "proxy/worker.js so the token lives on the proxy and never on the phone.\n\n"
                        + "Only your own token is used; nothing is bundled and nothing is shared.");
        help.setTextSize(10.5f);
        help.setTextColor(Palette.TEXT_DIM);
        box.addView(help);

        TextView forget = Widgets.text(a, "Forget this account and token", 12f, Palette.RED, true);
        forget.setPadding(Skin.dp(a, 10), Skin.dp(a, 10), Skin.dp(a, 10), Skin.dp(a, 10));
        forget.setBackground(Skin.stroke(0x33E2635B, 0xAAE2635B, a, 9f, 1f));
        LinearLayout.LayoutParams fp = Widgets.wide();
        fp.topMargin = Skin.dp(a, 12);
        box.addView(forget, fp);
        ScrollView scroll = new ScrollView(a);
        scroll.addView(box);
        AlertDialog d = new AlertDialog.Builder(a)
                .setView(scroll)
                .setNegativeButton("Close", null)
                .create();
        d.show();
        Chrome.styleDialogTall(a, d);

        forget.setOnClickListener(v -> new AlertDialog.Builder(a)
                .setTitle("Forget this account?")
                .setMessage("The token and the cached account are removed from this device. "
                        + "You can connect again at any time.")
                .setNegativeButton("Keep", null)
                .setPositiveButton("Forget", (yes, which) -> {
                    st.disconnect();
                    d.dismiss();
                    Chrome.toast(a, "Account and token removed from this device");
                    done.run();
                })
                .show());

        connect.setOnClickListener(v -> {
            String normalized = Tags.normalize(tag.getText().toString());
            if (normalized.isEmpty()) {
                tag.setError("Tags use 0 2 8 9 P Y L Q G R J C U V");
                status.setTextColor(Palette.RED);
                status.setText("That player tag is not valid.");
                return;
            }
            String rawToken = token.getText().toString().trim();
            if (rawToken.isEmpty()) {
                status.setTextColor(Palette.RED);
                status.setText("Paste the developer token from developer.clashofclans.com.");
                return;
            }
            String url = base.getText().toString().trim();
            st.setTag(normalized);
            st.setToken(rawToken);
            st.setBaseUrl(url);
            Chrome.hideKeyboard(a, token);
            d.dismiss();
            done.run();
        });
    }

}
