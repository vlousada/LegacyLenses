package com.vlousada.legacylenses;


import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;


public class SetFocalLenghtActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener
{
    private SeekBar     m_sbFocal;
    private TextView    m_tvInfo;
    private int minValue = 20; //init min
    private int maxValue = 200; //init max



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_focal);

        m_sbFocal = (SeekBar) findViewById(R.id.sbFocal);
        m_sbFocal.setOnSeekBarChangeListener(this);
        m_tvInfo = (TextView) findViewById(R.id.tvInfo);

        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        setTitle("Choose Focal Lenght");

        Preferences prefs = new Preferences(this);

        minValue = Integer.valueOf(prefs.getMinFocal());
        maxValue = Integer.valueOf(prefs.getMaxFocal());
        m_sbFocal.setMax(maxValue-minValue);
        String sPos = prefs.getLegacyFocal();
        if (sPos != "") {
            m_tvInfo.setText(sPos);
        } else {
            m_tvInfo.setText(Integer.toString(minValue) + "mm");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        String info = m_tvInfo.getText().toString();
        int pos = Integer.parseInt(LLUtils.getFocalFromInfo(info))-minValue;
        m_sbFocal.setProgress(pos);

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        //Save focal lenght
        Preferences prefs = new Preferences(this);
        prefs.setLegacyFocal(String.valueOf(m_sbFocal.getProgress()+minValue));
    }

    @Override
    protected boolean onEnterKeyDown()
    {
        onBackPressed();
        return true;
    }

    @Override
    protected boolean onUpperDialChanged(int value)
    {

        m_sbFocal.incrementProgressBy(value);
        return true;

    }

    @Override
    protected boolean onLowerDialChanged(int value)
    {

        m_sbFocal.incrementProgressBy(value);
        return true;

    }

    /* OnSeekBarChangeListener */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {

        if (progress < 0) {
            m_sbFocal.setProgress(0);
            m_tvInfo.setText(Integer.toString(minValue)+"mm");
        } else    m_tvInfo.setText(Integer.toString(progress+minValue) + "mm");
      /*
        if (fromUser)
        {
            //not needed?
        }
        */
    }
    public void onStartTrackingTouch(SeekBar var1)
    {
    }
    public void onStopTrackingTouch(SeekBar var1)
    {
    }

}
