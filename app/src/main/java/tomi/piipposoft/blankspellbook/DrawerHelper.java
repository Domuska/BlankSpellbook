package tomi.piipposoft.blankspellbook;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

/**
 * Created by Domu on 11-Apr-16.
 */
public class DrawerHelper {

    private static SQLiteDatabase myDb;
    private static BlankSpellBookContract.PowerListHelper powerDbHelper;
    private static Activity callerActivity;
    private static String TAG;

    private static final long SPELL_BOOK_PROFILE_IDENTIFIER = 1;
    private static final long DAILY_POWER_PROFILE_IDENTIFIER = 2;
    private static final long ADD_SPELL_BOOK_FOOTER_IDENTIFIER = 3;

    private static Drawer mDrawer;
    private static List<IDrawerItem> spellBooks;

    public static void createDrawer(Activity activity, Toolbar toolbar) {

        callerActivity = activity;
        TAG = "createDrawer, called by " + activity.getLocalClassName();

        fetchDataFromDB();

        //Create the drawer itself
        mDrawer = new DrawerBuilder()
                .withActivity(callerActivity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //Toast.makeText(callerActivity, "ID of the spell book: " + drawerItem.getIdentifier(), Toast.LENGTH_SHORT).show();

                        if (drawerItem.getIdentifier() == ADD_SPELL_BOOK_FOOTER_IDENTIFIER) {
                            populateDBHelperMethod();
                            populateSpellBooksList(mDrawer, spellBooks);
                        } else {
                            //this whole onItemClick thing should not be here, see populateSpellBooksList TODO
                        }

                        Log.d(TAG, "Drawer item identifier: " + drawerItem.getIdentifier());
                        return true;
                    }
                })
                .build();

        //initially populate the list with items
        populateSpellBooksList(mDrawer, spellBooks);


        final ProfileDrawerItem spellBooksProfile = new ProfileDrawerItem().withName("Spell Books")
                .withIcon(callerActivity.getResources().getDrawable(R.drawable.iqql_spellbook_billfold))
                .withIdentifier(SPELL_BOOK_PROFILE_IDENTIFIER);
        final ProfileDrawerItem dailySpellsProfile = new ProfileDrawerItem().withName("Power List")
                .withIdentifier(DAILY_POWER_PROFILE_IDENTIFIER);


        //create the header for the drawer and register it to the drawer
        AccountHeader drawerHeader = new AccountHeaderBuilder()
                .withActivity(callerActivity)
                .withHeaderBackground(R.drawable.drawer_header_background)
                .addProfiles(spellBooksProfile, dailySpellsProfile)
                .withProfileImagesVisible(false)
                .withDrawer(mDrawer)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        mDrawer.removeAllItems();

                        if (profile.equals(spellBooksProfile)) {
                            populateSpellBooksList(mDrawer, spellBooks);
                        } else if (profile.equals(dailySpellsProfile)) {
                            //drawer.addItem(item3);
                        }
                        return true;
                    }
                })
                .build();

    }

    public static DrawerLayout getDrawerLayout(){
        return mDrawer.getDrawerLayout();
    }

    private static void fetchDataFromDB(){

        powerDbHelper = new BlankSpellBookContract.PowerListHelper(callerActivity.getApplicationContext());
        myDb = powerDbHelper.getReadableDatabase();

        //get all spell books and daily spell lists from DB
        //should this be put into an asynctask?

        String[] projection = {
                BlankSpellBookContract.PowerListEntry._ID,
                BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
        };

        String sortOrder = BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " DESC";

        try {

            Cursor cursor = myDb.query(
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
            int i = 0;
            try {
                while (cursor.moveToNext()) {
                    spellBooks.add(new PrimaryDrawerItem()
                            .withName(cursor.getString(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)))
                            .withIdentifier(cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID))));
                    Log.d(TAG, "_ID of item found: " + cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID)));
                    i++;
                }
            } finally {
                cursor.close();
            }

        }
        catch(SQLiteException e){
            Toast.makeText(callerActivity, "Something went wrong with database, sorry", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SQLite exception caught!");
            e.printStackTrace();
        }
    }

    private static void populateSpellBooksList(Drawer drawer, List<IDrawerItem> collection){

        fetchDataFromDB();
        drawer.removeAllItems();

        if(collection != null) {
            //// TODO: 11-Apr-16 add onDrawerItemClickListeners to the drawer items
            for (int i = 0; i < collection.size(); i++) {
                drawer.addItem(collection.get(i));
            }
        }

        if(drawer.getStickyFooter() == null) {

            drawer.addStickyFooterItem(new PrimaryDrawerItem()
                    .withName("Add new spell book")
                    .withIdentifier(ADD_SPELL_BOOK_FOOTER_IDENTIFIER));
        }
    }

    private static void populateDBHelperMethod(){

        myDb = powerDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME, "Suikan priestin power list");
        myDb.insert(
                BlankSpellBookContract.PowerListEntry.TABLE_NAME,
                null,
                values
        );
    }



}

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