/*
 * Copyright 2015 Phil Shadlyn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.superutilities.UnitConverterUltimate.fragments;

import android.content.ActivityNotFoundException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.superutilities.R;
import com.superutilities.UnitConverterUltimate.Preferences;
import com.superutilities.UnitConverterUltimate.util.IntentFactory;


/**
 * Fragment to display preferences screen
 * Created by Phizz on 15-08-02.
 */
public class PreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String GITHUB_ISSUE = "null";

    public static PreferencesFragment newInstance()
    {
        return new PreferencesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*// Add listeners to preferences
        Preference unitRequest = findPreference("unit_request");
        unitRequest.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                requestUnit();
                return true;
            }
        });

        Preference rateApp = findPreference("rate_app");
        rateApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                rateApp();
                return true;
            }
        });

        Preference openIssue = findPreference("open_issue");
        openIssue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                openIssue();
                return true;
            }
        });

        Preference viewSource = findPreference("view_source");
        viewSource.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                viewSource();
                return true;
            }
        });*/
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Preferences.getInstance(getActivity()).getPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Preferences.getInstance(getActivity()).getPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void requestUnit()
    {
        try
        {
            startActivity(IntentFactory.getRequestUnitIntent(getString(R.string.request_unit)));
        }
        catch(ActivityNotFoundException ex)
        {
            Toast.makeText(getActivity(), R.string.toast_error_no_email_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void rateApp()
    {
        try
        {
            startActivity(IntentFactory.getOpenPlayStoreIntent(getActivity().getPackageName()));
        }
        catch(ActivityNotFoundException ex)
        {
            Toast.makeText(getActivity(), R.string.toast_error_google_play, Toast.LENGTH_SHORT).show();
        }
    }

    private void viewSource()
    {
        try
        {
            startActivity(IntentFactory.getOpenUrlIntent(IntentFactory.GITHUB_REPO));
        }
        catch(ActivityNotFoundException ex)
        {
            Toast.makeText(getActivity(), R.string.toast_error_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private void openIssue()
    {
        try
        {
            startActivity(IntentFactory.getOpenUrlIntent(GITHUB_ISSUE));
        }
        catch(ActivityNotFoundException ex)
        {
            Toast.makeText(getActivity(), R.string.toast_error_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(Preferences.PREFS_THEME))
        {
            // Theme change, restart all open activities and reload with new theme
            getActivity().finish();
        }
    }
}
