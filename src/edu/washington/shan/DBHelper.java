package edu.washington.shan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * @author shan@uw.edu
 * 
 */
public class DBHelper extends SQLiteOpenHelper {
	
	private static final String TAG="DBHelper";
	
	private static final String CREATE_TABLE="create table " +
		DBConstants.TABLE_NAME + " (" +
		DBConstants.KEY_ID + " integer primary key autoincrement, " +
		DBConstants.TITLE_NAME + " text not null, " +
		DBConstants.URL_NAME + " text not null, " +
		DBConstants.TIME_NAME + " integer, " +
		DBConstants.TOPICID_NAME + " integer, " +
		DBConstants.STATUS_NAME + " integer);";
			
    /**
     * Constructor 
     * 
     * @param ctx the Context within which to work
     * @param dbname the name of the database
     * @param factory may be null
     * @param dbversion the database version
     */
	public DBHelper(Context context, String dbname, CursorFactory factory, int dbversion){
		super(context, dbname, factory, dbversion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.v(TAG, "Creating all the tables");
		try{
			db.execSQL(CREATE_TABLE);
		}catch(SQLiteException e){
			Log.v(TAG, e.getMessage());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading from version " + oldVersion +
				" to " + newVersion +
				", which will destroy all old data.");
		db.execSQL("drop table if exists " + DBConstants.TABLE_NAME);
		onCreate(db);
	}
}
