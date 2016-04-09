package tomi.piipposoft.blankspellbook.Database;

/**
 * Created by Domu on 04-Aug-15.
 * TO BE DELETED
 */
/*
public class SpellBookListHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sp";

    private final String SQL_CREATE_TABLE = "CREATE TABLE" +
            SpellBookContract.SpellBookList.TABLE_NAME +
            " (" +
            SpellBookContract.SpellBookList.COLUMN_NAME_POWER_LIST_ID + " INTEGER PRIMARY KEY, " +
            SpellBookContract.SpellBookList.COLUMN_NAME_POWER_LIST_NAME + " TEXT )";

    public SpellBookListHelper(Context context){
        super(context, SpellBookContract.SpellBookList.TABLE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
*/