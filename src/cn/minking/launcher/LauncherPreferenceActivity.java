package cn.minking.launcher;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class LauncherPreferenceActivity extends PreferenceActivity
    implements OnPreferenceChangeListener{
    public static String LAST_DATABASE_NAME = "pref_key_last_database_name";

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
