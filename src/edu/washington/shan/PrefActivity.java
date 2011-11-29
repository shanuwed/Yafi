/**
 * 
 */
package edu.washington.shan;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author shan@uw.edu
 *
 */
public class PrefActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.subscriptionoptions);
	}
}
