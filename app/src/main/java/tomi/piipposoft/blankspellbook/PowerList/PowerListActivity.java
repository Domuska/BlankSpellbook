package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
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
import java.util.List;

import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Drawer.DrawerContract;
import tomi.piipposoft.blankspellbook.Drawer.DrawerHelper;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;

/**
 * Activity where all user's spell books are listed in a list
 *
 * http://developer.android.com/training/basics/data-storage/databases.html
 *
 * Uses library by bignerdranch: https://github.com/bignerdranch/expandable-recycler-view
 * https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/?utm_source=Android+Weekly&utm_campaign=8f0cc3ff1f-Android_Weekly_165&utm_medium=email&utm_term=0_4eb677ad19-8f0cc3ff1f-337834121
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

    private DrawerContract.UserActionListener myDrawerActionListener;
    private PowerListContract.UserActionListener myActionListener;

    private final String TAG = "SpellBookActivity";
    private String powerListId;
    private String powerListName;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private PowerListRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private DrawerHelper mDrawerHelper;
    ArrayList<Spell> spellList;
    List<SpellGroup> spellGroups;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_list);

        Intent thisIntent = getIntent();
        powerListId = thisIntent.getStringExtra(EXTRA_POWER_LIST_ID);
        powerListName = thisIntent.getStringExtra(EXTRA_POWER_LIST_NAME);
        Log.d(TAG, "ID got from extras: " + powerListId + " name got from extras: " + powerListName);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        //use new fancy lambda functionality!
        //fab.setOnClickListener(view -> myActionListener.openPowerDetails(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myActionListener.openPowerDetails(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(powerListName);
        }

        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDrawerHelper = DrawerHelper.getInstance(this, (Toolbar)findViewById(R.id.my_toolbar));
        //initialize listeners
        myActionListener = new PowerListPresenter(
                DataSource.getDatasource(this),
                this,
                DrawerHelper.getInstance(this, (Toolbar) findViewById(R.id.my_toolbar)));

        myDrawerActionListener = (DrawerContract.UserActionListener) myActionListener;
        myDrawerActionListener.powerListProfileSelected();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);

        //spellList = myActionListener.getSpellList(getApplicationContext(), powerListId);
        myActionListener.getSpellList(getApplicationContext(), powerListId);

        spellGroups = new ArrayList<>();

        adapter = new PowerListRecyclerAdapter(this, spellGroups, myActionListener);

//        adapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
//            @Override
//            public void onListItemExpanded(int position) {
////                SpellGroup expandedGroup = spellGroups.get(position);
////                Toast.makeText(PowerListActivity.this,
////                        "Spell group " + expandedGroup.getGroupName() + "expanded",
////                        Toast.LENGTH_SHORT)
////                        .show();
//            }
//
//            @Override
//            public void onListItemCollapsed(int position) {
//
//            }
//        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
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
    public void showPowerDetailsUI(String itemId) {
        Intent i = new Intent (this, PowerDetailsActivity.class);
        Log.d(TAG, "setting spell ID as extra: " + itemId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID, itemId);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_ID,
                powerListId);
        startActivity(i);
    }

    @Override
    public void showNewPowerUI() {
        Intent i = new Intent(this, PowerDetailsActivity.class);
        Log.d(TAG, "opening new power details UI");
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID,
                PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS);
        i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_ID,
                powerListId);
        startActivity(i);
    }


    @Override
    public void addSpellToList(Spell spell) {
        Log.d(TAG, "Got a spell to be added to adapter: " + spell.getName());

        String groupName = spell.getGroupName();
        //extra object allocation, would be good to just use a string or somesuch
        //to see if the spellgroup is already in the spellgroups list. see spellgroup .equals
        SpellGroup testableGroup = new SpellGroup(groupName, new Spell());

        if(spellGroups.contains(testableGroup)){
            Log.d(TAG, "spellgroups has the group " + groupName);
            Log.d(TAG, "" + spellGroups.get(spellGroups.indexOf(testableGroup)));
            spellGroups.get(spellGroups.indexOf(testableGroup)).addSpell(spell);
        }
        else{
            Log.d(TAG, "spellgroups does not yet have group " + groupName);
            SpellGroup group = new SpellGroup(spell.getGroupName(), spell);
            spellGroups.add(group);
            adapter.notifyParentItemInserted(spellGroups.size()-1);
        }
    }


    @Override
    public void removeSpellFromList(Spell spell) {
        Log.d(TAG, "starting to remove spell with name " + spell.getName());

        //sort of unnecessary object creation. Is there a better way?
        int spellGroupIndex = spellGroups.indexOf(new SpellGroup(spell.getGroupName(), new Spell()));
        SpellGroup group = spellGroups.get(spellGroupIndex);
        Log.d(TAG, "spell group index: " + spellGroupIndex + " group name: " + group.getGroupName());

        //SpellGroup.removeSpell returns the index of the child removed
        int removedChildIndex = group.removeSpell(spell);
        Log.d(TAG, "removed child's index: " + removedChildIndex);
        adapter.notifyChildItemRemoved(spellGroupIndex, removedChildIndex);
        if(group.getListSize() == 0){
            spellGroups.remove(group);
            adapter.notifyParentItemRemoved(spellGroupIndex);
        }

        adapter = new PowerListRecyclerAdapter(this, spellGroups, myActionListener);
        recyclerView.setAdapter(adapter);
    }

    // FROM DRAWER CONTRACT ACTIVITY VIEW INTERFACE


    @Override
    public void openPowerList(String powerListId, String powerListName) {
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
        myDrawerActionListener.powerListProfileSelected();
    }

    @Override
    public void dailyPowerListProfileSelected() {
        myDrawerActionListener.dailyPowerListProfileSelected();
    }

    @Override
    public void powerListClicked(IDrawerItem clickedItem) {
        PrimaryDrawerItem item = (PrimaryDrawerItem)clickedItem;
        myDrawerActionListener.powerListItemClicked(
                (String)item.getTag(),
                item.getName().toString());
    }

    @Override
    public void dailyPowerListClicked(IDrawerItem clickedItem) {
        myDrawerActionListener.dailyPowerListItemClicked(clickedItem.getIdentifier());
    }

    // FROM POPUP FRAGMENT INTERFACES

    // The method that is called when positive button on SetSpellbookNameDialog is clicked
    @Override
    public void onSetPowerListNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
        myDrawerActionListener.addPowerList(powerListName);
    }

    @Override
    public void onSetDailyPowerNameDialogPositiveClick(DialogFragment dialog, String dailyPowerListName) {
        myDrawerActionListener.addDailyPowerList(dailyPowerListName);
    }

    private class RecyclerDividerDecorator extends RecyclerView.ItemDecoration{

        private final int[] attrs = new int[]{android.R.attr.listDivider};
        private Drawable divider;

        public RecyclerDividerDecorator(Context context){
            final TypedArray styledAttributes = context.obtainStyledAttributes(attrs);
            divider = styledAttributes.getDrawable(0);
            styledAttributes.recycle();
        }

        public RecyclerDividerDecorator(Context context, int resId) {
            divider = ContextCompat.getDrawable(context, resId);
        }


        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);

            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
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