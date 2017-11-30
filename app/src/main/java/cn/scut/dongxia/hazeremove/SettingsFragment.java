package cn.scut.dongxia.hazeremove;

import android.os.Bundle;
import android.preference.PreferenceFragment;



public class SettingsFragment extends PreferenceFragment {

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }

}
