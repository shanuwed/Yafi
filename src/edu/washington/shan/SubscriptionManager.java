package edu.washington.shan;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

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
    
    /**
     * Check if the host can be resolved and connected.
     * @return
     */
    public boolean checkConnection()
    {
    	boolean result = false;
    	final String host = "finance.yahoo.com"; // "finance.yahoo.com:80"
    	
    	// Creates a socket and connects it to 
    	// the specified port number on the named host.
    	try 
    	{
			Socket socket = new Socket(host, 80);
			if(socket.isConnected())
			{
				result = true;
				socket.close();
			}
		} 
    	catch (UnknownHostException e) 
    	{
			Log.e(TAG, e.toString());
		} 
    	catch (IOException e) 
    	{
			Log.e(TAG, e.toString());
		}
    	return result;
    }
    
    public boolean getRssFeed(String url, int topicId)
    {
    	assert topicId >=0;
    	
        boolean ret = false;
        mDbAdapter.open();
        try 
        {
			ret = getRssFeedPrivate(url, topicId);
		} 
        catch (UnknownHostException e) 
        {
			Log.e("Unable to read RSS feed due to UnknownHostException", e.getMessage());
		}
        mDbAdapter.close();
        return ret;
    }
    
    private boolean getRssFeedPrivate(String url, int topicId) throws java.net.UnknownHostException
    {
		Log.v(TAG, "Requesting RSS feed for:" + url + " and topicId:" + topicId);
		
    	boolean ret = false;
        try 
        {
	        RSSReader reader = new RSSReader();
			RSSFeed feed = reader.load(url); // may throw RSSReaderException or UnknownHostException
			List<RSSItem> rssItems = feed.getItems();
			for(RSSItem rssItem: rssItems)
			{
				String title = rssItem.getTitle();
				String sqlTitle = title.replace("'", "''");
    			java.util.Date timestamp = rssItem.getPubDate();
    			Long timeInMillisec = timestamp.getTime();
    			
    			// Does the item already exist?
    			// TODO: query can be further constrained by topicId
    			Cursor cursor = mDbAdapter.fetchItemsByTitle(sqlTitle);
    			if(cursor.getCount() == 0)
    			{
	    			mDbAdapter.createItem(title, 
						rssItem.getLink().toString(), 
						timeInMillisec, 
						topicId, 
						0);// status (TODO not in use)
    			}
			}
			ret = true;
		} 
        catch (RSSReaderException e) 
        {
			Log.e("Failed to read RSS feed:", e.toString());
		}
        catch (java.lang.NullPointerException e)
        {
			Log.e("Failed to read RSS feed:", e.toString());
		}
		return ret;
    }
}
