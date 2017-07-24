package tomi.piipposoft.blankspellbook;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by OMISTAJA on 24.7.2017.
 */

public class SettingsActivity extends Activity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //set a custom theme for this activity so we get proper text colours
        setTheme(R.style.SettingsFragmentStyle);
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            //view.setBackgroundColor(ContextCompat.getColor(view.getContext(), android.R.color.background_light));
            return view;
        }
    }

}

