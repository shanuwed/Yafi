package edu.washington.shan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Custom adapter for the list view inside a tab.
 * This adapter is used to format date and URL.
 * @author shan@uw.edu
 *
 */
public class CustomCursorAdapter extends SimpleCursorAdapter {
	
	//private int thisYear = Calendar.getInstance().get(Calendar.YEAR);
	//private SimpleDateFormat longDateFormat = new SimpleDateFormat("dd MMM yyyy");
	private SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd MMM");
	
	public CustomCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
	}
	
	/**
     * Called by bindView() to set the text for a TextView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an TextView.
     *
     * Intended to be overridden by Adapters that need to filter strings
     * retrieved from the database.
     * 
     * @param v TextView to receive text
     * @param text the text to be set for the TextView
     */    
    public void setViewText(TextView v, String text) {
    	int id = v.getId();
    	if(id == R.id.rss_row_text_content){
      		v.setText(text);
    	}
    	else if (id == R.id.rss_row_text_date){
    		String dateStr = "";
    		try{
    			long timeInMillisec = Long.parseLong(text);
    			if(timeInMillisec > 0){
    				
    				// Sometimes server returns a negative number so
    				// make sure it's valid.
    				Calendar calendar= Calendar.getInstance();
    				calendar.setTimeInMillis(timeInMillisec);
    				
    				// display the date like this: 15 Dec
    				java.util.Date date = new java.util.Date(timeInMillisec);
    				dateStr = shortDateFormat.format(date);
    			}
    		}
    		catch(NumberFormatException e){
    			// Long.parseLong may throw an exception.
    			// Just swallow it.
    		}
			v.setText(dateStr);
    	}
    	else if(id == R.id.rss_row_text_title){
			v.setText(text);
    	}
    }
}
