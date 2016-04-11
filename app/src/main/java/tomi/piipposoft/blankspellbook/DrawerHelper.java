package tomi.piipposoft.blankspellbook;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.Database.PowerContract;
import tomi.piipposoft.blankspellbook.Database.PowerListContract;

/**
 * Created by Domu on 11-Apr-16.
 */
public class DrawerHelper {

    private static SQLiteDatabase myDb;
    private static PowerContract.PowerHelper powerDbHelper;

    public static void createDrawer(Activity activity, Toolbar toolbar) {

        final Activity callerActivity = activity;
        final String TAG = "createDrawer, called by " + activity.getLocalClassName();




        powerDbHelper = new PowerContract.PowerHelper(activity.getApplicationContext());
        myDb = powerDbHelper.getReadableDatabase();

        //get all spell books and daily spell lists from DB
        //should this be put into an asynctask?

        String[] projection = {
                PowerContract.PowerListEntry._ID,
                PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
        };

        String sortOrder = PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " DESC";

        Cursor cursor = myDb.query(
                PowerContract.PowerListEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        final List<IDrawerItem> spellBooks = new ArrayList<>();

        int i = 0;
        try {
            while (cursor.moveToNext()) {
                spellBooks.add(new PrimaryDrawerItem()
                        .withName(cursor.getString(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME)))
                        .withIdentifier(cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID))));
                Log.d(TAG, "Identifier: " + cursor.getLong(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID)));
                i++;
            }
        } finally {
            cursor.close();
        }



        //TODO: muutetaan nämä dynaamisesti luoduiksi tietokannasta
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Sepon Spellbook");
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Askon Spellbook");
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Sepon daily spellit");
        final SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("secondary drawer item");
        final SecondaryDrawerItem item7 = new SecondaryDrawerItem().withName("secondary drawer item 2");

        final List<IDrawerItem> itemList = new ArrayList<>();
        itemList.add(item4);
        itemList.add(item7);

        final PrimaryDrawerItem item5 = new PrimaryDrawerItem().withName("Section header").withEnabled(false).withSelectable(false);
        final PrimaryDrawerItem itemWithChildren = new PrimaryDrawerItem().withName("Section header").withSubItems(itemList);


        //Create the drawer itself
        //TODO: Notice, the position you get in onItemClick might not be correct, headers are apparently also items in it and might screw up things if you use positions

        //TODO: we can add a custom view as header of the drawer with .withHeader, could use this instead of monkeying around with accountHeader:
        //http://stackoverflow.com/questions/7838921/android-listview-addheaderview-nullpointerexception-for-predefined-views-defin/7839013#7839013
        //https://github.com/mikepenz/MaterialDrawer/issues/760
        final com.mikepenz.materialdrawer.Drawer drawer = new DrawerBuilder()
                .withActivity(callerActivity)
                .withToolbar(toolbar)
                /*.addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2
                )*/
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //Toast.makeText(callerActivity, "painoit nappulaa kohdassa " + position, Toast.LENGTH_SHORT).show();
                        //Log.d(TAG, "painoit nappulaa kohdassa " + position);
                        Toast.makeText(callerActivity, "ID of the spell book: " + drawerItem.getIdentifier(), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "ID of the spell book: " + drawerItem.getIdentifier());
                        return true;
                    }
                })
                .build();


        final ProfileDrawerItem spellBooksProfile = new ProfileDrawerItem().withName("Spell Books").withIcon(callerActivity.getResources().getDrawable(R.drawable.iqql_spellbook_billfold));
        final ProfileDrawerItem dailySpellsProfile = new ProfileDrawerItem().withName("Power List");


        //create the header for the drawer and register it to the drawer
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

                            //drawer.addItem(new SectionDrawerItem().withName("Saved spell books"));

                            for(int i = 0; i < spellBooks.size(); i++){
                                drawer.addItem(spellBooks.get(i));
                            }

                            drawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Add new spell book"));

                        } else if (profile.equals(dailySpellsProfile)) {
                            drawer.addItem(item3);
                        }
                        return true;
                    }
                })
                .build();


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
