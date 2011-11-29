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

/**
 * @author shan@uw.edu
 *
 */
public class SubscriptionManager {
	
	private static final String TAG="SubscriptionManager";
    private DBAdapter mDbAdapter;
	
    public SubscriptionManager(Context context) 
    {
    	mDbAdapter = new DBAdapter(context);
    }
    
    public boolean getRssFeed(String url, int topicId)
    {
    	assert topicId >=0;
    	
        boolean ret = false;
        mDbAdapter.open();
        ret = getRssFeedPrivate(url, topicId);
        mDbAdapter.close();
        return ret;
    }
    
    private boolean getRssFeedPrivate(String url, int topicId)
    {
		Log.e(TAG, "Requesting RSS feed for:" + url + " and topicId:" + topicId);
		
    	boolean ret = false;
        try {
	        RSSReader reader = new RSSReader();
			RSSFeed feed = reader.load(url); // may throw RSSReaderException
			List<RSSItem> rssItems = feed.getItems();
			for(RSSItem rssItem: rssItems){
    			java.util.Date timestamp = rssItem.getPubDate();
    			Long timeInMillisec = timestamp.getTime();
    			String time = Long.toString(timeInMillisec);
    			mDbAdapter.createItem(rssItem.getTitle(), 
					rssItem.getLink().toString(), 
					time, 
					topicId, 
					0);// status (TODO not in use)
			}
			ret = true;
		} catch (RSSReaderException e) {
			Log.e("Failed to read RSS feed:", e.toString());
		} catch (java.lang.NullPointerException e){
			Log.e("Failed to read RSS feed:", e.toString());
		}
		return ret;
    }
}
