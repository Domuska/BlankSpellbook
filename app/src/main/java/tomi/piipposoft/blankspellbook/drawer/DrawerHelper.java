package tomi.piipposoft.blankspellbook.drawer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;
import tomi.piipposoft.blankspellbook.Database.PowerListContract;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetSpellbookNameDialog;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.powerlist.SpellBookActivity;

/**
 * Created by Domu on 11-Apr-16.
 *
 * This activity uses MaterialDrawer library by mikePenz (https://github.com/mikepenz/MaterialDrawer)
 */
public class DrawerHelper implements DrawerContract.View{

    private static SQLiteDatabase mDb;
    private static BlankSpellBookContract.DBHelper mDbHelper;
    private static AppCompatActivity callerActivity;
    private static String TAG;

    private static final long SPELL_BOOKS_PROFILE_IDENTIFIER = -5;
    private static final long DAILY_POWERS_PROFILE_IDENTIFIER = -2;
    private static final long ADD_POWER_LIST_FOOTER_IDENTIFIER = -3;
    private static final long ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER = -4;

    private static Drawer mDrawer;
    private static List<IDrawerItem> spellBooks;
    private static List<IDrawerItem> dailyPowerLists;

    public DrawerHelper(Activity activity, Toolbar toolbar){
        createDrawer(activity, toolbar);
    }

    public void createDrawer(Activity activity, Toolbar toolbar) {

        callerActivity = (AppCompatActivity) activity;
        TAG = "createDrawer, called by " + activity.getLocalClassName();

        mDbHelper = new BlankSpellBookContract.DBHelper(callerActivity.getApplicationContext());


        createDrawer(toolbar);

        //Create the drawer itself


        //initially populate the list with items
        populateSpellBooksList(mDrawer);


        final ProfileDrawerItem spellBooksProfile = new ProfileDrawerItem().withName("Spell Books")
                .withIcon(callerActivity.getResources().getDrawable(R.drawable.iqql_spellbook_billfold))
                .withIdentifier(SPELL_BOOKS_PROFILE_IDENTIFIER);
        final ProfileDrawerItem dailySpellsProfile = new ProfileDrawerItem().withName("Daily Power Lists")
                .withIdentifier(DAILY_POWERS_PROFILE_IDENTIFIER);


        //create the header for the drawer and register it to the drawer
        AccountHeader drawerHeader = new AccountHeaderBuilder()
                .withActivity(callerActivity)
                .withHeaderBackground(R.drawable.drawer_header_background)
                .addProfiles(spellBooksProfile, dailySpellsProfile)
                .withProfileImagesVisible(false)
                .withDrawer(mDrawer)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {

                    //Handle account changing
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {

                        Log.d(TAG, "withOnAccountHeader... profile ID: " + profile.getIdentifier());

                        if (profile.getIdentifier() == SPELL_BOOKS_PROFILE_IDENTIFIER) {
                            populateSpellBooksList(mDrawer);

                        } else if (profile.getIdentifier() == DAILY_POWERS_PROFILE_IDENTIFIER) {
                            populateDailyPowersList(mDrawer);
                        }
                        return true;
                    }
                })
                .build();



    }

    public DrawerLayout getDrawerLayout(){
        return mDrawer.getDrawerLayout();
    }

    public static void updateSpellBookList(){
        populateSpellBooksList(mDrawer);
    }

    public static void updateDailyPowersList(){
        populateDailyPowersList(mDrawer);
    }


    private static void createDrawer(Toolbar toolbar){
        mDrawer = new DrawerBuilder()
                .withActivity(callerActivity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {

                    // Handle sticky footer item clicks
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem.getIdentifier() == ADD_POWER_LIST_FOOTER_IDENTIFIER) {


                            try {
                                DialogFragment dialog = new SetSpellbookNameDialog();
                                dialog.show(callerActivity.getSupportFragmentManager(), "SetSpellBookNameDialogFragment");
                            } catch(IllegalStateException e){
                                /* do nothing, apparently there is no way to avoid this error after
                                    saveInstanceState has been performed,see:
                                    http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit */
                                Log.d(TAG, "error while opening SetSpellbookNameDialog");
                                e.printStackTrace();

                            }
                            //populateSpellBooksList(mDrawer);

                        }
                        else if (drawerItem.getIdentifier() == ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER){

                            try {
                                DialogFragment dialog = new SetDailyPowerListNameDialog();
                                dialog.show(callerActivity.getSupportFragmentManager(), "SetDailyPowerListNameDialog");
                            }catch(IllegalStateException e){
                                /* do nothing, apparently there is no way to avoid this error after
                                    saveInstanceState has been performed,see:
                                    http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit */
                                Log.d(TAG, "error while opening SetDailyPowerListNameDialog");
                                e.printStackTrace();
                            }
                            //populateDailyPowersList(mDrawer);
                        }

                        //Log.d(TAG, "Drawer item identifier: " + drawerItem.getIdentifier());
                        return false;
                    }
                })
                .withCloseOnClick(false)
                .build();
    }

    @Override
    public void showPowerListItems(List<IDrawerItem> drawerItems) {

    }

    @Override
    public void showDailyPowerListItems(List<IDrawerItem> drawerItems) {

    }

    @Override
    public void showDailyPowerList() {

    }

    @Override
    public void showPowerList() {
        populateSpellBooksList(mDrawer);
    }

    /**
     * Helper method to fetch the data from database
     * Will store the data in arrayList spellBooks class variable
     */
    private static void fetchSpellBookListDataFromDB(){

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
            Toast.makeText(callerActivity, "Something went wrong with database, sorry!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to populate the drawer spell books side
     *
     * @param drawer
     */
    private static void populateSpellBooksList(Drawer drawer){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
                .withName("Add new spell book")
                .withIdentifier(ADD_POWER_LIST_FOOTER_IDENTIFIER));

        fetchSpellBookListDataFromDB();

        for (int i = 0; i < spellBooks.size(); i++) {
            drawer.addItem(spellBooks.get(i));
        }
    }

    private static void fetchDailyPowerListDataFromDB(){

        mDb = mDbHelper.getReadableDatabase();

        String[] projection = {
                BlankSpellBookContract.DailyPowerListEntry._ID,
                BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME
        };

        String sortOrder = BlankSpellBookContract.DailyPowerListEntry.
                COLUMN_NAME_DAILY_POWER_LIST_NAME + " DESC";

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

            dailyPowerLists = new ArrayList<>();

            try{
                while (cursor.moveToNext()){
                    dailyPowerLists.add(initializeDailyPowerListItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry.COLUMN_NAME_DAILY_POWER_LIST_NAME)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(BlankSpellBookContract.DailyPowerListEntry._ID))
                    ));

                }
            } finally {
                cursor.close();
            }
        }
        catch(SQLiteException e) {
            Toast.makeText(callerActivity, "Something went wrong with database, sorry!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }

        /*
        * try {




            //generate drawerItems with data from DB
            spellBooks = new ArrayList<>();
            int i = 0;
            try {
                while (cursor.moveToNext()) {
                    spellBooks.add(initializeSpellBookListItem(
                            cursor.getString(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID))
                    ));

                    Log.d(TAG, "_ID of item found: " + cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID)));
                    i++;
                }
            } finally {
                cursor.close();
            }

        }
        catch(SQLiteException e){
            Toast.makeText(callerActivity, "Something went wrong with database, sorry!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }*/

    }

    /**
     * Helper method to populate the drawer daily powers side
     *
     * @param drawer
     */
    private static void populateDailyPowersList(Drawer drawer){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
            .withName("Add new daily power list")
            .withIdentifier(ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER));

        fetchDailyPowerListDataFromDB();

        for(int i = 0; i < dailyPowerLists.size(); i++){
            drawer.addItem(dailyPowerLists.get(i));
        }
    }

    /**
     * Helper method used to initialize a drawer item in the spell book side
     *
     * @param itemName name of the item
     * @param itemId ID of the item (from database)
     * @return a newly initialized PrimaryDrawerItem with onDrawerItemClickListener added
     */
    private static PrimaryDrawerItem initializeSpellBookListItem(String itemName, final Long itemId) {

        return new PrimaryDrawerItem()
                .withName(itemName)
                .withIdentifier(itemId)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        Intent i = new Intent(callerActivity, SpellBookActivity.class);
                        i.putExtra(SpellBookActivity.EXTRA_POWER_BOOK_ID, itemId);
                        mDrawer.closeDrawer();
                        callerActivity.startActivity(i);
                        return true;
                    }
                });
    }

    private static PrimaryDrawerItem initializeDailyPowerListItem (String itemName, final Long itemId){

        return new PrimaryDrawerItem()
                .withName(itemName)
                .withIdentifier(itemId)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // TODO: 14-Apr-16 handle moving to daily power activity
                        return true;
                    }
                });
    }

}

/*
private static void populateDBHelperMethod(){

        mDb = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME, "Suikan priestin power list");
        mDb.insert(
                BlankSpellBookContract.PowerListEntry.TABLE_NAME,
                null,
                values
        );
    }
 */

/*
 AccountHeader drawerHeader = new AccountHeaderBuilder()
                .withActivity(callerActivity)
                .withHeaderBackground(R.drawable.drawer_header_background)
                .addProfiles(spellBooksProfile, dailySpellsProfile)
                .withProfileImagesVisible(false)
                .withDrawer(drawer)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        drawer.removeAllItems();

                        if (profile.equals(spellBooksProfile)) {
                            drawer.addItem(itemWithChildren);
                            drawer.addItem(new SectionDrawerItem().withName("section"));

                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();
                            drawer.addItem(item2);
                            new DividerDrawerItem();


                            drawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Add new spell book"));

                        } else if (profile.equals(dailySpellsProfile)) {
                            drawer.addItem(item3);
                        }
                        return true;
                    }
                })
                .build();
 */


/*final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Sepon Spellbook");
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Askon Spellbook");
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Sepon daily spellit");
        final SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("secondary drawer item");
        final SecondaryDrawerItem item7 = new SecondaryDrawerItem().withName("secondary drawer item 2");

        final List<IDrawerItem> itemList = new ArrayList<>();
        itemList.add(item4);
        itemList.add(item7);

        final PrimaryDrawerItem item5 = new PrimaryDrawerItem().withName("Section header").withEnabled(false).withSelectable(false);
        final PrimaryDrawerItem itemWithChildren = new PrimaryDrawerItem().withName("Section header").withSubItems(itemList);*/