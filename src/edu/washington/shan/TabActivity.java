package edu.washington.shan;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;


public class TabActivity extends ListActivity{
	
	private static final String TAG="TabActivity";
    private DBAdapter mDbAdapter;
    private Cursor mCursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_list);
        mDbAdapter = new DBAdapter(this);
        mDbAdapter.open();
        fillData();
        
        /*
        setListAdapter(new SimpleAdapter(this, getData(""),
                android.R.layout.simple_list_item_2, 
                new String[] { "title" }, // column name
                new int[] { android.R.id.text1 }));
        getListView().setTextFilterEnabled(true);
        */
    }
	
    private void fillData() {
        // Get all of the rows from the database and create the item list
        mCursor = mDbAdapter.fetchAllItems();
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
}
