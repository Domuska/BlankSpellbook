package tomi.piipposoft.blankspellbook.Drawer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Utils.DataSource;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DrawerPresenter implements DrawerContract.UserActionListener{

    // TODO: 5.5.2017 removal and re-adding of listeners (spellListReference & dailySpellListReference) should be possible
    // TODO: 10.5.2017 database actions should be moved from here to somewhere else. Maybe DataSource? or another class like it?
    private static BlankSpellBookContract.DBHelper mDbHelper;
    protected static DrawerContract.View mDrawerView;
    private DrawerContract.ViewActivity mDrawerActivityView;
    private SQLiteDatabase mDb;
    private static final String TAG = "DrawerPresenter";

    //use static variables for these since it may be that the app is started
    //from different activities, all need to know if a listener is already attached
    private static ChildEventListener powerListChildListener;
    private static ChildEventListener dailyPowerListChildListener;



    public DrawerPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull DrawerContract.View drawerView,
            @NonNull DrawerContract.ViewActivity drawerActivity){

        mDbHelper = dbHelper;
        mDrawerView = drawerView;
        mDrawerActivityView = drawerActivity;
        if(powerListChildListener != null)
            DataSource.removePowerListListener(powerListChildListener);
        if(dailyPowerListChildListener != null)
            DataSource.removeDailyPowerListListener(dailyPowerListChildListener);
        powerListChildListener = null;
        dailyPowerListChildListener = null;
    }

    /**
     * Called by a data source to tell there is a new power list to be displayed
     * @param powerListName The name of the power list
     * @param powerListId The ID of the power list
     */
    public static void handlePowerList(String powerListName, String powerListId){
        Log.d(TAG, "adding new powerList to drawer, name: " + powerListName + " id: " + powerListId);
        mDrawerView.addDrawerItem(initializeSpellBookListItem(powerListName, powerListId));
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
            DataSource.removePowerListListener(powerListChildListener);
            powerListChildListener = null;
        }
        if(dailyPowerListChildListener != null){
            DataSource.removeDailyPowerListListener(dailyPowerListChildListener);
            dailyPowerListChildListener = null;
        }
        showPowerLists();
    }

    /**
     * Called by a data source to tell there is a new power list to be displayed
     * @param dailyPowerListName Name of the daily power list
     * @param dailyPowerListId ID of the daily power list
     */
    public static void handleDailyPowerList(String dailyPowerListName, String dailyPowerListId){
        mDrawerView.addDrawerItem(initializeDailyPowerListItem(dailyPowerListName, dailyPowerListId));
    }

    /**
     * Handle saving a new power list to the DB, should be called by subclasses only
     * @param powerListName Name of the newly added power list
     */
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

    /**
     * Handle saving a new daily power list to the DB, should be called by subclasses only
     * @param dailyPowerListName Name of the newly added daily power list
     */
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


    /**
     * Show the Power Lists in the drawer, will attach a listener to the power lists
     * portion of database, or will fetch the power lists if listener is already attached
     */
    protected static void showPowerLists(){
        //attach listener to the DB
        if(powerListChildListener == null) {
            powerListChildListener = DataSource.attachPowerListListener(DataSource.DRAWERPRESENTER);
        }
        //we have a listener, we just need to get the data again (when an activity is resumed)
        //should this data be cached at onPause rather? Would make it more difficult to synch though
        else{
            Log.d(TAG, "showPowerLists: listener already set, just fetching data once");
            DataSource.getPowerLists(DataSource.DRAWERPRESENTER);
        }

        mDrawerView.showPowerList();
        //remove the DAILY POWER LIST listener, we're not interested in those now
        if(dailyPowerListChildListener != null){
            DataSource.removeDailyPowerListListener(dailyPowerListChildListener);
            dailyPowerListChildListener = null;
        }
    }


    /**
     * Show the daily power lists in the drawer, will attach a listener to the daily power lists
     * portion of database, or will fetch the power lists if listener is already attached
     */
    protected void showDailyPowerLists(){
        if(dailyPowerListChildListener == null)
            dailyPowerListChildListener = DataSource.attachDailyPowerListListener(DataSource.DRAWERPRESENTER);
        else
            DataSource.getDailyPowerLists(DataSource.DRAWERPRESENTER);

        mDrawerView.showDailyPowerList();

        //remove the POWER LIST listener, we're not interested in those for now
        if(powerListChildListener != null){
            DataSource.removePowerListListener(powerListChildListener);
            powerListChildListener = null;
        }
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


    //FROM DrawerContract.UserActionListener

    @Override
    public void addPowerList(@NonNull String powerListName) {
        this.addNewPowerList(powerListName);
    }

    @Override
    public void addDailyPowerList(@NonNull String dailyPowerListName) {
        this.addNewDailyPowerList(dailyPowerListName);
    }

    @Override
    public void drawerOpened() {

    }

    @Override
    public void powerListItemClicked(String itemId, String name) {
        mDrawerActivityView.openPowerList(itemId, name);
    }

    @Override
    public void dailyPowerListItemClicked(long itemId) {
        mDrawerActivityView.openDailyPowerList(itemId);
    }

    @Override
    public void powerListProfileSelected() {
        showPowerLists();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        this.showDailyPowerLists();
    }


}
