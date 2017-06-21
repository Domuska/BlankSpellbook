package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.ApplicationActivity;
import tomi.piipposoft.blankspellbook.Utils.DataSource;
import tomi.piipposoft.blankspellbook.Utils.SharedPreferencesHandler;
import tomi.piipposoft.blankspellbook.Utils.Spell;
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
public class PowerListActivity extends ApplicationActivity
        implements PowerListContract.View{

    //TODO: put this field to preferences maybe?
    public static final String EXTRA_POWER_LIST_ID = "powerListId";
    public static final String EXTRA_POWER_LIST_NAME = "powerBookName";

    private PowerListContract.UserActionListener myActionListener;

    private final String TAG = "PowerListActivity";
    private String powerListId;
    private String powerListName;

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private PowerListRecyclerAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    List<SpellGroup> spellGroups;

    private ImageButton deleteButton;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_list);

        if(!SharedPreferencesHandler.isDatabasePersistanceSet(this)){
            DataSource.setDatabasePersistance();
            SharedPreferencesHandler.setDatabasePersistance(true, this);
        }

        Intent thisIntent = getIntent();
        powerListId = thisIntent.getStringExtra(EXTRA_POWER_LIST_ID);
        powerListName = thisIntent.getStringExtra(EXTRA_POWER_LIST_NAME);
        Log.d(TAG, "ID got from extras: " + powerListId + " name got from extras: " + powerListName);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        deleteButton = (ImageButton) findViewById(R.id.toolbar_delete);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //use new fancy lambda functionality!
        //fab.setOnClickListener(view -> myActionListener.openPowerDetails(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myActionListener.openPowerDetails(PowerDetailsActivity.EXTRA_ADD_NEW_POWER_DETAILS, null, null);
            }
        });

        //onclicklistener for the delete button in "menu" bar
        findViewById(R.id.toolbar_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "delete menu button clicked");
                showConfirmDeletionDialog();
            }
        });

        ((TextView)findViewById(R.id.powerlist_name_text)).setText(powerListName);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            //((AppCompatTextView)toolbar.findViewById(R.id.toolbar_title)).setText(powerListName);
        }

        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        this.drawerHelper = DrawerHelper.getInstance(this, (Toolbar) findViewById(R.id.my_toolbar));

        myActionListener = PowerListPresenter.getInstance(
                DataSource.getDatasource(this),
                this,
                this.drawerHelper,
                powerListId
        );

        this.drawerActionListener = (DrawerContract.UserActionListener) myActionListener;
        this.drawerActionListener.powerListProfileSelected();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);

        if(adapter == null) {
            spellGroups = new ArrayList<>();
            adapter = new PowerListRecyclerAdapter(this, spellGroups, myActionListener, this);
        }
        myActionListener.getPowersForDisplay(powerListId);

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
        getMenuInflater().inflate(R.menu.menu_power_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        //when delete was in the menu bar still
        /*if (id == R.id.deleteButton) {
            Log.d(TAG, "delete menu button clicked");
            showConfirmDeletionDialog();
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //adapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        myActionListener.pauseActivity();
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //adapter.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    // FROM POWER LIST CONTRACT INTERFACE

    @Override
    public void showPowerDetailsUI(String itemId, String itemName, String powerListId, View transitioningView) {
        if (transitioningView != null){
            Intent i = new Intent(PowerListActivity.this, PowerDetailsActivity.class);
            String transitionName = getString(R.string.transition_powerDetails_name);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            PowerListActivity.this,
                            transitioningView,
                            transitionName);
            Bundle bundle = options.toBundle();

            i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_ID, itemId);
            i.putExtra(PowerDetailsActivity.EXTRA_POWER_LIST_ID, powerListId);
            i.putExtra(PowerDetailsActivity.EXTRA_POWER_DETAIL_NAME, itemName);
            ActivityCompat.startActivity(PowerListActivity.this, i, bundle);
        }
        else
            this.openPowerDetailsActivity(itemId, powerListId);
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
    public void addSpellToList(Spell power) {
        //first hide the loading icon
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        Log.d(TAG, "addSpellToList: Got a spell to be added to adapter: " + power.getName());

        String powerGroupName = power.getGroupName();
        Log.d(TAG, "addSpellToList: power group name " + powerGroupName);
        //see if spellgroups already has this group
        if(!spellGroups.contains(new SpellGroup(powerGroupName, power))) {
            //add the power group, pass the power for SpellGroups in constructor
            spellGroups.add(new SpellGroup(powerGroupName, power));
            //notify adapter that new parent was added so it is added to list
            adapter.notifyParentItemInserted( spellGroups.size()-1 );
        }
        else {
            //get index of the group with a "dummy group", a needless object allocation here tbh
            int parentIndex = spellGroups.indexOf(new SpellGroup(powerGroupName, power));

            //power might already be in the list, check for that
            if(!spellGroups.get(parentIndex).containsSpell(power)) {
                //add the spell to the SpellGroup's list of powers
                spellGroups.get(parentIndex).addSpell(power);
                //notify adapter that new child was added so it can animate it in
                Log.d(TAG, "addSpellToList: parent index " + parentIndex);
                Log.d(TAG, "addSpellToList: parent list size: " + spellGroups.get(parentIndex).getListSize());
                adapter.notifyChildItemInserted(
                        parentIndex,
                        spellGroups.get(parentIndex).getListSize() - 1
                );
            }
        }
    }


    @Override
    public void removeSpellFromList(Spell power, boolean isLastPowerInGroup) {
        String powerGroupName = power.getGroupName();
        Log.d(TAG, "removeSpellFromList: removing power " + power.getName() + " group " + powerGroupName);
        int parentIndex = spellGroups.indexOf(new SpellGroup(powerGroupName, power));

        //remove the power
        if(spellGroups.get(parentIndex).containsSpell(power)) {
            int powerIndex = spellGroups.get(parentIndex).removeSpell(power);
            //notify adapter of item changes
            adapter.notifyChildItemRemoved(parentIndex, powerIndex);
        }
        //check if the group should be removed too
        if(isLastPowerInGroup){
            spellGroups.remove(power.getGroupName());
            adapter.notifyParentItemRemoved(parentIndex);
        }

        /*int spellGroupIndex;
        //make sure power has group name
        if (power.getGroupName() != null && !"".equals(power.getGroupName())) {
            Log.d(TAG, "power grouped");
            //sort of unnecessary object creation. Is there a better way?
            //get index of the power group the deletable power is part of
            spellGroupIndex = spellGroups.indexOf(new SpellGroup(power.getGroupName(), new Spell()));
        } else {
            //the power is not in a group named by user, so it is in un grouped "group"
            Log.d(TAG, "power not grouped");
            spellGroupIndex = spellGroups.indexOf(new SpellGroup(
                    getString(R.string.spell_group_not_grouped), power));
        }
        SpellGroup group = spellGroups.get(spellGroupIndex);

        Log.d(TAG, "power group index: " + spellGroupIndex + " group name: " + group.getGroupName());

        //SpellGroup.removeSpell returns the index of the child removed
        int removedChildIndex = group.removeSpell(power);
        Log.d(TAG, "removed child's index: " + removedChildIndex);
        adapter.notifyChildItemRemoved(spellGroupIndex, removedChildIndex);
        if (group.getListSize() == 0) {
            spellGroups.remove(group);
            adapter.notifyParentItemRemoved(spellGroupIndex);
        }*/
        //adapter = new PowerListRecyclerAdapter(this, spellGroups, myActionListener);
        //recyclerView.setAdapter(adapter);
    }


    @Override
    public void showPowerDeletedSnackBar() {
        Snackbar snackbar = Snackbar.make(
                findViewById(R.id.power_list_layout),
                getString(R.string.power_list_snackbar_info),
                Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myActionListener.userPushingUndo();
                    }
                });

        //to set the text colour
        int snackBarTextId = android.support.design.R.id.snackbar_text;
        TextView snackBarText = (TextView) snackbar.getView().findViewById(snackBarTextId);
        snackBarText.setTextColor(ContextCompat.getColor(this, R.color.myTextColorPrimary));

        snackbar.show();
    }

    void setPowerSelectionMode(boolean selectionMode){
        if(selectionMode)
            deleteButton.setVisibility(View.VISIBLE);
        else
            deleteButton.setVisibility(View.GONE);
    }

    /**
     * Show user a popup asking if she wants to delete selected powers
     * On yes will defer to Presenter to do the job
     */
    private void showConfirmDeletionDialog(){
        //give builder our custom dialog fragment style
        new AlertDialog.Builder(this, R.style.dialogFragment_title_style)
                .setMessage(getString(R.string.powerlist_confirmRemoval))
                .setTitle(getString(R.string.powerList_confirmRemoval_title))
                .setPositiveButton(getString(R.string.action_remove),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                myActionListener.userDeletingPowersFromList(
                                                PowerListActivity.this.adapter.getSelectedSpells());
                                deleteButton.setVisibility(View.GONE);
                                //tell adapter to switch selection mode off
                                PowerListActivity.this.adapter.endSelectionMode();
                            }
                        })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }


    private class RecyclerDividerDecorator extends RecyclerView.ItemDecoration {

        private final int[] attrs = new int[]{android.R.attr.listDivider};
        private Drawable divider;

        public RecyclerDividerDecorator(Context context) {
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
}