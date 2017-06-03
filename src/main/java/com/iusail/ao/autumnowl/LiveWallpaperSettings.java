package com.iusail.ao.autumnowl;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

;

public class LiveWallpaperSettings extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static String H_ENABLED_CHECKBOX_KEY;
    public static String SF_ENABLED_CHECKBOX_KEY;

    @Override
    protected void onCreate(Bundle icircle){super.onCreate(icircle);
    getPreferenceManager().setSharedPreferencesName(LiveWallpaperService.PREFERENCES);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }

}
