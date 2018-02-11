package com.vlousada.legacylenses;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.github.ma1co.pmcademo.app.BaseActivity;


public class TabSettingsActivity extends BaseActivity {
    private Button m_button;
    private CheckBox m_cbSort;
    private CheckBox m_cbLRKeys;
    private Spinner m_spCoc;
    private Preferences     prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_settings);

        prefs = new Preferences(this);
        //init controls
        m_button = (Button) findViewById(R.id.bt_saveSettings);
        m_cbSort = (CheckBox) findViewById(R.id.cb_sortLenses);
        m_cbLRKeys = (CheckBox) findViewById(R.id.cb_enableLRkeys);
        m_spCoc = (Spinner) findViewById(R.id.sp_coc);

        //LLUtils.log("*** Create Settings Activity ***" + "\n");
        //LLUtils.log("Sort: " + prefs.getSettingLensesSorted() + "    ");
        //LLUtils.log("LRKeys: " + prefs.getSettingUseLeftRightKeys() + "    ");
        //LLUtils.log("Coc:" + prefs.getSettingCoC() + "\n");

        m_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //LLUtils.log("*** Click before FINISH ***" + "\n");

                finish();
               // onBackPressed();
            }
        });

        m_cbSort.setChecked(prefs.getSettingLensesSorted().equalsIgnoreCase("YES")? true : false);
        m_cbLRKeys.setChecked(prefs.getSettingUseLeftRightKeys().equalsIgnoreCase("YES")? true : false);


        Integer i = getIndex(m_spCoc,prefs.getSettingCoC());
        if (i>0)
            m_spCoc.setSelection(i);
        else
            m_spCoc.setSelection(0);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        prefs.setSettingLensesSorted(m_cbSort.isChecked() ? "YES" : "NO");
        prefs.setSettingUseLeftRightKeys(m_cbLRKeys.isChecked() ? "YES" : "NO");
        prefs.setSettingCoc(m_spCoc.getSelectedItem().toString());
        finish();


    }


    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    @Override
    protected boolean onPlayKeyDown()
    {
        //take screenshot?
        Bitmap bitmap = LLUtils.takeScreenshot(this.findViewById(android.R.id.content).getRootView());
        LLUtils.saveBitmap(bitmap,3);
        return true;
    }
}
