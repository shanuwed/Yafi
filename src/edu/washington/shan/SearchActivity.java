/**
 * 
 */
package edu.washington.shan;

import edu.washington.shan.RssActivity.RefreshBroadcastReceiver;
import android.app.ListActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;

/**
 * @author shan@uw.edu
 * Search functionality is not fully implemented
 */
public class SearchActivity extends ListActivity {

	private static final String TAG="SearchActivity";
    //private DBAdapter mDbAdapter;
    //private Cursor mCursor;
    
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
        // Immediately go to the database and query the items to show
        //mDbAdapter = new DBAdapter(this);
        //mDbAdapter.open();
    }
	
	/**
	 * Starts the search
	 * @param v
	 */
    public void onSearch(View v)
    {
		Log.v(TAG, "onSearch");
    }
}
