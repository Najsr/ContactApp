package com.example.david.contactapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by David on 10-Dec-17.
 */

public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "contacts.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_IMAGEPATH = "image";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_PHONE = "phone";

    public Database(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(id integer primary key, name text, image text, phone text,email text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public long insertContact (String name, String phone, String image, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("image", image);
        contentValues.put("phone", phone);
        return db.insert("contacts", null, contentValues);
    }

    public boolean columnExists(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        res.moveToFirst();
        return res.getCount() > 0;
    }

    public Contact getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        res.moveToFirst();
        if(res.getCount() <= 0)
            return null;

        return new Contact(res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_EMAIL)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_IMAGEPATH)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)));
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
    }

    public boolean updateContact(int id, String name, String phone, String image, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("image", image);
        contentValues.put("phone", phone);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public int deleteContact (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public Contact getLastOccurrence(String text) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts WHERE name LIKE '" + text + "%' ORDER BY name COLLATE NOCASE" , null );
        res.moveToFirst();
        if(res.getCount() <= 0)
            return null;

        return new Contact(res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_EMAIL)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_IMAGEPATH)),
                res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)));
    }

    public ArrayList<Contact> getAllContacts() {
        ArrayList<Contact> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts ORDER BY name ASC", null );
        res.moveToFirst();

        while(!res.isAfterLast()){
            Contact contact = new Contact(res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_EMAIL)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_IMAGEPATH)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)));
            array_list.add(contact);
            res.moveToNext();
        }
        return array_list;
    }
}
