package tomi.piipposoft.blankspellbook.drawer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Database.PowerListContract;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.powerlist.SpellBookActivity;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DrawerPresenter{

    protected final BlankSpellBookContract.DBHelper mDbHelper;
    protected final DrawerContract.View mDrawerView;
    private SQLiteDatabase mDb;
    private final String TAG = "DrawerPresenter";


    public DrawerPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull DrawerContract.View drawerView){

        mDbHelper = dbHelper;
        mDrawerView = drawerView;
    }


    protected void addNewPowerList(@NonNull String powerListName) {

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



        //get the data from DB and tell view to update itself
        mDrawerView.showPowerList(fetchSpellBookListDataFromDB(new ArrayList<IDrawerItem>()));

    }

    protected void addNewDailyPowerList(@NonNull String dailyPowerListName){

        mDb = this.mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME,
        dailyPowerListName);

        mDb.insert(
                BlankSpellBookContract.DailyPowerListEntry.TABLE_NAME,
                null,
                values
        );

//        mDrawerView.showDailyPowerList();
    }

    protected void showPowerLists(){

        mDrawerView.showPowerList(fetchSpellBookListDataFromDB(new ArrayList<IDrawerItem>()));
    }

    private List<IDrawerItem> fetchSpellBookListDataFromDB(List<IDrawerItem> spellBooks){

        //// TODO: 11-Apr-16 should most likely be put to asynctask at some point

        mDb = mDbHelper.getReadableDatabase();

        //get all spell books and daily spell lists from DB

        String[] projection = {
                BlankSpellBookContract.PowerListEntry._ID,
                BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
        };

        String sortOrder = BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " DESC";

        try {

            Cursor cursor = mDb.query(
                    BlankSpellBookContract.PowerListEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );


        //generate drawerItems with data from DB
        spellBooks = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                spellBooks.add(initializeSpellBookListItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID))
                ));

                Log.d(TAG, "_ID of item found: " + cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID)));

            }
        } finally {
            cursor.close();
        }

        }
        catch(SQLiteException e){
//            Toast.makeText(callerActivity, "Something went wrong with database, sorry!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }

        return spellBooks;
    }


    private PrimaryDrawerItem initializeSpellBookListItem(String itemName, final Long itemId) {

        return new PrimaryDrawerItem()
                .withName(itemName)
                .withIdentifier(itemId);

    }

}
