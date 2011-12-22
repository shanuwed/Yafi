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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * @author shan@uw.edu
 *
 */
public class RssActivity extends ListActivity {
	
	private static final String TAG="RssActivity";
    private DBAdapter mDbAdapter;
    private Cursor mCursor;
    private String mKey = null; // preference key as defined in subscriptionoptions.xml. It may look like subscriptionoptions_mostpopular.
	private RefreshBroadcastReceiver refreshBroadcastReceiver = new RefreshBroadcastReceiver();
    
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rss_list);
		
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        {
            mKey = extras.getString(Constants.KEY_PREFKEY);
        }
        
        // Immediately go to the database and query the items to show
        mDbAdapter = new DBAdapter(this);
        mDbAdapter.open();
        fillData();
        
        registerReceiver(refreshBroadcastReceiver, 
        		new IntentFilter(Constants.REFRESH_ACTION));
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
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		// Using the id get the URL from the db
		Cursor cursor = mDbAdapter.fetchItemsByRowId(id);
		startManagingCursor(cursor);
		if(cursor != null)
		{
			int colIndex = cursor.getColumnIndex(DBConstants.URL_NAME);
			String uri = cursor.getString(colIndex);
			if(uri != null && uri.length() > 0)
			{
				// Intent to open a browser
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(uri));
				startActivity(i);
			}
		}
	}

    private void fillData() 
    {
    	// For the given key get the int value and use that
    	// to retrieve RSS entries from the db.
    	if(mKey != null && mKey.length() > 0)
    	{
	    	int topicId = PrefKeyManager.getInstance().keyToValue(mKey); 
	    	Log.v(TAG, "fillData called for key: " + mKey + " topicId: " + topicId);
	    	
	        // Get the rows from the database and create the item list
	        mCursor = mDbAdapter.fetchItemsByTopicId(topicId);
	        startManagingCursor(mCursor);
	
	        // Create an array to specify the fields we want to display in the list (only TITLE)
	        String[] from = new String[]{DBConstants.TITLE_NAME, 
	        		DBConstants.URL_NAME,
	        		DBConstants.TIME_NAME};
	
	        // and an array of the fields we want to bind those fields to
	        int[] to = new int[]{R.id.rss_row_text_content, 
	        		R.id.rss_row_text_title,
	        		R.id.rss_row_text_date};
	
	        // Now create a simple cursor adapter and set it to display
	        CustomCursorAdapter items = 
	            new CustomCursorAdapter(this, R.layout.rss_row, mCursor, from, to);
	        setListAdapter(items);
    	}
    }
    
    /**
     * The message is originated from the Main activity to 
     * notify that the RSS data retrieval is completed.
     * As soon as we get the message we update the list.
     * TODO: when you click on the refresh button and switch 
     * the tab the original tab won't be receiving this message.
     */
    protected class RefreshBroadcastReceiver extends BroadcastReceiver 
    {
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equals(Constants.REFRESH_ACTION))
			{
				// Retrieve the topic Id and see if it's valid
				int topicId = intent.getIntExtra(Constants.KEY_TOPICID, -1);
				if(-1 != topicId)
				{
					// Get the topic and see if it's valid
					String topic = PrefKeyManager.getInstance().ValueToKey(topicId);
					if(topic.length() > 0)
					{
						// Is this broadcast message intended for this activity?
						if(0 == topic.compareTo(mKey))
						{
					    	Log.v(TAG, "RefreshBroadcastReceiver for key: " + mKey);
					    	fillData();
						}
						else
						{
							// message is intended for other Rss activity...
							// TODO: save it for later retrieval
						}
					}
				}
			}
		}
    }
    
}