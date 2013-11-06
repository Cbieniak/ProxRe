package com.BienProgramming.proxre;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**Simple Sql database class
 * 
 * @author Christian adapted from Android Hive tutorial
 *
 */
public class Database extends SQLiteOpenHelper{
	public static final int DB_VER =1;

    private static final String DB_NAME = "contactsManager";
 
    private static final String TABLE_CONTACTS = "contacts";
    
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DEV_ID = "dev_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PH_NAME = "phone_name";
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DEV_ID + " TEXT," + KEY_NAME + " TEXT,"
                + KEY_PH_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
		
	}
	public void addContact(Contact contact) {
	    SQLiteDatabase db = this.getWritableDatabase();
	 
	    ContentValues values = new ContentValues();
	    values.put(KEY_DEV_ID, contact.getDevice_id()); // Contact Device id
	    values.put(KEY_NAME, contact.getName()); // Contact Name
	    values.put(KEY_PH_NAME, contact.getPhone_name()); // Contact Phone Name
	 
	    // Inserting Row
	    db.insert(TABLE_CONTACTS, null, values);
	    db.close(); // Closing database connection
	}
	
	public Contact getContact(int id) {
	    SQLiteDatabase db = this.getReadableDatabase();
	 
	    Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
	            KEY_NAME, KEY_PH_NAME }, KEY_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Contact contact = new Contact(
	           cursor.getString(1), cursor.getString(3), cursor.getString(2));
	    // return contact
	    return contact;
	}
	
	public List<Contact> getAllContacts() {
	    List<Contact> contactList = new ArrayList<Contact>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Contact contact = new Contact();
	            contact.setDevice_id(cursor.getString(1));
	            contact.setName(cursor.getString(2));
	            contact.setPhone_name(cursor.getString(3));
	            // Adding contact to list
	            contactList.add(contact);
	        } while (cursor.moveToNext());
	    }
	 
	    // return contact list
	    return contactList;
	}
	 public int getContactsCount() {
	        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
	        SQLiteDatabase db = this.getReadableDatabase();
	        Cursor cursor = db.rawQuery(countQuery, null);
	        cursor.close();
	
	        return cursor.getCount();
	    }
	 
	 public int updateContact(Contact contact) {
		    SQLiteDatabase db = this.getWritableDatabase();
		 
		    ContentValues values = new ContentValues();
		    values.put(KEY_DEV_ID, contact.getDevice_id()); // Contact Device id
		    values.put(KEY_NAME, contact.getName()); // Contact Name
		    values.put(KEY_PH_NAME, contact.getPhone_name()); // Contact Phone Name
		 
		    // updating row
		    return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
		            new String[] { String.valueOf(contact.getId()) });
		}
	 
	 public void deleteContact(Contact contact) {
		    SQLiteDatabase db = this.getWritableDatabase();
		    db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
		            new String[] { String.valueOf(contact.getId()) });
		    db.close();
		}
}
