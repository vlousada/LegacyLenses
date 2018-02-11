package com.vlousada.legacylenses;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class MenuActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addTab("lenses", "Lenses", android.R.drawable.ic_menu_camera, TabLensesActivity.class);
        addTab("special", "Special", android.R.drawable.ic_input_get, TabSpecialActivity.class);
        addTab("settings", "Settings", android.R.drawable.ic_menu_manage, TabSettingsActivity.class);

    }

    protected void addTab(String tag, String label, int iconId, Class activity) {
        TabHost.TabSpec tab = getTabHost().newTabSpec(tag);
        tab.setIndicator(label, getResources().getDrawable(iconId));
        tab.setContent(new Intent(this, activity));
        getTabHost().addTab(tab);
        //getTabHost().setCurrentTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}