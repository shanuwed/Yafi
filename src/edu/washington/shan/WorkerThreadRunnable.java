/**
 * 
 */
package edu.washington.shan;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author shan@uw.edu
 *
 */
public class WorkerThreadRunnable implements Runnable {
	
	private static final String TAG = "WorkerThreadRunnable";
	private Handler mMainThreadHandler;
	private Context mContext; 
	private String mRssUrl;
	private int mTopicId;
	
	public WorkerThreadRunnable(Context context, Handler handler, String rssUrl, int topicId)
	{
		mContext = context;
		mMainThreadHandler = handler;
		mRssUrl = rssUrl;
		mTopicId = topicId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Log.v(TAG, "Requesting RSS feed for:" + mRssUrl);
		boolean rssResult = false; 

    	SubscriptionManager subscription = new SubscriptionManager(mContext);
    	if(subscription.checkConnection())
    	{
    		rssResult = subscription.getRssFeed(mRssUrl, mTopicId);
    	}
    	informFinish(rssResult);
	}
	
	public void informFinish(boolean result)
	{
		Log.v(TAG, "Finishing RSS feed for:" + mRssUrl);
		
		Bundle bundle = new Bundle();
		bundle.putBoolean(Constants.KEY_STATUS, result); // key, value
		
		Message msg = mMainThreadHandler.obtainMessage();
		msg.setData(bundle);
		mMainThreadHandler.sendMessage(msg);
	}

}
