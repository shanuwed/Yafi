package edu.washington.shan;

import com.mobyfactory.uiwidgets.RadioStateDrawable;
import com.mobyfactory.uiwidgets.ScrollableTabActivity;
import com.mobyfactory.uiwidgets.ScrollableTabActivity.SliderBarActivityDelegate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class YafiActivity extends ScrollableTabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
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
}