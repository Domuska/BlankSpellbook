package tomi.piipposoft.blankspellbook.Drawer;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.List;

import tomi.piipposoft.blankspellbook.dialog_fragments.SetDailyPowerListNameDialog;
import tomi.piipposoft.blankspellbook.dialog_fragments.SetPowerListNameDialog;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.PowerList.PowerListActivity;

/**
 * Created by Domu on 11-Apr-16.
 *
 * This activity uses MaterialDrawer library by mikePenz (https://github.com/mikepenz/MaterialDrawer)
 */
public class DrawerHelper implements
        DrawerContract.View {


    private static AppCompatActivity callerActivity;
    private static String TAG;

    private static final long POWER_LISTS_PROFILE_IDENTIFIER = -5;
    private static final long DAILY_POWER_LISTS_PROFILE_IDENTIFIER = -2;
    private static final long ADD_POWER_LIST_FOOTER_IDENTIFIER = -3;
    private static final long ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER = -4;

    private static Drawer mDrawer;

    private static DrawerHelper instance;

    private static DrawerListener mDrawerListener;

    public interface DrawerListener {
        void dailyPowerListProfileSelected();
        void powerListProfileSelected();
        void powerListClicked(IDrawerItem clickedItem);
        void dailyPowerListClicked(IDrawerItem clickedItem);

    }

    private DrawerHelper(Activity activity, Toolbar toolbar ){
        callerActivity = (AppCompatActivity) activity;
        mDrawerListener = (DrawerListener) activity;
        createDrawer(toolbar);
    }

    private DrawerHelper(){}

    public static DrawerHelper getInstance(Activity activity, Toolbar toolbar){

        Log.d(TAG, "drawer getInstance called");

        return new DrawerHelper(activity, toolbar);
//        if(instance == null){
//            Log.d(TAG, "instance null, creating new instance");
//            instance = new DrawerHelper(activity, toolbar);
//            return instance;
//        }
//        else {
//            Log.d(TAG, "instance not null, setting values and returning current instance");
//            instance = new DrawerHelper(activity, toolbar);
//            return instance;
//        }
    }

    private void createDrawer(Toolbar toolbar) {
        TAG = "createDrawer, called by " + callerActivity.getLocalClassName();

        //Create the drawer itself
        populateDrawer(toolbar);

        final ProfileDrawerItem spellBooksProfile = new ProfileDrawerItem().withName("Spell Books")
                .withIcon(callerActivity.getResources().getDrawable(R.drawable.iqql_spellbook_billfold))
                .withIdentifier(POWER_LISTS_PROFILE_IDENTIFIER);
        final ProfileDrawerItem dailySpellsProfile = new ProfileDrawerItem().withName("Daily Power Lists")
                .withIdentifier(DAILY_POWER_LISTS_PROFILE_IDENTIFIER);


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

                        if (profile.getIdentifier() == POWER_LISTS_PROFILE_IDENTIFIER) {
//                            populateSpellBooksList(mDrawer);
                            mDrawerListener.powerListProfileSelected();


                        } else if (profile.getIdentifier() == DAILY_POWER_LISTS_PROFILE_IDENTIFIER) {
//                            populateDailyPowersList(mDrawer);
                            mDrawerListener.dailyPowerListProfileSelected();
                        }
                        return true;
                    }
                })
                .build();

    }

    public DrawerLayout getDrawerLayout(){
        return mDrawer.getDrawerLayout();
    }


    private void populateDrawer(Toolbar toolbar){
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
                                DialogFragment dialog = new SetPowerListNameDialog();
                                dialog.show(callerActivity.getSupportFragmentManager(), "SetSpellBookNameDialogFragment");
                            } catch(IllegalStateException e){
                                /* do nothing, apparently there is no way to avoid this error after
                                    saveInstanceState has been performed,see:
                                    http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit */
                                Log.d(TAG, "error while opening SetSpellbookNameDialog");
                                e.printStackTrace();
                            }
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
                        }
                        return false;
                    }
                })
                .withCloseOnClick(false)
                .build();
    }



    public void closeDrawer(){
        mDrawer.closeDrawer();
    }

    /**
     * From drawerContract
     */
    @Override
    public void showDailyPowerList() {
        populateDailyPowersList(mDrawer);
    }

    /**
     * From drawerContract
     */
    @Override
    public void showPowerList() {
        populateSpellBooksList(mDrawer);
    }

    @Override
    public void addDrawerItem(IDrawerItem item){
        PrimaryDrawerItem primaryItem = (PrimaryDrawerItem) item;
        primaryItem.withOnDrawerItemClickListener(new SpellDrawerItemListener());
        mDrawer.addItem(primaryItem);
    }

    @Override
    public void removeDrawerItem(String itemId){

    }

    @Override
    public void lockDrawer() {
        //this doesn't seem to work for some reason
        mDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void unlockDrawer() {
        //this doesnt seem to work for some reason
        mDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    // TODO: 11.5.2017 this method should not stay here. This is just for testing if we could lock the nav drawer
    public static void lockDrawerAndChangeIcon(){
        //this doesn't seem to work. If you do this, the toolbar button will change,
        //but the drawer isn't locked nor is the on click event changed. Icon changes though!
        //http://stackoverflow.com/questions/33479575/changing-navigation-drawer-hamburger-icon

        mDrawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        mDrawer.getActionBarDrawerToggle().setHomeAsUpIndicator(R.drawable.ic_done_black_24dp);

        mDrawer.getActionBarDrawerToggle().setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "now we should be cancelling editing!");
            }
        });

        mDrawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * Helper method to populate the drawer spell books side
     *
     * @param drawer
     */
    private void populateSpellBooksList(Drawer drawer){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
                .withName("Add new spell book")
                .withIdentifier(ADD_POWER_LIST_FOOTER_IDENTIFIER));

        /*
        Log.d(TAG, "numer of drawer items being added: " + drawerItems.size());
        for (int i = 0; i < drawerItems.size(); i++) {
            final PrimaryDrawerItem item = (PrimaryDrawerItem)drawerItems.get(i);
            Log.d(TAG, "new item being added to drawer: " + item.getName() +
                    " ID: " + item.getTag());
            //add listener to the drawer items
            item.withOnDrawerItemClickListener(new SpellDrawerItemListener());
            drawer.addItem(drawerItems.get(i));
        }
        */
    }

    private class SpellDrawerItemListener implements Drawer.OnDrawerItemClickListener{

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            //tell the current activity that an item has been clicked
            mDrawerListener.powerListClicked(drawerItem);
            return true;
        }
    }

    /**
     * Helper method to populate the drawer daily powers side
     *
     * @param drawer
     */
    private void populateDailyPowersList(Drawer drawer){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
            .withName("Add new daily power list")
            .withIdentifier(ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER));

        /*
        for(int i = 0; i < drawerItems.size(); i++){
            final PrimaryDrawerItem item = (PrimaryDrawerItem)drawerItems.get(i);
            Log.d(TAG, "new item being added to drawer: " + item.getName() +
                    " ID: " + item.getTag());
            //add listener to the drawer items
            item.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    //tell the current activity that an item has been clicked
                    mDrawerListener.dailyPowerListClicked(drawerItem);
                    return true;
                }
            });

            drawer.addItem(drawerItems.get(i));
        }
        */
    }
}