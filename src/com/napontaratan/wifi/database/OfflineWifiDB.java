package com.napontaratan.wifi.database;

import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.model.WifiMarker;

/**
 * Offline database to store new WifiConnections waiting to be pushed to the remote server
 * @author Napon Taratan
 */
public class OfflineWifiDB extends SQLiteOpenHelper {
	/*
	 * TODO Implement methods at the bottom.
	 * 
	 */
	
	
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
	public void addToDB(WifiConnection connection) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			values.put(KEY_DATA, WifiMarker.serialize(connection));
		} catch (IOException e) {
			System.out.println("IOException caught in addToDB()");
			e.printStackTrace();
		}

		db.insert(TABLE_NAME, null, values);
		db.close();

	}
	
	// I need methods like the following. - Kurt
	/**
	 * Temporary method.
	 * @return True if no entry exists.
	 * @author Kurt Ahn
	 */
	public boolean isEmpty() {
		return false;
	}
	
	/**
	 * Temporary method.
	 * @return Either newest or oldest entry if there's any.
	 * @author Kurt Ahn
	 */
	public WifiConnection next() {
		return null;
	}
	
	/**
	 * Temporary method.
	 * Removes a WifiConnection object.
	 * @param connection - Connection data to remove.
	 * @author Kurt Ahn
	 */
	public void remove(WifiConnection connection) {
		
	}
}
