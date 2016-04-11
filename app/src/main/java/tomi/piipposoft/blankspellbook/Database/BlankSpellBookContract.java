package tomi.piipposoft.blankspellbook.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Domu on 02-Feb-16.
 *
 * Contract class for 13th age power details
 * The contract class is used to store the column and table names
 * and to store the SQLiteOpenHelper class that takes care of
 * creating, updating and deleting the table
 *
 * Also holds information about spell books table, a table that holds
 * individual spell books
 *
 * More information, see:
 * http://developer.android.com/training/basics/data-storage/databases.html#DefineContract
 */


public final class BlankSpellBookContract {

    private BlankSpellBookContract(){}
    private static final String DATABASE_NAME = "Blank_SpellBook_Database";
    private static final int DATABASE_VERSION = 1;
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEPARATOR = ",";

    /* Inner class that defines the power table contents */
    public static abstract class PowerEntry implements BaseColumns{

        public static final String TABLE_NAME = "power";
        public static final String COLUMN_NAME_POWER_ID = "powerId";
        public static final String COLUMN_NAME_POWER_LIST_ID = "powerListId";
        public static final String COLUMN_NAME_POWER_NAME = "powerName";
        public static final String COLUMN_NAME_RANGE = "range";
        public static final String COLUMN_NAME_RECHARGE = "recharge";
        public static final String COLUMN_NAME_TARGET = "target";
        public static final String COLUMN_NAME_ATTACK = "attack";
        public static final String COLUMN_NAME_HIT_DAMAGE = "hit_damage";
        public static final String COLUMN_NAME_MISS_DAMAGE = "miss_damage";
        public static final String COLUMN_NAME_NOTES = "notes";
    }

    /* Inner class that defines the powerList table contents */

    public static abstract class PowerListEntry implements BaseColumns{

        public static final String TABLE_NAME = "powerList";
        public static final String COLUMN_NAME_POWER_LIST_ID = "powerListId";
        public static final String COLUMN_NAME_POWER_LIST_NAME = "powerListName";
    }

    public static abstract class DailyPowerListEntry implements BaseColumns{

        public static final String TABLE_NAME = "dailyPowerList";
        public static final String COLUMN_NAME_DAILY_POWER_LIST_NAME = "dailyPowerListName";
    }

    public static class PowerHelper extends SQLiteOpenHelper{

        private final String SQL_CREATE_POWERS_TABLE =
                "CREATE TABLE " + PowerEntry.TABLE_NAME +
                " (" +
                PowerEntry._ID + " INTEGER PRIMARY KEY, " +
                PowerEntry.COLUMN_NAME_POWER_ID + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_POWER_LIST_ID + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_POWER_NAME + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_RANGE + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_RECHARGE + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_TARGET + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_ATTACK + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_HIT_DAMAGE + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_MISS_DAMAGE + TEXT_TYPE + COMMA_SEPARATOR +
                PowerEntry.COLUMN_NAME_NOTES + TEXT_TYPE + COMMA_SEPARATOR +
                "FOREIGN KEY("+ PowerEntry.COLUMN_NAME_POWER_LIST_ID + ") REFERENCES " +
                PowerListEntry.TABLE_NAME +
                "(" + PowerListEntry._ID + ")"
                + " )";

        private final String SQL_DELETE_POWER_TABLE = "DROP TABLE IF EXISTS " + PowerEntry.TABLE_NAME;

        /*private final String SQL_CREATE_POWER_LIST_TABLE = "CREATE TABLE " +
                PowerListEntry.TABLE_NAME +
                " (" +
                PowerListEntry._ID + " INTEGER PRIMARY KEY, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_ID + " TEXT, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";

        private final String SQL_DELETE_POWERS_LIST_TABLE = "DROP TABLE IF EXISTS " + PowerListEntry.TABLE_NAME;*/

        public PowerHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_POWERS_TABLE);
            //db.execSQL(SQL_CREATE_POWER_LIST_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //here we could maybe do something smarter than to just delete the whole table if the schema is upgraded
            //db.execSQL(SQL_DELETE_POWERS_LIST_TABLE);
            db.execSQL(SQL_DELETE_POWER_TABLE);
            onCreate(db);
        }
    }

    public static class PowerListHelper extends SQLiteOpenHelper{

        private final String SQL_CREATE_POWER_LIST_TABLE = "CREATE TABLE " +
                PowerListEntry.TABLE_NAME +
                " (" +
                PowerListEntry._ID + " INTEGER PRIMARY KEY, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_ID + " TEXT, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";

        private final String SQL_DELETE_POWERS_LIST_TABLE = "DROP TABLE IF EXISTS " + PowerListEntry.TABLE_NAME;

        public PowerListHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_POWER_LIST_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_POWERS_LIST_TABLE);
            onCreate(db);
        }
    }
}
