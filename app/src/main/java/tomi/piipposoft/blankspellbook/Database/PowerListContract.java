package tomi.piipposoft.blankspellbook.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Domu on 04-Aug-15.
 *
 * Contract class to hold the information about table that
 * holds power (spell) lists
 *
 * The contract class is used to store the column and table names
 * and to store the SQLiteOpenHelper class that takes care of
 * creating, updating and deleting the table
 *
 * More information, see:
 * http://developer.android.com/training/basics/data-storage/databases.html#DefineContract
 */

/* HUOMIO, TÄMÄ LUOKKA EI OLE ENÄÄ KÄYTÖSSÄ, ELÄ KOSKE TÄHÄN, ELÄ KÄYTÄ TÄTÄ, KÄYTÄ POWERCONTRACTIA TÄMÄN SIJAAN */

    //http://developer.android.com/training/basics/data-storage/databases.html#DbHelper
public final class PowerListContract {

    private PowerListContract(){}

    /* Inner class that defines the table contents */
    public static abstract class PowerListEntry implements BaseColumns{

        public static final String TABLE_NAME = "powerList";
        public static final String COLUMN_NAME_POWER_LIST_ID = "powerListId";
        public static final String COLUMN_NAME_POWER_LIST_NAME = "powerListName";
    }


    /**
     *
     */
    public static class PowerListHelper extends SQLiteOpenHelper {

        public static final int DATABASE_VERSION = 2;

        /*private final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
                SpellBookList.TABLE_NAME +
                " (" +
                SpellBookList.COLUMN_NAME_POWER_LIST_ID + " INTEGER PRIMARY KEY, " +
                SpellBookList.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";
        */
        private final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
                PowerListEntry.TABLE_NAME +
                " (" +
                PowerListEntry._ID + " INTEGER PRIMARY KEY, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_ID + " TEXT, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";

        private final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + PowerListEntry.TABLE_NAME;

        public PowerListHelper(Context context){
            super(context, PowerListEntry.TABLE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //here we could maybe do something smarter than to just delete the whole table if the schema is upgraded
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }



}
