package us.nathanielapps.quicklistfree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static DatabaseOpenHelper helper           = null;
    private static final int          DATABASE_VERSION = 1;
    private static final String       LISTS_CREATE     = "CREATE TABLE " + Data.TABLE_LISTS + " (" + Data.ID
                                                               + " INTEGER PRIMARY KEY," + Data.NAME
                                                               + " TEXT NOT NULL);";
    private static final String       ITEMS_CREATE     = "CREATE TABLE " + Data.TABLE_ITEMS + " (" + Data.ID
                                                               + " INTEGER PRIMARY KEY," + Data.LIST_ID
                                                               + " INTEGER NOT NULL REFERENCES " + Data.TABLE_LISTS
                                                               + " (" + Data.ID + ")," + Data.NAME + " TEXT NOT NULL,"
                                                               + Data.VALUE + " INTEGER NOT NULL DEFAULT '0');";

    private Context                   context;

    public synchronized static DatabaseOpenHelper get(Context context) {
        if (helper == null) {
            helper = new DatabaseOpenHelper(context);
            // create database if it's not already created
            helper.getWritableDatabase();
            helper.close();
        }
        return helper;
    }

    private DatabaseOpenHelper(Context context) {
        super(context, Data.DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LISTS_CREATE);
        db.execSQL(ITEMS_CREATE);

        /*
        // Upgrade from old Quick List
        final String NEW_ITEM_PHASE_OUT = "Click here to add an item.";
        File rootDir = context.getFilesDir();
        String[] oldListNames = rootDir.list();
        if (oldListNames != null && oldListNames.length > 0) {
            Data data = new Data(context);
            for (String name : oldListNames) {
                String oldList = fileReader(rootDir, name);
                if (oldList != null) {
                    int listId = data.createList(name);
                    String item;
                    Scanner readFile = new Scanner(oldList);
                    while (readFile.hasNext()) {
                        item = "";
                        String temp = readFile.next();
                        while (!temp.equals("//listMarker//")) {
                            item += temp;
                            temp = readFile.next();
                            if (!temp.equals("//listMarker//")) {
                                item += " ";
                            }
                        }
                        if (!item.substring(1).equals(NEW_ITEM_PHASE_OUT)) { // account for older programs during
                                                                             // upgrade
                            if (item.startsWith("1")) {
                                data.addItem(listId, item.substring(1));
                            } else {
                                data.addItemChecked(listId, item.substring(1));
                            }
                        }
                    }
                }
            }
            // delete old files
            for (File oldFile : rootDir.listFiles()) {
                //oldFile.delete();
                // will implement eventually...don't want to blow away old lists yet
            }
        }
        // End Upgrade
        */
    }

    /*
     * Reader method from old Quick List Only here to read lists from there for compatibility
     */
    private String fileReader(File rootDir, String fileName) {
        String toReturn = null;
        File destFile = new File(rootDir, fileName);
        if (destFile.exists()) {
            byte[] buffer = new byte[(int) destFile.length()];
            try {
                FileInputStream fis = context.openFileInput(fileName);
                fis.read(buffer);
                fis.close();
            } catch (IOException e) {
                return null;
            }
            toReturn = new String(buffer);
        }
        return toReturn;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

}
