package com.sielski.marcin.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class PopularMoviesSettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_popular_movies);
        Preference preference = findPreference(getString(R.string.key_themoviedb_api_key));
        preference.setSummary(PopularMoviesUtils.getTheMoviesDBApiKey(getContext()));
        preference.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        Toast error = Toast.makeText(getContext(),
                getString(R.string.toast_settings_themoviedb_api_key), Toast.LENGTH_SHORT);
        String value = (String)o;
        if (value.length() != getResources().getInteger(R.integer.length_themoviedb_api_key)) {
            error.show();
            return false;
        }
        value = value.toLowerCase();
        if (!value.matches("[a-z0-9]+")) {
            error.show();
            return false;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        findPreference(s).setSummary(sharedPreferences.getString(s,""));
    }
}
