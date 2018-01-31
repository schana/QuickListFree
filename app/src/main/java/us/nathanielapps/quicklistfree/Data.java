package us.nathanielapps.quicklistfree;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

//abstraction for the database used by the application
public class Data {
    public static final String ID               = "_id";
    public static final String NAME             = "name";
    public static final String VALUE            = "value";
    public static final String LIST_ID          = "list_id";
    public static final String TABLE_LISTS      = "lists";
    public static final String TABLE_ITEMS      = "items";
    public static final String DATABASE_NAME    = "QuickListDatabase";

    public static final int    ORDER_TIME_ADDED = 0;
    public static final int    ORDER_ALPHA      = 1;
    public static final int    ORDER_CHECKED    = 2;

    private DatabaseOpenHelper helper;

    public Data(Context c) {
        helper = DatabaseOpenHelper.get(c);
    }

    public void addItem(int list_id, String name) {
        addItemAndValue(list_id, name, 0);
    }

    public void addItemChecked(int list_id, String name) {
        addItemAndValue(list_id, name, 1);
    }

    private void addItemAndValue(int list_id, String name, int value) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(LIST_ID, list_id);
        insertValues.put(NAME, name);
        insertValues.put(VALUE, value);
        SQLiteDatabase d = helper.getWritableDatabase();
        d.insert(TABLE_ITEMS, null, insertValues);
    }

    public void click(int item_id, int value) {
        String strFilter = ID + "=" + item_id;
        ContentValues updateValues = new ContentValues();
        updateValues.put(VALUE, value);
        SQLiteDatabase d = helper.getWritableDatabase();
        d.update(TABLE_ITEMS, updateValues, strFilter, null);
    }

    public void deleteList(int list_id) {
        // delete items from list
        String strFilter = LIST_ID + "=" + list_id;
        SQLiteDatabase d = helper.getWritableDatabase();
        d.delete(TABLE_ITEMS, strFilter, null);
        // delete list
        strFilter = ID + "=" + list_id;
        d.delete(TABLE_LISTS, strFilter, null);
    }

    public void deleteItem(int item_id) {
        String strFilter = ID + "=" + item_id;
        SQLiteDatabase d = helper.getWritableDatabase();
        d.delete(TABLE_ITEMS, strFilter, null);
    }

    public void editItem(int item_id, String newName) {
        String strFilter = ID + "=" + item_id;
        ContentValues updateValues = new ContentValues();
        updateValues.put(NAME, newName);
        SQLiteDatabase d = helper.getWritableDatabase();
        d.update(TABLE_ITEMS, updateValues, strFilter, null);
    }

    public int createList(String name) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(NAME, name);
        SQLiteDatabase d = helper.getWritableDatabase();
        int id = (int) d.insert(TABLE_LISTS, null, insertValues);
        return id;
    }

    public Cursor getList(int list_id, int order) {
        String[] cols = { ID, NAME, VALUE };
        String selection = LIST_ID + "=?";
        String[] selection_args = { Integer.toString(list_id) };
        String groupBy = null;
        String having = null;
        String orderBy = null;
        switch (order) {
        case ORDER_TIME_ADDED:
            orderBy = ID;
            break;
        case ORDER_ALPHA:
            orderBy = NAME;
            break;
        case ORDER_CHECKED:
            orderBy = VALUE;
            break;
        case ORDER_CHECKED + ORDER_ALPHA:
            orderBy = VALUE + "," + NAME;
            break;
        }

        SQLiteDatabase d = helper.getReadableDatabase();
        Cursor c = d.query(TABLE_ITEMS, cols, selection, selection_args, groupBy, having, orderBy);
        return c;
    }

    public String getListName(int list_id) {
        SQLiteDatabase d = helper.getReadableDatabase();
        Cursor c = d.query(TABLE_LISTS, new String[] { NAME }, ID + "=" + list_id, null, null, null, null);
        if (!c.moveToFirst()) {
            return null;
        }
        return c.getString(c.getColumnIndexOrThrow(NAME));
    }

    public Cursor getLists() {
        SQLiteDatabase d = helper.getReadableDatabase();
        Cursor c = d.query(TABLE_LISTS, null, null, null, null, null, null);
        return c;
    }
}
