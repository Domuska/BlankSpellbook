package tomi.piipposoft.blankspellbook;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import tomi.piipposoft.blankspellbook.Database.PowerContract;
import tomi.piipposoft.blankspellbook.Database.PowerListContract;

/**
 * Activity where all user's spell books are listed in a list
 *
 * http://developer.android.com/training/basics/data-storage/databases.html
 *
 * This activity uses https://github.com/mikepenz/MaterialDrawer
 */
public class SpellBookActivity extends AppCompatActivity {

    //TODO: put this field to preferences maybe?
    public static final String EXTRA_POWER_BOOK_ID = "powerBookId";

    //private SpellbookContract.SpellbookListHelper powerListDbHelper;
    //private PowerListContract.PowerListHelper powerListDbHelper;
    private PowerContract.PowerHelper powerDbHelper;
    private SQLiteDatabase myDb;


    private final String TAG = "SpellBookActivity";
    private String powerBookId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_book);

        Intent thisIntent = getIntent();
        powerBookId = thisIntent.getStringExtra(EXTRA_POWER_BOOK_ID);
        Log.d(TAG, "ID got from extras: " + powerBookId);


        /*Log.d(TAG, "Instantiating powerListDbHelper");
        //powerListDbHelper = new PowerListContract.PowerListHelper(getApplicationContext());
        powerDbHelper = new PowerContract.PowerHelper(getApplicationContext());

        //myDb = powerListDbHelper.getWritableDatabase();
        myDb = powerDbHelper.getWritableDatabase();

        ContentValues values2 = new ContentValues();
        //values2.put(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_ID, 123);
        values2.put(PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_ID, 123);
        //values2.put(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME, "Askon spellbook");
        values2.put(PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME, "Askon 2 spellbook");

        long newRowId2;
        newRowId2 = myDb.insert(
                //PowerListContract.PowerListEntry.TABLE_NAME,
                PowerContract.PowerListEntry.TABLE_NAME,
                null,
                values2);



        //myDb = powerListDbHelper.getReadableDatabase();
        myDb = powerDbHelper.getReadableDatabase();

        //specify which columns from the database we use
        String[] projection2 = {
                //PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME,
                PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME,
                //PowerListContract.PowerListEntry._ID};
                PowerContract.PowerListEntry._ID};

        //define how the results will be sorted in the resulting cursor
        //String sortOrder2 = PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " DESC";
        String sortOrder2 = PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME + " DESC";



        Cursor cursor2 = myDb.query(
                //PowerListContract.PowerListEntry.TABLE_NAME,
                PowerContract.PowerListEntry.TABLE_NAME,
                projection2,
                null,
                null,
                null,
                null,
                sortOrder2
        );

        cursor2.moveToFirst();
        //Long itemId = cursor2.getLong(cursor2.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID));
        Long itemId = cursor2.getLong(cursor2.getColumnIndexOrThrow(PowerContract.PowerListEntry._ID));

        //Toast.makeText(this, itemId.toString(), Toast.LENGTH_SHORT).show();


        //get the ID of a power list named "Askon spellbook" to be used in insert below

        //pitäskö tässä käyttää _ID eikä power list ID?
        String[] projection3 = {
                //PowerListContract.PowerListEntry._ID
                PowerContract.PowerListEntry._ID
        };

        Cursor cursor3 = myDb.query(
                //PowerListContract.PowerListEntry.TABLE_NAME,
                PowerContract.PowerListEntry.TABLE_NAME,
                projection3,
                null,
                null,
                null,
                null,
                null
        );

        cursor3.moveToFirst();
        //Long powerListId = cursor3.getLong(cursor3.getColumnIndexOrThrow(PowerListContract.PowerListEntry._ID));
        Long powerListId = cursor3.getLong(cursor3.getColumnIndexOrThrow(PowerContract.PowerListEntry._ID));

        powerDbHelper = new PowerContract.PowerHelper(getApplicationContext());

        myDb = powerDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(PowerContract.PowerEntry.COLUMN_NAME_POWER_ID, 12);
        values.put(PowerContract.PowerEntry.COLUMN_NAME_POWER_NAME, "Chaos Bolt");
        values.put(PowerContract.PowerEntry.COLUMN_NAME_RANGE, "Ranged Spell");
        values.put(PowerContract.PowerEntry.COLUMN_NAME_POWER_LIST_ID, powerListId);

        Toast.makeText(this, powerListId.toString(), Toast.LENGTH_SHORT).show();

        long newRowId;
        newRowId = myDb.insert(
                PowerContract.PowerEntry.TABLE_NAME,
                null,
                values);

        myDb = powerDbHelper.getReadableDatabase();

        String[] projection = {
            PowerContract.PowerEntry.COLUMN_NAME_POWER_NAME,
            PowerContract.PowerEntry._ID
        };

        String sortOrder = PowerContract.PowerEntry.COLUMN_NAME_POWER_NAME + " DESC"; //TODO: in the actual case sort by group or something

        Cursor cursor = myDb.query(
                PowerContract.PowerEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        cursor.moveToFirst();
        String powerName = cursor.getString(cursor.getColumnIndex(PowerContract.PowerEntry.COLUMN_NAME_POWER_NAME));
        Toast.makeText(this, powerName, Toast.LENGTH_SHORT).show();



        //Check if the ID that is in chaos bolt's row can actually get the name of the spell book it belongs to


        projection = new String[]{
                PowerContract.PowerEntry.COLUMN_NAME_POWER_LIST_ID
        };

        cursor = null;
        cursor = myDb.query(
                PowerContract.PowerEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        Long powerListId2 = cursor.getLong(cursor.getColumnIndexOrThrow(PowerContract.PowerEntry.COLUMN_NAME_POWER_LIST_ID));



        //now use the ID to get the name of the powerList that the chaos bolt is in
        //myDb = powerListDbHelper.getReadableDatabase();


        projection = new String[]{
                //PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
                PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME
        };

        //String selection = PowerListContract.PowerListEntry._ID +  " = ?";
        String selection = PowerContract.PowerListEntry._ID +  " = ?";

        Log.d(TAG, "ID used to get power list name: "+ powerListId2.toString());
        String[] selectionArgs = {
                powerListId2.toString()
        };

        cursor = null;
        cursor = myDb.query(
                //PowerListContract.PowerListEntry.TABLE_NAME,
                PowerContract.PowerListEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        //String powerListName = cursor.getString(cursor.getColumnIndexOrThrow(PowerListContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME));
        String powerListName = cursor.getString(cursor.getColumnIndexOrThrow(PowerContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME));

        Toast.makeText(this, powerListName, Toast.LENGTH_LONG).show();

        */

        //TODO put here recyclerview to show the list of items


        Log.d(TAG, "Instantiating powerDbHelper");
        powerDbHelper = new PowerContract.PowerHelper(getApplicationContext());

        myDb = powerDbHelper.getWritableDatabase();

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //TODO: kun profiilia vaihdetaan jotenkin pitäisi saada tyhjennettyä nykyinen itemien lista: https://github.com/mikepenz/MaterialDrawer/issues/860

        //Create the header for the drawer...
        AccountHeader drawerHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header_background)
                .addProfiles(new ProfileDrawerItem().withName("Spell Books").withIcon(getResources().getDrawable(R.drawable.iqql_spellbook_billfold)), new ProfileDrawerItem().withName("Power List"))
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        //drawer.removeAllItems();
                        return true;
                    }
                })
                .build();



        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Sepon Spellbook");
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Askon Spellbook");


        //Create the drawer itself
        //TODO: Notice, the position you get in onItemClick might not be correct, headers are apparently also items in it and might screw up things if you use positions

        //TODO: we can add a custom view as header of the drawer with .withHeader, could use this instead of monkeying around with accountHeader:
        //http://stackoverflow.com/questions/7838921/android-listview-addheaderview-nullpointerexception-for-predefined-views-defin/7839013#7839013
        //https://github.com/mikepenz/MaterialDrawer/issues/760
        Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(drawerHeader)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener(){

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Toast.makeText(SpellBookActivity.this, "painoit nappulaa kohdassa " + position, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "painoit nappulaa kohdassa " + position);
                        return true;
                    }
                })
                .build();






        //String spellBookName = cursor.getString(cursor.getColumnIndex(SpellBookContract.SpellBookList.COLUMN_NAME_POWER_LIST_NAME));
        //Toast.makeText(this, spellBookName, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spell_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
