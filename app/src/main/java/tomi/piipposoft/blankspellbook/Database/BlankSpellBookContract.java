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
 * A single Helper class should handle a single table
 *
 * Also holds information about spell books table, a table that holds
 * individual spell books.
 *
 * More information, see:
 * http://developer.android.com/training/basics/data-storage/databases.html#DefineContract
 */


public final class BlankSpellBookContract {

    private BlankSpellBookContract(){}
    private static final String DATABASE_NAME = "Blank_SpellBook_Database";
    private static final int DATABASE_VERSION = 2;
    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEPARATOR = ",";

    /* Defines the table representing single powers (spells) */
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
        public static final String COLUMN_NAME_GROUP = "group";
    }

    /* Defines the table representing spell books */

    public static abstract class PowerListEntry implements BaseColumns{

        public static final String TABLE_NAME = "powerList";
        public static final String COLUMN_NAME_POWER_LIST_ID = "powerListId";
        public static final String COLUMN_NAME_POWER_LIST_NAME = "powerListName";
    }

    /* Defines the table representing list of collections of daily powers  */
    public static abstract class DailyPowerListEntry implements BaseColumns{

        public static final String TABLE_NAME = "dailyPowerList";
        public static final String COLUMN_NAME_DAILY_POWER_LIST_NAME = "dailyPowerListName";
    }


    /* Defines the table representing which power lists a single power belongs to */
    public static abstract class PowerListPowerRelation implements BaseColumns{

        public static final String TABLE_NAME = "powerListPowerRelations";
        public static final String COLUMN_NAME_POWER_ID = "powerId";
        public static final String COLUMN_NAME_POWER_LIST_ID = "powerListId";
    }

    /* Defines the table representing which daily power lists a single power belongs to*/
    public static abstract class DailyPowerListPowerRelation implements BaseColumns{

        public static final String TABLE_NAME = "dailyPowerListPowerRelations";
        public static final String COLUMN_NAME_POWER_ID = "powerId";
        public static final String COLUMN_NAME_POWER_LIST_ID = "dailyPowerListId";

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
                PowerEntry.COLUMN_NAME_GROUP + TEXT_TYPE + COMMA_SEPARATOR +
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

    public static class PowerListEntryHelper extends SQLiteOpenHelper{

        private final String SQL_CREATE_POWER_LIST_TABLE = "CREATE TABLE " +
                PowerListEntry.TABLE_NAME +
                " (" +
                PowerListEntry._ID + " INTEGER PRIMARY KEY, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_ID + " TEXT, " +
                PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";

        private final String SQL_DELETE_POWERS_LIST_TABLE = "DROP TABLE IF EXISTS " + PowerListEntry.TABLE_NAME;

        public PowerListEntryHelper(Context context){
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


    public static class DailyPowerListEntryHelper extends SQLiteOpenHelper {

        private final String SQL_CREATE_DAILY_POWER_LIST_ENTRY_TABLE = "CREATE TABLE " +
                DailyPowerListEntry.TABLE_NAME +
                " (" +
                DailyPowerListEntry._ID + INTEGER_TYPE + " PRIMARY KEY, " +
                DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME + TEXT_TYPE + " )";

        private final String SQL_DELETE_DAILY_POWER_LIST_ENTRY_TABLE = "DROP TABLE IF EXISTS "
                + DailyPowerListEntry.TABLE_NAME;

        public DailyPowerListEntryHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_DAILY_POWER_LIST_ENTRY_TABLE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_DAILY_POWER_LIST_ENTRY_TABLE);
            onCreate(db);
        }
    }

    //TODO: untested
    public static class PowerListPowerRelationHelper extends SQLiteOpenHelper {

        private final String SQL_CREATE_TABLE_POWER_LIST_POWER_RELATION_TABLE = "CREATE TABLE " +
                PowerListPowerRelation.TABLE_NAME +
                " (" +
                PowerListPowerRelation._ID + " INTEGER PRIMARY KEY, " +
                PowerListPowerRelation.COLUMN_NAME_POWER_ID + INTEGER_TYPE +
                PowerListPowerRelation.COLUMN_NAME_POWER_LIST_ID + INTEGER_TYPE +
                " FOREIGN KEY(" + PowerListPowerRelation.COLUMN_NAME_POWER_ID + ") REFERENCES" +
                PowerEntry.TABLE_NAME +
                "(" + PowerEntry._ID + ")" +
                " FOREIGN KEY(" + PowerListPowerRelation.COLUMN_NAME_POWER_LIST_ID + ") REFERENCES" +
                PowerEntry.TABLE_NAME +
                "(" + PowerEntry._ID + ")" +
                " )";


        private final String SQL_DELETE_POWER_LIST_POWER_RELATION_TABLE = "DROP TABLE IF EXISTS" +
                PowerListPowerRelation.TABLE_NAME;

        public PowerListPowerRelationHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_POWER_LIST_POWER_RELATION_TABLE);
            onCreate(db);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_POWER_LIST_POWER_RELATION_TABLE);
        }
    }
}
