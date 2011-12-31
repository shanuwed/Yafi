package edu.washington.shan;

import java.util.ArrayList;
import java.util.List;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
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
    private SyncManager mSyncManager;
    private Handler mHandler;
    private ProgressBar mProgressBar;
    private List<String> mTabTags; // collection of tab tags like: usmarkets, mostpopular...

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar1);
        mHandler = new Handler(mCallback);
        mResources = getResources();
        mPrefKeyManager = PrefKeyManager.getInstance();
        mSyncManager = new SyncManager(this, mHandler); // pass the context and the handler
        mPrefKeyManager.initialize(this); // be sure to initialize before using it
        mTabTags = new ArrayList<String>();
        
        prepareDatabase();
        preparePreferences();
        cleanupOldFeeds();
        addTabsBasedOnPreferences();
        syncAtStartup();
        showAboutDialogBoxOnFirstRun();
        clearFirstTimeRunFlag();
        
        // When a tab changes check to see if new RSS feeds are available for
        // the tab. Then sends a broadcast message to refresh the tab.
        getTabHost().setOnTabChangedListener(new TabHost.OnTabChangeListener(){
        	@Override
        	public void onTabChanged(String tabId) {
        		// tabId == tabTag
        		
                // Check to see if new data is available
                if(mSyncManager.isNewDataAvailable(tabId))
                {
                	mSyncManager.clearNewDataAvailableFlag(tabId);
            		
    		    	// Send a message for RSS activity to refresh
                	Intent intent = new Intent(Constants.REFRESH_ACTION);
    		    	sendBroadcast(intent);
                }
        	}});        
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
    	    showAboutDialogBox();
		}
		
		// Returning true ensures that the menu event is not be further processed.
		return true;
	}

	/**
	 * 
	 */
	private void showAboutDialogBox() {
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		View dlgView = inflater.inflate(R.layout.help_dialog_layout, null);

		WebView webview = (WebView) dlgView.findViewById(R.id.help_dialog_layout_webView1);
		webview.loadUrl("file:///android_asset/readme.html");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(mResources.getString(R.string.help_dialog_title));
		builder.setIcon(R.drawable.rss_active); // sets the top left icon
		builder.setView(dlgView);

		builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		}).show();
	}
	
	private void showAboutDialogBoxOnFirstRun()	{
        if(isFirstTimeRunFlagSet()) {
        	showAboutDialogBox();
        }
	}

	/**
	 * Prepare database for the first run after install
	 * Initializes the database by importing a pre-populated database
	 * from the assets directory to the database directory.
	 */
	private void prepareDatabase() {
        if(isFirstTimeRunFlagSet()) {
        	// Copy the initial database from assets dir to database dir
        	DBHelper.importDatabase(this);
        }
	}
	
	private void preparePreferences(){
        if(isFirstTimeRunFlagSet()) {
            String[] prefList = mResources.getStringArray(R.array.subscriptionoptions_keys);
            setPreference(prefList[0], true); // show 'US market' tab
            setPreference(prefList[1], true); // show 'Most Popular' tab
        }
	}
	
	private boolean isFirstTimeRunFlagSet(){
		// Determine if this is the first time running the app
		// Shared preference for 'initialized' is stored in 
		// MainActivity.xml preference file which is different 
		// from the subscription preferences file.
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        if(!sharedPref.getBoolean("initialized", false))
        	return true;
        return false;
	}
	
	private void clearFirstTimeRunFlag(){
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
    	// Now set the "initialized" flag
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putBoolean("initialized", true);
    	editor.commit();
	}
    
	/**
     * Forces to create a preference: <boolean name="usmarkets" value="true" />
     * 
     * If you want to change the default update these two files:
     * subscriptionoptions.xml - the first checkboxpreference is the default.
     * arrays.xml - the first items in subscriptionoptions_keys and
     * subscriptionoptions_keys_title arrays are the defaults.
     */
    private void initializePreferences()
    {
        String[] prefList = mResources.getStringArray(R.array.subscriptionoptions_keys);
        setPreference(prefList[0], true);
    }
    
    /**
     * Given preference key and value it sets the shared preference
     * @param prefKey
     * @param value
     */
    private void setPreference(String prefKey, boolean value)
    {
        SharedPreferences sharedPref = getSharedPreferences(
        		mResources.getString(R.string.pref_filename), 
        		MODE_PRIVATE);
        
        Editor editor = sharedPref.edit();
        editor.putBoolean(prefKey, value);
        editor.commit();
    }
    
    /**
     * Deletes old feeds if preferences are set.
     */
    private void cleanupOldFeeds() 
    {
        SharedPreferences sharedPref = getSharedPreferences(
        		mResources.getString(R.string.pref_filename), 
        		MODE_PRIVATE);
        
        if(sharedPref.getBoolean(
        		mResources.getString(R.string.settings_rss_feed_save_key), false)){
        	DBAdapter dbAdapter = new DBAdapter(this);
        	dbAdapter.open();
        	dbAdapter.deleteItemsOlderThan(Constants.RETENTION_IN_DAYS); // x days
        	dbAdapter.close();
        }
	}
    
    /**
     * It syncs RSS feeds if the preference to 'sync at startup' is selected.
     * 
     */
    private void syncAtStartup()
    {
        SharedPreferences sharedPref = getSharedPreferences(
        		mResources.getString(R.string.pref_filename), 
        		MODE_PRIVATE);
        
        if(sharedPref.getBoolean(
        		mResources.getString(R.string.settings_auto_sync_key), false)){
        	sync();
        }
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
		
		if(requestCode == ACTIVITY_SETTINGS) // returned from settings menu
		{
			cleanupOldFeeds();
		}
		else if(requestCode == ACTIVITY_PREF) // returned from Subscription preference activity
		{
	        // make sure there's at least one tab or it will throw.
	        // Force the first preference to be on all the time.
	        initializePreferences(); 
	        
	        TabHost tabHost = getTabHost();
	        tabHost.clearAllTabs();
	        mTabTags.clear();
	        addTabsBasedOnPreferences();
		}
		else if(requestCode == ACTIVITY_SEARCH)
		{
			// nothing yet
		}
	}

    /**
     * Adds tabs according to the subscription
     * preferences set by the user.
     * Remove all tabs before calling this.
     */
	private void addTabsBasedOnPreferences() 
    {
		SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(this);
		
		// Get the labels for tabs
		String[] tabLabels = mResources.getStringArray(R.array.subscriptionoptions_keys_title);
		
		// Get the keys from the resource to check each preference
		String[] tabTags = mResources.getStringArray(R.array.subscriptionoptions_keys);
		
		for(int index=0; index < tabTags.length; index++)
		{
			String tabTag = tabTags[index];
			
			// Is the preference selected?
			if(prefs.getBoolean(tabTag, false))
			{
				if(addNewTab(tabTag, tabLabels[index]))
				{
					// keep track of the tabs currently open
					mTabTags.add(tabTag);
				}
			}
		}
	}

	/**
	 * Adds a new tab as specified by the parameters.
	 * 
	 * @param tabTag A key string is put onto the Intent and passed to RssActivity. This is
	 *            the same as the tab Tag which is used as an indentifier of a tab
	 * @param label Label for a tab which shows up in UI
	 */
	private boolean addNewTab(String tabTag, String label) 
	{
		boolean ret = false;
		try
		{
			// Create an intent and stuff it with a key.
			// we will need the key to figure out topicId to 
			// query the db in RssActivity.
			Intent intent= new Intent(this, RssActivity.class);
			intent.putExtra(Constants.KEY_TAB_TAG, tabTag); 
			
	        TabHost tabHost = getTabHost();
	        TabHost.TabSpec spec;
	
	        // Initialize a TabSpec for each tab and add it to the TabHost
	        spec = tabHost.newTabSpec(tabTag).setIndicator(label,
	                          mResources.getDrawable(R.drawable.ic_tab_lang))
	                      .setContent(intent);
	        tabHost.addTab(spec);
	        ret = true;
		}
		catch(java.lang.NullPointerException e){}
		return ret;
	}

	/**
	 * Callback function for the background thread to call
	 * when it's done.
	 */
	private Handler.Callback mCallback = new Handler.Callback() 
	{
		@Override
		public boolean handleMessage(Message msg) {
			
			Log.v(TAG, "Handler.Callback entered");
			
			// The background thread returned.
			// Stop the progressbar.
			mProgressBar.setVisibility(ProgressBar.GONE);
			
			Bundle bundle = msg.getData();
			if(bundle != null)
			{
				boolean overallResult = true;
				boolean[] results = bundle.getBooleanArray(Constants.KEY_STATUS);
				//String[] tabTags = bundle.getStringArray(Constants.KEY_TAB_TAG);// only if needed
				for(boolean result : results)
				{
					if(!result)
					{
						overallResult = false;
						break;
					}
				}
				
				if(overallResult)
				{
					Log.v(TAG, "RSS retrieval succeeded");
					
			    	// Send "refresh" message to a tab. Only the active tab will receive.
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
	
	private void sync()
	{
		// Poor man's synchronization.
		// Check for visibility of the progress bar to
		// determine if a thread is already started.
		// If a thread is already in work do not start another one.
		// Visibility is one of VISIBLE, INVISIBLE, GONE.
		if(ProgressBar.VISIBLE != mProgressBar.getVisibility())
		{
			// Set the progress bar visibility
			// Start the progressbar.
			mProgressBar.setVisibility(ProgressBar.VISIBLE);
			String[] tabTags = mTabTags.toArray(new String[]{}); 
			mSyncManager.sync(tabTags);
		}
	}
	
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
		sync();
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
    	//Intent intent = new Intent(this, SearchActivity.class);
    	//startActivityForResult(intent, ACTIVITY_SEARCH);
    }

}