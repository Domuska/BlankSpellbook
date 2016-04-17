package tomi.piipposoft.blankspellbook.drawer;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Utils.DataSource;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DrawerPresenter{

    protected final BlankSpellBookContract.DBHelper mDbHelper;
    protected final DrawerContract.View mDrawerView;
    private SQLiteDatabase mDb;


    public DrawerPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull DrawerContract.View drawerView){

        mDbHelper = dbHelper;
        mDrawerView = drawerView;
    }


    public void addNewPowerList(@NonNull String powerListName) {

        mDb = this.mDbHelper.getWritableDatabase();

        //save the new daily power list to DB
        ContentValues values = new ContentValues();
        values.put(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME,
                powerListName);

        mDb.insert(
                BlankSpellBookContract.PowerListEntry.TABLE_NAME,
                null,
                values
        );

        //tell view to update itself
        mDrawerView.showPowerList();

    }


//    public void addDailyPowerList(@NonNull String dailyPowerListName) {
//
//    }
//
//
//    public void listPowerListItemClicked(@NonNull long itemId) {
//
//    }
//
//
//    public void listDailyPowerListItemClicked(@NonNull long itemId) {
//
//    }
//
//
//    public void powerListProfileSelected() {
//
//    }
//
//
//    public void dailyPowerListProfileSelected() {
//
//    }
}
