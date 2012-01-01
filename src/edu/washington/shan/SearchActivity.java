/**
 * 
 */
package edu.washington.shan;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

/**
 * @author shan@uw.edu
 */
public class SearchActivity extends ListActivity {

	private static final String TAG="SearchActivity";
    private DBAdapter mDbAdapter;
    private Cursor mCursor;
    private CustomViewBinder customViewBinder = new CustomViewBinder();
    private EditText mEditText;
    private String mSearchTerm;
    private final static String key = "searchTerm";
    
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		
		// Wire edittext
		mEditText = (EditText)findViewById(R.id.search_edittext);
		
		if(savedInstanceState != null){
			mSearchTerm = (String) savedInstanceState.getSerializable(key);
		}else{
			Bundle extras = getIntent().getExtras();
			if(extras != null){
				mSearchTerm = extras.getString(key);
			}
		}
        search();
    }
	
	// Note that saveState() must be called in both onSaveInstanceState and onPause
	// to ensure that the data is saved. This is because there's no guarantee that
	// onSaveInstanceState will be called and because when it is called,
	// it is called before onPause.
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		saveState();
		outState.putSerializable(key, mSearchTerm);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		restoreState();
	}

	private void saveState() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = sharedPref.edit();
    	editor.putString(key, mSearchTerm);
    	editor.commit();
	}

	private void restoreState() {
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        mSearchTerm = sharedPref.getString(key, "");
        mEditText.setText(mSearchTerm);
	}

	private void search() 
    {
    	Log.v(TAG, "Entering search()...");
    	
        mDbAdapter = new DBAdapter(this);
        mDbAdapter.open();

    	mSearchTerm = mEditText.getText().toString();
    	if(mSearchTerm != null && mSearchTerm.length() > 0)
    	{
	    	Log.v(TAG, "search called for: " + mSearchTerm);
	    	
	        // Get the rows from the database and create the item list
	        mCursor = mDbAdapter.fetchItemsByMatchingTitle(mSearchTerm);
	        startManagingCursor(mCursor);
	
	        // Create an array to specify the fields we want to display in the list (only TITLE)
	        String[] from = new String[]{DBConstants.TITLE_NAME, 
	        		DBConstants.URL_NAME,
	        		DBConstants.TIME_NAME,
	        		DBConstants.STATUS_NAME};
	
	        // and an array of the fields we want to bind those fields to
	        int[] to = new int[]{R.id.rss_row_text_content, 
	        		R.id.rss_row_text_title,
	        		R.id.rss_row_text_date,
	        		R.id.rss_row_thumbImage};
	
	        // Now create a simple cursor adapter and set it to display
	        SimpleCursorAdapter adapter = 
	        	new SimpleCursorAdapter(this, R.layout.rss_row, mCursor, from, to);
	        adapter.setViewBinder(customViewBinder);
	        setListAdapter(adapter);
	        
	        stopManagingCursor(mCursor); // TODO is this good enough?
    	}
    	mDbAdapter.close();
    }
	
	/**
	 * Handle the search button
	 * @param v
	 */
    public void onSearch(View v)
    {
		Log.v(TAG, "onSearch");
		search();
    }
}
