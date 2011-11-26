package edu.washington.shan;

import com.mobyfactory.uiwidgets.RadioStateDrawable;
import com.mobyfactory.uiwidgets.ScrollableTabActivity;
import com.mobyfactory.uiwidgets.ScrollableTabActivity.SliderBarActivityDelegate;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class YafiActivity extends ScrollableTabActivity {
	
    AddNewTabReceiver addNewTabReceiver = new AddNewTabReceiver();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*
         * set this activity as the tab bar delegate
         * so that onTabChanged is called when users tap on the bar
         */
        setDelegate(new SliderBarActivityDelegateImpl());
        
        for (int i=0; i<2; i++)
        {
        	Intent intent= new Intent(this, DemoActivity1.class);
        	
        	/*
        	 * This adds a title and an image to the tab bar button
        	 * Image should be a PNG file with transparent background.
        	 * Shades are opaque areas in on and off state are specific as parameters
        	 */
        	this.addTab("title"+i, 
        		R.drawable.star, 
        		RadioStateDrawable.SHADE_GRAY, 
        		RadioStateDrawable.SHADE_GREEN,
        		intent);
        }
        
        /*
         * commit is required to redraw the bar after add tabs are added
         * if you know of a better way, drop me your suggestion please.
         */
        commit();
        
        // register
        registerReceiver(addNewTabReceiver, 
        		new IntentFilter(Constants.ADDNEWTAB_ACTION));
    }
    
    @Override
    public void onPause()
    {
    	unregisterReceiver(addNewTabReceiver);
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
        // register
        registerReceiver(addNewTabReceiver, 
        		new IntentFilter(Constants.ADDNEWTAB_ACTION));
        super.onResume();
    }

    private class SliderBarActivityDelegateImpl extends SliderBarActivityDelegate
    {
    	/*
    	 * Optional callback method
    	 * called when users tap on the tab bar button
    	 */
    	protected void onTabChanged(int tabIndex) 
    	{
    		Log.d("onTabChanged",""+tabIndex);
    	}
    }
    
    protected class AddNewTabReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(Constants.ADDNEWTAB_ACTION)){
				// Add a new tab
				Intent actionIntent = new Intent(context, DemoActivity1.class);
				actionIntent.putExtra(Constants.KEY_URL, "http://www.yahoo.com" );
	        	((YafiActivity)context).addTab("new tab", 
	            		R.drawable.star, 
	            		RadioStateDrawable.SHADE_GRAY, 
	            		RadioStateDrawable.SHADE_GREEN,
	            		actionIntent);
			}
			((YafiActivity)context).commit();
		}
    }
}