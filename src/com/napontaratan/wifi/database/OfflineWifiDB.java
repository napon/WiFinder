package com.napontaratan.wifi.database;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.model.WifiMarker;

/**
 * Offline database to store new WifiConnections waiting to be pushed to the remote server
 * @author Napon Taratan
 */
public class OfflineWifiDB extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "OfflineWifiDB";

	private static final String TABLE_NAME = "buffer";
	private static final String KEY_ID = "id";
	private static final String KEY_DATA = "WifiConnection";

	public OfflineWifiDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_BUFFER_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_DATA + " BLOB )";
		db.execSQL(CREATE_BUFFER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS buffer");
		this.onCreate(db);
	}
	
	/**
	 * Add a WifiMarker object to the DB
	 * @author Napon Taratan
	 */
	public void add(WifiConnection connection) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		try {
			values.put(KEY_DATA, WifiConnection.serialize(connection));
		} catch (IOException e) {
			System.out.println("IOException caught in addToDB()");
			e.printStackTrace();
		}

		db.insert(TABLE_NAME, null, values);
		db.close();

	}
	
	/**
	 * TODO : Delete an item from database
	 * @param connection
	 * @author Napon Taratan
	 */
	public void remove(WifiConnection connection) {
		//TODO
	}
	
	/**
	 * 
	 * @return Next entry.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public WifiConnection next() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		WifiConnection obj = null;
		if (c.moveToNext()) {
			try {
				obj = (WifiConnection) WifiConnection.deserialize(c.getBlob(1));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		c.close();
		return obj;
	}
	
	/**
	 * Return true if OfflineWifiDB is empty
	 * @author Napon Taratan
	 */
	public boolean isEmpty() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		cursor.close();
		return cursor.getCount() == 0;
	}
}
