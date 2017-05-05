package tomi.piipposoft.blankspellbook.Drawer;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Database.DailySpellList;
import tomi.piipposoft.blankspellbook.Database.SpellList;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DrawerPresenter{

    protected final BlankSpellBookContract.DBHelper mDbHelper;
    protected final DrawerContract.View mDrawerView;
    private SQLiteDatabase mDb;
    private final String TAG = "DrawerPresenter";



    //firebase
    private final String SPELLBOOK_REFERENCE = "spellbook";
    private final String SPELL_LISTS_REFERENCE = "spell_lists";
    private final String DAILY_SPELL_LIST_REFERENCE = "daily_power_lists";

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference spellListsReference = database.getReference(SPELL_LISTS_REFERENCE);
    private DatabaseReference dailySpellListReference = database.getReference(DAILY_SPELL_LIST_REFERENCE);


    public DrawerPresenter(
            @NonNull BlankSpellBookContract.DBHelper dbHelper,
            @NonNull DrawerContract.View drawerView){

        mDbHelper = dbHelper;
        mDrawerView = drawerView;
    }

    protected void addNewPowerList(@NonNull String powerListName){
        SpellList spellList = new SpellList(powerListName);
        spellListsReference.push().setValue(spellList);
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
        mDrawerView.showPowerList(fetchSpellBookListDataFromDB(new ArrayList<IDrawerItem>()));

    }

    protected void addNewDailyPowerList(@NonNull String dailyPowerListName){
        DailySpellList dailySpellList = new DailySpellList(dailyPowerListName);
        dailySpellListReference.push().setValue(dailySpellList);
    }

    // TODO: 5.5.2017 change to use firebase
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

        mDrawerView.showDailyPowerList(fetchDailyPowerListDataFromDB(new ArrayList<IDrawerItem>()));
    }


    protected void showPowerLists(){
        //mDrawerView.showPowerList(fetchSpellBookListDataFromDB(new ArrayList<IDrawerItem>()));

        //added after the firebase stuff was added, above is old implementation
        // TODO: 5.5.2017 should this maybe be implemented as one-time fetch, instead of listener?
        // https://firebase.google.com/docs/database/android/read-and-write
        final List<IDrawerItem> powerLists = new ArrayList<IDrawerItem>();

        spellListsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapShot : dataSnapshot.getChildren()){
                    SpellList spellList = snapShot.getValue(SpellList.class);
                    Log.d(TAG, "Spell list name: " + spellList.getName());
                    Log.d(TAG, "Spell list id: " + snapShot.getKey());
                    powerLists.add(initializeSpellBookListItem(
                            spellList.getName(),
                            (long)i));
                    i++;
                }
                //tell the drawer view to present the data
                mDrawerView.showPowerList(powerLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "fetchSpellBookListDataFromDB: " + databaseError.toException());
            }
        });
    }


    protected void showDailyPowerLists(){
        //mDrawerView.showDailyPowerList(fetchDailyPowerListDataFromDB(new ArrayList<IDrawerItem>()));

        //firebase implementation from this line forward
        final List<IDrawerItem> dailyPowerLists = new ArrayList<>();

        dailySpellListReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    DailySpellList spellList = snapshot.getValue(DailySpellList.class);
                    Log.d(TAG, "Daily spell list name: " + spellList.getName());
                    dailyPowerLists.add(initializeDailyPowerListItem(
                            spellList.getName(),
                            (long) i)
                    );
                    i++;
                }
                mDrawerView.showDailyPowerList(dailyPowerLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error at showDailyPowerLists: " + databaseError.toException());
            }
        });

    }

    // TODO: 5.5.2017 tätä metodia ei enää edes kutsuta, voidaan poistaa, tismalleen sama koodi showPowerListsissä
    private List<IDrawerItem> fetchSpellBookListDataFromDB2(final List<IDrawerItem> powerLists) {

        spellListsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapShot : dataSnapshot.getChildren()){
                    SpellList spellList = snapShot.getValue(SpellList.class);
                    Log.d(TAG, "Spell list name: " + spellList.getName());
                    powerLists.add(initializeSpellBookListItem(
                            spellList.getName(),
                            (long)i));
                    i++;
                }
                mDrawerView.showPowerList(powerLists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "fetchSpellBookListDataFromDB: " + databaseError.toException());
            }
        });

        Log.d(TAG, "Returning a list of spell lists, # of items: " + powerLists.size());
        return powerLists;
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
                powerLists.add(initializeSpellBookListItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.PowerListEntry._ID))
                ));

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


    private PrimaryDrawerItem initializeSpellBookListItem(String itemName, Long itemId) {
        return new PrimaryDrawerItem()
                .withName(itemName);
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
                    dailyPowerLists.add(initializeDailyPowerListItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry._ID))
                    ));
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

    private static PrimaryDrawerItem initializeDailyPowerListItem(String itemName, final Long itemId) {

        return new PrimaryDrawerItem()
                .withName(itemName)
                .withIdentifier(itemId)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // TODO: 14-Apr-16 handle moving to daily power activity
                        // TODO: 5.5.2017 update, this place is prolly not the right place for this
                        return true;
                    }
                });
    }


}
