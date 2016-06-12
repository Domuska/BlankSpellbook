package tomi.piipposoft.blankspellbook.powerlist;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.powerdetails.PowerDetailsActivity;

/**
 * Activity where all user's spell books are listed in a list
 *
 * http://developer.android.com/training/basics/data-storage/databases.html
 *
 *
 */
public class PowerListActivity extends AppCompatActivity
        implements SetPowerListNameDialog.NoticeDialogListener,
        SetDailyPowerListNameDialog.NoticeDialogListener,
        DrawerHelper.DrawerListener,
        PowerListContract.View,
        DrawerContract.ViewActivity{

    //TODO: put this field to preferences maybe?
    public static final String EXTRA_POWER_LIST_ID = "powerListId";
    public static final String EXTRA_POWER_LIST_NAME = "powerBookName";

    private DrawerContract.UserActionListener mDrawerActionListener;
    private PowerListContract.UserActionListener mActionListener;

    private final String TAG = "SpellBookActivity";
    private Long powerListId;
    private String powerListName;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private DrawerHelper mDrawerHelper;
    ArrayList<Spell> dataSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_list);

        Intent thisIntent = getIntent();
        powerListId = thisIntent.getLongExtra(EXTRA_POWER_LIST_ID, -1);
        powerListName = thisIntent.getStringExtra(EXTRA_POWER_LIST_NAME);
        Log.d(TAG, "ID got from extras: " + powerListId + " name got from extras: " + powerListName);

        fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "you pressed FAB!");
//                mActionListener.openPowerDetails(PowerDetailsActivity.ADD_NEW_POWER_DETAILS);
                //todo fix above
//                dataSet.add("ali bali's superior fireball");
//                adapter.notifyItemInserted(dataSet.size()-1);

//                dataSet[dataSet.length] = "ali bali's superior fireball";
//                adapter.notifyItemInserted(dataSet.length-1);
            }
        });

        //TODO put here recyclerview to show the list of items

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);

        if (toolbar != null) {
            toolbar.setTitle(powerListName);
        }

        setSupportActionBar(toolbar);

        //recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        dataSet = DataSource.getSpellsWithSpellBookId(powerListId);
        adapter = new PowerListRecyclerAdapter(dataSet);

        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mDrawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        //initialize listeners
        mActionListener = new PowerListPresenter(
                DataSource.getDatasource(this),
                this,
                DrawerHelper.getInstance(this, (Toolbar) findViewById(R.id.my_toolbar)));

        mDrawerActionListener = (DrawerContract.UserActionListener) mActionListener;

        mDrawerActionListener.powerListProfileSelected();


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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    // FROM POWER LIST CONTRACT INTERFACE

    @Override
    public void showPowerDetailUI(long itemId) {
        // TODO: 17-Apr-16 Handle opening power details page
        Intent i = new Intent (this, PowerDetailsActivity.class);
        Log.d(TAG, "setting spell ID as extra: " + itemId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID, itemId);
        startActivity(i);
    }


    // FROM DRAWER CONTRACT ACTIVITY VIEW INTERFACE

    @Override
    public void openPowerList(Long powerListId, String powerListName) {
        Intent i = new Intent(this, PowerListActivity.class);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_ID, powerListId);
        i.putExtra(PowerListActivity.EXTRA_POWER_LIST_NAME, powerListName);
        mDrawerHelper.closeDrawer();
        startActivity(i);
    }

    @Override
    public void openDailyPowerList(Long dailyPowerListId) {
        // handle opening daily power list activity
    }


    // FROM DRAWER CONTRACT VIEW INTERFACE

    @Override
    public void powerListProfileSelected() {
        mDrawerActionListener.powerListProfileSelected();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        mDrawerActionListener.dailyPowerListProfileSelected();
    }

    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        PrimaryDrawerItem item = (PrimaryDrawerItem)clickedItem;
        mDrawerActionListener.powerListItemClicked(
                item.getIdentifier(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        mDrawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
    }

    // FROM POPUP FRAGMENT INTERFACES

    // The method that is called when positive button on SetSpellbookNameDialog is clicked
    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {

    }

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {

    }



//    private void populateDBHelperMethod(){
//
//        myDb = mDbHelper.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//
//        values.put(BlankSpellBookContract.PowerListEntry.COLUMN_NAME_POWER_LIST_NAME, "Suikan priestin power list");
//        myDb.insert(
//                BlankSpellBookContract.PowerListEntry.TABLE_NAME,
//                null,
//                values
//        );
//    }


}
/*Log.d(TAG, "Instantiating powerListDbHelper");
        //powerListDbHelper = new PowerListContract.PowerListHelper(getApplicationContext());
        mDbHelper = new PowerContract.PowerHelper(getApplicationContext());

        //myDb = powerListDbHelper.getWritableDatabase();
        myDb = mDbHelper.getWritableDatabase();

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
        myDb = mDbHelper.getReadableDatabase();

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

        mDbHelper = new PowerContract.PowerHelper(getApplicationContext());

        myDb = mDbHelper.getWritableDatabase();

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

        myDb = mDbHelper.getReadableDatabase();

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