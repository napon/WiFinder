package com.napontaratan.wifi.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.napontaratan.wifi.model.WifiConnection;

/**
 * Offline database to store new WifiConnections waiting to be pushed to the remote server
 * Implemented as a Queue
 * 
 * @author Napon Taratan
 */
public class OfflineBuffer extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "OFFLINE_DATABASE";

	private static final String TABLE_NAME = "WIFI_CONNECTION_TABLE";
	private static final String KEY_ID = "ID";
	private static final String KEY_DATA = "WIFI_CONNECTION";

	///temp
	private static final String KEY_TIME = "TIME";
	
	public OfflineBuffer(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_BUFFER_TABLE = 
				"CREATE TABLE " + TABLE_NAME + 
				" ( " + 
						KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
						KEY_DATA + " BLOB, " + 
						KEY_TIME + " INTEGER" +
				" )";
		db.execSQL(CREATE_BUFFER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		this.onCreate(db);
	}
	
	/**
	 * Pushes a WifiConnection object to the database.
	 * 
	 * @author Napon Taratan
	 * @author Kurt Ahn
	 */
	public void push(WifiConnection connection) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		
		try {
			// push if cursor is empty or last update was long enough ago
			if (shouldPush(connection)) {
				values.put(KEY_DATA, serialize(connection));
				values.put(KEY_TIME, connection.timeDiscovered.getTime());
				
				if (database.insert(TABLE_NAME, null, values) == -1)
					throw new IOException();
			}
		} catch (IOException e) {
			System.out.println("Failed to push new entry.");
			e.printStackTrace();
		} finally {
			database.close();
		}
	}
	
	/**
	 * Determines whether a WifiConnection object should be pushed.
	 * An object should be pushed if it's scanned for the first time
	 * or hasn't been scanned for a long time. (5 minutes at the moment)
	 * 
	 * @param connection
	 * @return True if object should be pushed, false otherwise.
	 */
	private boolean shouldPush(WifiConnection connection) {
		SQLiteDatabase database = this.getWritableDatabase();
		Cursor cursor = null;
		
		try {
			cursor = database.query(
					TABLE_NAME,
					new String[] {KEY_DATA, KEY_TIME},
					KEY_DATA + " = ?", 
					new String[] {new String(serialize(connection))},
					null, 
					null,
					KEY_TIME + " DESC");
			
			if (!cursor.moveToFirst())
				return false;
			
			long lastUpdate = 
					cursor.getLong(cursor.getColumnIndex(KEY_TIME));
			
			return (connection.timeDiscovered.getTime() - lastUpdate) > 300000L; // 5 minutes
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		
		return false;
	}
	
	/**
	 * Pops, i.e. retrieves and deletes, the first item.
	 * 
	 * @return Next entry
	 * @author Napon Taratan
	 */
	public WifiConnection pop() {
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
		WifiConnection obj = null;
		
		try {
			if (c.moveToFirst()) {
				obj = (WifiConnection) OfflineBuffer.deserialize(c.getBlob(1));
				String objId = c.getString(c.getColumnIndex(KEY_ID));
				db.delete(TABLE_NAME, KEY_ID + "=?", new String[] {objId});
			}
		} catch (IOException e) {
			System.out.println("Failed to pop entry.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			c.close();
			db.close();
		}
		
		return obj;
	}
	
	/**
	 * Return true if OfflineWifiDB is empty
	 * @author Napon Taratan
	 */
	public boolean isEmpty() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		int count = cursor.getCount();
		cursor.close();
		db.close();
		return count == 0;
	}

	/**
	 * Converts an array of bytes back to its object form
	 * @param data Data to deserialize
	 * @return Deserialized object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @author Napon Taratan
	 */
	public static Object deserialize(byte[] data) 
			throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	/**
	 * Converts a Serializable object into an array of bytes
	 * to be stored into the database.
	 * @param obj Object to serialize
	 * @return Serialized data
	 * @throws IOException
	 * 
	 * @author Napon Taratan
	 */
	public static byte[] serialize(Object obj) 
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}
}
