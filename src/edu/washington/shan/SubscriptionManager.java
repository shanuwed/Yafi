package edu.washington.shan;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;


public class SubscriptionManager {
	
	private static final String TAG="TabActivity";
	private static final String baseUri = "http://finance.yahoo.com/rss/";
    private DBAdapter mDbAdapter;
	
    public SubscriptionManager(Context context) {
    	mDbAdapter = new DBAdapter(context);
    }
    
    public boolean getRssFeed(String uri){
        boolean ret = false;
        mDbAdapter.open();
        ret = getRssFeedPrivate(uri);
        mDbAdapter.close();
        return ret;
    }
    
    private boolean getRssFeedPrivate(String uri){
    	boolean ret = false;
        try {
	        RSSReader reader = new RSSReader();
			RSSFeed feed = reader.load(uri); // may throw RSSReaderException
			List<RSSItem> rssItems = feed.getItems();
			for(RSSItem rssItem: rssItems){
    			java.util.Date timestamp = rssItem.getPubDate();
    			Long timeInMillisec = timestamp.getTime();
    			String time = Long.toString(timeInMillisec);
    			mDbAdapter.createItem(rssItem.getTitle(), 
					rssItem.getLink().toString(), 
					time, 
					0, // topicId (TODO fix it) 
					0);// status (TODO not in use)
			}
			ret = true;
		} catch (RSSReaderException e) {
			Log.e("Failed to read RSS feed:", e.toString());
		}
		return ret;
    }
}
