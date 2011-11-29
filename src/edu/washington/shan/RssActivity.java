/**
 * 
 */
package edu.washington.shan;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

/**
 * @author shan@uw.edu
 *
 */
public class RssActivity extends ListActivity {
	
	private static final String TAG="RssActivity";
    private DBAdapter mDbAdapter;
    private Cursor mCursor;
    private String mKey; // preference key as defined in subscriptionoptions.xml
    private String mRssUrl;
	private RefreshBroadcastReceiver refreshBroadcastReceiver = new RefreshBroadcastReceiver();
    
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_list);
		
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        {
            mKey = extras.getString(Constants.KEY_PREFKEY);
        	mRssUrl = extras.getString(Constants.KEY_URL);
        }
        
        // Immediately go to the database and query the items to show
        mDbAdapter = new DBAdapter(this);
        mDbAdapter.open();
        fillData();
        //mDbAdapter.close();// don't close
        
    }
	
    @Override
    public void onPause()
    {
    	unregisterReceiver(refreshBroadcastReceiver);
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
        registerReceiver(refreshBroadcastReceiver, 
        		new IntentFilter(Constants.REFRESH_ACTION));
        super.onResume();
    }
    
    private void fillData() 
    {
    	// For the given key get the int value and use that
    	// to retrieve RSS entries from the db.
    	
    	int topicId = PrefKeyManager.getInstance().keyToValue(mKey); 
    	Log.v(TAG, "fillData called for key: " + mKey + " topicId: " + topicId);
    	
        // Get the rows from the database and create the item list
        mCursor = mDbAdapter.fetchItemsByTopicId(topicId);
        startManagingCursor(mCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{DBConstants.TITLE_NAME};

        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = 
            new SimpleCursorAdapter(this, R.layout.rss_row, mCursor, from, to);
        setListAdapter(items);
    }
    
    /*
     * 
     */
    protected class RefreshBroadcastReceiver extends BroadcastReceiver 
    {
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equals(Constants.REFRESH_ACTION))
			{
		    	Log.v(TAG, "RefreshBroadcastReceiver for key: " + mKey);
		    	fillData();
			}
		}
    }
    
}