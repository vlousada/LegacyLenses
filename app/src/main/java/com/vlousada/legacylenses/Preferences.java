package com.vlousada.legacylenses;

/*
 * Adapted sourcecode from com.github.obs1dium.bettermanual
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.sony.scalar.hardware.CameraEx;

public class Preferences
{
    private final SharedPreferences m_prefs;

    private static final String KEY_SCENE_MODE = "sceneMode";
    private static final String KEY_DRIVE_MODE = "driveMode";
    private static final String KEY_BURST_DRIVE_SPEED = "burstDriveSpeed";
    private static final String KEY_MIN_SHUTTER_SPEED = "minShutterSpeed";
    private static final String KEY_VIEW_FLAGS = "viewFlags";

    private static final String KEY_MY_LENS_NAME = "lens XPTO";
    private static final String KEY_MY_LENS_FOCAL = "50";
    private static final String KEY_MY_LENS_APERTURE = "80";
    private static final String KEY_MY_APERTURES = "14;20;28;40;56;80;160;220";
    private static final String KEY_MIN_FOCAL = "7";
    private static final String KEY_MAX_FOCAL = "1200";

    private static final String KEY_SETTING_SORT = "YES";
    private static final String KEY_SETTING_LR_KEYS = "NO";
    private static final String KEY_SETTING_COC = "0.30";




    public Preferences(Context context)
    {
        m_prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean clearPrefs()
    {
        return m_prefs.edit().clear().commit();
    }


    public String getSceneMode()
    {
        return m_prefs.getString(KEY_SCENE_MODE, CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE);
    }

    public void setSceneMode(String mode)
    {
        m_prefs.edit().putString(KEY_SCENE_MODE, mode).apply();
    }

    public String getDriveMode()
    {
        return m_prefs.getString(KEY_DRIVE_MODE, CameraEx.ParametersModifier.DRIVE_MODE_BURST);
    }

    public void setDriveMode(String mode)
    {
        m_prefs.edit().putString(KEY_DRIVE_MODE, mode).apply();
    }

    public String getBurstDriveSpeed()
    {
        return m_prefs.getString(KEY_BURST_DRIVE_SPEED, CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH);
    }

    public void setBurstDriveSpeed(String speed)
    {
        m_prefs.edit().putString(KEY_BURST_DRIVE_SPEED, speed).apply();
    }

    public int getMinShutterSpeed()
    {
        return m_prefs.getInt(KEY_MIN_SHUTTER_SPEED, -1);
    }

    public void setMinShutterSpeed(int speed)
    {
        m_prefs.edit().putInt(KEY_MIN_SHUTTER_SPEED, speed).apply();
    }

    public int getViewFlags(int defaultValue)
    {
        return m_prefs.getInt(KEY_VIEW_FLAGS, defaultValue);
    }
    public void setViewFlags(int flags)
    {
        m_prefs.edit().putInt(KEY_VIEW_FLAGS, flags).apply();
    }




    //************** LEGACY Shared Preferences ********************

    public String getLegacyLensName ()
    {
        return m_prefs.getString(KEY_MY_LENS_NAME, "Lens XPTO");
    }
    public void setLegacyLensName(String legacyName)
    {
        m_prefs.edit().putString(KEY_MY_LENS_NAME, legacyName).apply();
    }

    public String getLegacyFocal ()
    {
        return m_prefs.getString(KEY_MY_LENS_FOCAL, "50");
    }
    public void setLegacyFocal(String legacyFocal)
    {
        m_prefs.edit().putString(KEY_MY_LENS_FOCAL, legacyFocal).apply();
    }

    public String getLegacyAperture ()
    {
        return m_prefs.getString(KEY_MY_LENS_APERTURE, "80");
    }//F8
    public void setLegacyAperture(String legacyAperture)
    {
        m_prefs.edit().putString(KEY_MY_LENS_APERTURE, legacyAperture).apply();
    }


    public String getMyApertures ()
    {
        return m_prefs.getString(KEY_MY_APERTURES, "14;20;28;40;56;80;160;220");
    }
    public void setMyApertures(String myApertures)
    {
        m_prefs.edit().putString(KEY_MY_APERTURES, myApertures).apply();

    }

    public String getMinFocal ()
    {
        return m_prefs.getString(KEY_MIN_FOCAL, "50");
    }
    public void setMinFocal(String myFocals)
    {
        m_prefs.edit().putString(KEY_MIN_FOCAL, myFocals).apply();
    }

    public String getMaxFocal ()
    {
        return m_prefs.getString(KEY_MAX_FOCAL, "50");
    }
    public void setMaxFocal(String myFocals)
    {
        m_prefs.edit().putString(KEY_MAX_FOCAL, myFocals).apply();
    }


    //************** User SETTINGS Shared Preferences ********************
    public String getSettingLensesSorted ()
    {
        return m_prefs.getString(KEY_SETTING_SORT, "YES");
    }
    public void setSettingLensesSorted(String doSortYesNo)
    {
        m_prefs.edit().putString(KEY_SETTING_SORT, doSortYesNo).apply();
    }

    public String getSettingUseLeftRightKeys ()
    {
        return m_prefs.getString(KEY_SETTING_LR_KEYS, "NO");
    }
    public void setSettingUseLeftRightKeys(String useLRKeysYesNo)
    {
        m_prefs.edit().putString(KEY_SETTING_LR_KEYS, useLRKeysYesNo).apply();
    }

    public String getSettingCoC ()
    {
        return m_prefs.getString(KEY_SETTING_COC, "0.30");
    }
    public void setSettingCoc(String COConFullFrame)
    {
        m_prefs.edit().putString(KEY_SETTING_COC, COConFullFrame).apply();
    }


}
