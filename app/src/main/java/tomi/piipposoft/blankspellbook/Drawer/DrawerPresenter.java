package tomi.piipposoft.blankspellbook.Drawer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Database.DailySpellList;
import tomi.piipposoft.blankspellbook.Database.SpellList;
import tomi.piipposoft.blankspellbook.Utils.DataSource;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DrawerPresenter{

    // TODO: 5.5.2017 removal and re-adding of listeners (spellListReference & dailySpellListReference) should be possible
    // TODO: 10.5.2017 database actions should be moved from here to somewhere else. Maybe DataSource? or another class like it?
    private static BlankSpellBookContract.DBHelper mDbHelper;
    private static DrawerContract.View mDrawerView;
    private SQLiteDatabase mDb;
    private static final String TAG = "DrawerPresenter";

    //use static variables for these since it may be that the app is started
    //from different activities, all need to know if a listener is already attached
    private static ChildEventListener powerListChildListener;
    private static ChildEventListener dailyPowerListChildListener;

    public DrawerPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull DrawerContract.View drawerView){

        mDbHelper = dbHelper;
        mDrawerView = drawerView;
        if(powerListChildListener != null)
            DataSource.removePowerListChildListener(powerListChildListener);
        if(dailyPowerListChildListener != null)
            DataSource.removeDailyPowerListChildListener(dailyPowerListChildListener);
        powerListChildListener = null;
        dailyPowerListChildListener = null;
    }

    public static void handlePowerList(String spellListName, String spellListId){
        mDrawerView.addDrawerItem(initializeSpellBookListItem(spellListName, spellListId));
    }

    public static void handleRemovedItem(String powerListName, String powerListId){
        /*
        This really is not the good way to handle removing items. In here we nullify our
        own listener and remove it from the database instance so we can re-initialize it
        later in showPowerLists. This is since in showPowerLists we call mDrawerView.showPowerList()
        which removes all the drawer items and the drawer items are later on added when the asynchronous
        calls from the child listeners are added.
        Since we cant find items from the drawer by anything but long IDs, this needs to be done.
        Silly drawer library.
         */
        if(powerListChildListener != null) {
            DataSource.removePowerListChildListener(powerListChildListener);
            powerListChildListener = null;
        }
        if(dailyPowerListChildListener != null){
            DataSource.removeDailyPowerListChildListener(dailyPowerListChildListener);
            dailyPowerListChildListener = null;
        }
        showPowerLists();
    }

    public static void handleDailyPowerList(String dailyPowerListName, String dailyPowerListId){
        mDrawerView.addDrawerItem(initializeDailyPowerListItem(dailyPowerListName, dailyPowerListId));
    }

    protected void addNewPowerList(@NonNull String powerListName){
        DataSource.addNewPowerList(powerListName);
    }

    // TODO: 5.5.2017 can be removed, firebase implementation done
    protected void addNewPowerList2(@NonNull String powerListName) {

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
        //mDrawerView.showPowerList(fetchSpellBookListDataFromDB(new ArrayList<IDrawerItem>()));
    }

    protected void addNewDailyPowerList(@NonNull String dailyPowerListName){
        DataSource.addNewDailyPowerList(dailyPowerListName);
    }

    // TODO: 5.5.2017 can be removed, firebase implementation is done
    protected void addNewDailyPowerList2(@NonNull String dailyPowerListName){

        mDb = this.mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME,
        dailyPowerListName);

        mDb.insert(
                BlankSpellBookContract.DailyPowerListEntry.TABLE_NAME,
                null,
                values
        );

        //mDrawerView.showDailyPowerList(fetchDailyPowerListDataFromDB(new ArrayList<IDrawerItem>()));
    }


    protected static void showPowerLists(){
        if(powerListChildListener == null) {
            powerListChildListener = DataSource.attachPowerListDrawerListener();
        }
        mDrawerView.showPowerList();
        //remove the listener to the daily power list so it will be re-initialized later
        if(dailyPowerListChildListener != null){
            DataSource.removeDailyPowerListChildListener(dailyPowerListChildListener);
            dailyPowerListChildListener = null;
        }
    }


    protected void showDailyPowerLists(){
        //DataSource.attachDailyPowerListDrawerListener();
        //attachDailyPowerListDrawerListener();
        if(dailyPowerListChildListener == null)
            dailyPowerListChildListener = DataSource.attachDailyPowerListDrawerListener();
        mDrawerView.showDailyPowerList();

        //remove listener to power lists side so it is re-initialized later
        if(powerListChildListener != null){
            DataSource.removePowerListChildListener(powerListChildListener);
            powerListChildListener = null;
        }
    }

    protected void drawerLockAndClose(){
        mDrawerView.lockDrawer();
    }

    protected void drawerUnlock(){
        mDrawerView.unlockDrawer();
    }

    /**
     * Helper method for fetching data for the power list drawer side
     */
    // TODO: 5.5.2017 can be removed, showPowerLists uses firebase already
    private List<IDrawerItem> fetchSpellBookListDataFromDB(List<IDrawerItem> powerLists){

        //// TODO: 11-Apr-16 should most likely be put to asynctask at some point

        mDb = mDbHelper.getReadableDatabase();

        //get all spell books and daily spell lists from DB

        String[] projection = {
                BlankSpellBookContract.PowerListEntry._ID,
                BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
        };

        String sortOrder = BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " ASC";

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


        try {
            while (cursor.moveToNext()) {
                //commented out since initialize was changed, second parameter used to be Long is now String
                /*powerLists.add(initializeSpellBookListItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerListEntry._ID))
                ));*/

                Log.d(TAG, "_ID of item found: " + cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerListEntry._ID)));

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

        return powerLists;
    }


    private static PrimaryDrawerItem initializeSpellBookListItem(String itemName, String identifier) {
        return new PrimaryDrawerItem()
                .withName(itemName)
                .withTag(identifier);
    }

    /**
     * Helper method for fetching data for the daily power list drawer side
     */
    // TODO: 5.5.2017 firebase in use, this can be removed, showDailyPowerLists alrady handles database
    private List<IDrawerItem> fetchDailyPowerListDataFromDB(List<IDrawerItem> dailyPowerLists) {
        mDb = mDbHelper.getReadableDatabase();

        String[] projection = {
                BlankSpellBookContract.DailyPowerListEntry._ID,
                BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME
        };

        String sortOrder = BlankSpellBookContract.DailyPowerListEntry.
                COLUMN_NAME_DAILY_POWER_LIST_NAME + " ASC";

        try {
            Cursor cursor = mDb.query(
                    BlankSpellBookContract.DailyPowerListEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );



            try {
                while (cursor.moveToNext()) {
                    //commented out since initialize was changed, second parameter used to be Long is now String
                    /*dailyPowerLists.add(initializeDailyPowerListItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry._ID))
                    ));*/
                    Log.d(TAG, "_ID of item found: " + cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry._ID)));

                }
            } finally {
                cursor.close();
            }
        } catch (SQLiteException e) {
//            Toast.makeText(callerActivity, "Something went wrong with database, sorry!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }


        return dailyPowerLists;

    }

    private static PrimaryDrawerItem initializeDailyPowerListItem(String itemName, String identifier) {
        return new PrimaryDrawerItem()
                .withName(itemName)
                .withTag(identifier);
    }


}
