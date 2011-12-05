package edu.washington.shan;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.Toast;

public class MainActivity extends TabActivity {
	
    private static final String TAG="MainActivity";
	private static final int ACTIVITY_SETTINGS = 0;
	private static final int ACTIVITY_PREF = 1;
	private static final int ACTIVITY_SEARCH = 2;
	private Resources mResources;
    private PrefKeyManager mPrefKeyManager;
    private Handler mHandler;
    private Thread mWorkerThread;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeDatabase();
        
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
        mHandler = new Handler(mCallback);
        mResources = getResources();
        mPrefKeyManager = PrefKeyManager.getInstance();
        mPrefKeyManager.initialize(this); // be sure to initialize before using it
        
        initializePrefs();
        addTabsBasedOnPreferences();
    }

    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		if(item.getItemId() == R.id.mainmenu_settings)
		{
			// Launch to SettingsPrefActivity screen
	    	Intent intent = new Intent(this, SettingsPrefActivity.class);
	    	startActivityForResult(intent, ACTIVITY_SETTINGS);
		}
		else if(item.getItemId() == R.id.mainmenu_help)
		{
			// Show the help dialog
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setTitle(mResources.getString(R.string.help_dialog_title));
			dialog.setMessage(mResources.getString(R.string.help_dialog_msg));
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, 
					mResources.getString(R.string.help_dialog_okay),
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// nothing to do 
					}
				});
			dialog.show();
		}
		return true;
	}

	/**
	 * Initializes the database by importing a pre-populated database
	 * from the assets directory to the database directory.
	 */
	private void initializeDatabase() {
		// Determine if this is the first time running the app
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        if(!sharedPref.getBoolean("initialized", false))
        {
        	// Copy the initial database from assets dir to database dir
        	DBHelper.importDatabase(this);
        	
        	// Now set the "initialized" flag
        	SharedPreferences.Editor editor = sharedPref.edit();
        	editor.putBoolean("initialized", true);
        	editor.commit();
        }
	}
    
	/**
     * Force to create a pref: <boolean name="subscriptionoptions_usmarkets" value="true" />
     */
    private void initializePrefs()
    {
        boolean prefValue = true;
        String[] prefList = mResources.getStringArray(R.array.subscriptionoptions_keys);
        String prefKey = prefList[0];
        
        SharedPreferences sharedPref = getSharedPreferences(
        		mResources.getString(R.string.pref_filename), 
        		MODE_PRIVATE);
        
        Editor editor = sharedPref.edit();
        editor.putBoolean(prefKey, prefValue);
        editor.commit();
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.v(TAG, "onActivityResult called with requestCode:" + requestCode +
				" resultCode:" + resultCode);
		
		if(requestCode == ACTIVITY_SETTINGS)
		{
			// nothing yet
		}
		else if(requestCode == ACTIVITY_PREF)
		{
	        // make sure there's at least one tab or it will throw.
	        // Force the first preference to be on all the time.
	        initializePrefs(); 
	        
	        TabHost tabHost = getTabHost();
	        tabHost.clearAllTabs();
	        addTabsBasedOnPreferences();
		}
		else if(requestCode == ACTIVITY_SEARCH)
		{
			// nothing yet
		}
	}

    /**
     * Add tabs according to the subscription
     * preferences set by the user.
     * Remove all tabs before calling this.
     */
	private void addTabsBasedOnPreferences() 
    {
    	boolean defValue = false;
    	
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(this);
		
		// Get the labels for tabs
		String[] tabLabels = mResources.getStringArray(R.array.subscriptionoptions_keys_title);
		
		// Get the keys from the resource to check each preference
		String[] tabKeys = mResources.getStringArray(R.array.subscriptionoptions_keys);
		
		for(int index=0; index < tabKeys.length; index++)
		{
			String key = tabKeys[index];
			String[] tokens = key.split("_");
			// assert keyParts.length == 2
			
			// Is the preference selected?
			if(prefs.getBoolean(key, defValue))
			{
				addNewTab(key, tokens[1], tabLabels[index]);
			}
		}
	}

	private void addNewTab(String key, String tag, String label) 
	{
		try
		{
			// Create an intent and stuff it with a key.
			// we will need the key to figure out topicId to 
			// query the db in RssActivity.
			Intent intent= new Intent(this, RssActivity.class);
			intent.putExtra(Constants.KEY_PREFKEY, key); 
			
	        TabHost tabHost = getTabHost();
	        TabHost.TabSpec spec;
	
	        // Initialize a TabSpec for each tab and add it to the TabHost
	        spec = tabHost.newTabSpec(tag).setIndicator(label,
	                          mResources.getDrawable(R.drawable.ic_tab_lang))
	                      .setContent(intent);
	        tabHost.addTab(spec);
		}
		catch(java.lang.NullPointerException e){}
	}

	/**
	 * This is the callback for the background to thread to call
	 * when it's done.
	 */
	private Handler.Callback mCallback = new Handler.Callback() 
	{
		@Override
		public boolean handleMessage(Message msg) {
			
			// The background thread returned.
			// Stop the progressbar.
			mProgressBar.setVisibility(ProgressBar.GONE);
			
			Bundle bundle = msg.getData();
			if(bundle != null)
			{
				boolean result = bundle.getBoolean(Constants.KEY_STATUS);
				if(result)
				{
					Log.v(TAG, "RSS retrieval succeeded");
					
			    	// Send "refresh" message to the tab
			    	Intent intent = new Intent(Constants.REFRESH_ACTION);
			    	sendBroadcast(intent);
				}
				else
				{
					Log.v(TAG, "RSS retrieval failed");
					Toast.makeText(getApplicationContext(), 
							mResources.getString(R.string.rss_retrieval_failed), 
							Toast.LENGTH_SHORT).show();
				}
			}
			return false;
		}
	};
	
	/*
     * Below is to handle the actionbar events
     */
    
    /**
     * Refreshes RSS feed
     * @param v
     */
    public void onRefresh(View v)
    {
		Log.v(TAG, "onRefresh");
		
		// Set the progress bar visibility
		// Start the progressbar.
		mProgressBar.setVisibility(ProgressBar.VISIBLE);
		
    	// Figure out the active tab
        TabHost tabHost = getTabHost();  // The activity TabHost
        String currentTabTag = tabHost.getCurrentTabTag(); // like "usmarkets"
        
        String key = "subscriptionoptions_" + currentTabTag;
        String rssUrl = Constants.RSS_BASE_URI + currentTabTag;
        int topicId = PrefKeyManager.getInstance().keyToValue(key);
        
        // Start a background thread to sync
        // The background thead shall get the latest RSS feed
        // then compare to the RSS items in the db.
        // If there are new items, add them to the db (only the new ones)
        // Then signal back to the calling thread that
        // there are new items available. The calling thread shall
        // refresh the list.
        mWorkerThread = new Thread(new WorkerThreadRunnable(this, mHandler, rssUrl, topicId));
        mWorkerThread.start();
        
    }
    
    /**
     * Starts the subscription preference activity
     * @param v
     */
    public void onAdd(View v)
    {
		Log.v(TAG, "onAdd");
		// If a tab gets removed while it's in focus you get an exception.
		// A workaround is to bring the first tab to the front before switching
		// to another activity. The first tab will always be there.
        getTabHost().setCurrentTab(0);
    	Intent intent = new Intent(this, SubscriptionPrefActivity.class);
    	startActivityForResult(intent, ACTIVITY_PREF);
    }
    
	/**
	 * Starts the search activity
	 * @param v
	 */
    public void onSearch(View v)
    {
		Log.v(TAG, "onSearch");
    	Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
    }

}