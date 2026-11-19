package plugin.clashofclans;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import plugin.clashofclans.data.Store;
import plugin.clashofclans.ui.AccountPanel;
import plugin.clashofclans.ui.ArmyPanel;
import plugin.clashofclans.ui.Chrome;
import plugin.clashofclans.ui.Dialogs;
import plugin.clashofclans.ui.Palette;
import plugin.clashofclans.ui.Skin;
import plugin.clashofclans.ui.ToolsPanel;
import plugin.clashofclans.ui.VillagePanel;
import plugin.clashofclans.ui.WarPanel;
import plugin.clashofclans.ui.Widgets;

/**
 * Single-activity shell: themed top bar, one scrollable screen and the five
 * bottom tabs. Everything it shows comes from the local {@link Store}.
 */
public final class MainActivity extends Activity {

    private static final String STATE_TAB = "tab";

    private Store store;
    private Chrome.Header header;
    private FrameLayout content;
    private LinearLayout navHost;
    private int tab = 0;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        store = new Store(this);
        Palette.applyAccent(store.theme());

        getWindow().setStatusBarColor(Palette.BG);
        getWindow().setNavigationBarColor(0xFF080B0F);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
        }

        LinearLayout shell = Widgets.column(this);
        shell.setBackground(Skin.backdrop(this));

        header = Chrome.topBar(this, store, this::openSettings);
        shell.addView(header.view);
        View hairline = new View(this);
        hairline.setBackgroundColor(0x33FFFFFF);
        shell.addView(hairline, Widgets.lp(ViewGroup.LayoutParams.MATCH_PARENT,
                Math.max(1, Skin.dp(this, 1))));

        content = new FrameLayout(this);
        shell.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        navHost = Widgets.column(this);
        shell.addView(navHost, Widgets.wide());

        setContentView(shell);
        if (state != null) tab = state.getInt(STATE_TAB, 0);
        show(tab);
    }

    private void openSettings() {
        Dialogs.settings(this, store, () -> show(tab));
    }

    private void show(int index) {
        tab = Math.max(0, Math.min(4, index));
        header.refresh(store);
        content.removeAllViews();

        View panel = buildPanel(tab);
        ScrollView scroll = Chrome.scrollWrap(this, panel);
        scroll.setVerticalScrollBarEnabled(false);
        content.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        panel.setAlpha(0f);
        panel.setTranslationY(Skin.dp(this, 14));
        panel.animate().alpha(1f).translationY(0f).setDuration(190L).start();

        navHost.removeAllViews();
        navHost.addView(Chrome.navBar(this, tab, this::show));
    }

    private View buildPanel(int index) {
        switch (index) {
            case 1: return VillagePanel.build(this, store, () -> show(1));
            case 2: return ArmyPanel.build(this, store, () -> show(2));
            case 3: return WarPanel.build(this, store, () -> show(3));
            case 4: return ToolsPanel.build(this, store, () -> show(4));
            default: return AccountPanel.build(this, store, () -> show(0));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.settle();
        show(tab);
    }

    @Override
    public void onBackPressed() {
        if (tab != 0) {
            show(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt(STATE_TAB, tab);
    }
}
