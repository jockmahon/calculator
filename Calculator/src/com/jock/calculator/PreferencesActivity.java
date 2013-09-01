package com.jock.calculator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class PreferencesActivity extends PreferenceActivity
{

	public static final String KEY_PREF_REVERSE_NUMBERS = "pref_reverse_numberd";


	public void onCreate( Bundle savedState )
	{

		super.onCreate( savedState );

		addPreferencesFromResource( R.xml.preferences );
	}


	@Override
	public boolean onOptionsItemSelected( MenuItem item )
	{
		switch (item.getItemId())
		{
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected( item );
	}
}
