package tomi.piipposoft.blankspellbook.Drawer;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
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

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by Domu on 11-Apr-16.
 *
 * This activity uses MaterialDrawer library by mikePenz (https://github.com/mikepenz/MaterialDrawer)
 */
public class DrawerHelper implements
        DrawerContract.View {


    private AppCompatActivity callerActivity;
    private static String TAG = "DrawerHelper";

    public static final int POWER_LIST = 1;
    public static final int DAILY_POWER_LIST = 2;



    private static final long POWER_LISTS_PROFILE_IDENTIFIER = -5;
    private static final long DAILY_POWER_LISTS_PROFILE_IDENTIFIER = -2;
    private static final long ADD_POWER_LIST_FOOTER_IDENTIFIER = -3;
    private static final long ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER = -4;

    private Drawer mDrawer;


    private static DrawerListener mDrawerListener;

    /**
     * Implemented by an activity that handles the item clicks,
     * most likely ApplicationActivity
     */
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

        //Create the drawer itself
        populateDrawer(toolbar);

        final ProfileDrawerItem spellBooksProfile = new ProfileDrawerItem().withName("Spell Books")
                .withIcon(ResourcesCompat.getDrawable(
                        callerActivity.getResources(),
                        R.drawable.iqql_spellbook_billfold,
                        null))
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
        Log.d(TAG, "populateDrawer: drawer created: " + mDrawer.toString());
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
    public void addDrawerItem(IDrawerItem item, DrawerContract.WhichItem which){
        Log.d(TAG, "adding new drawer item with id: " + item.getTag());
        PrimaryDrawerItem primaryItem = (PrimaryDrawerItem) item;
        if(which == DrawerContract.WhichItem.PowerList)
            primaryItem.withOnDrawerItemClickListener(new PowerListDrawerItemListener());
        else if(which == DrawerContract.WhichItem.DailyPowerList)
            primaryItem.withOnDrawerItemClickListener(new DailyPowerListDrawerItemListener());
        mDrawer.addItem(primaryItem);
    }

    @Override
    public void removeDrawerItem(String itemId){

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
    }

    /**
     * Helper method to populate the drawer daily powers side
     * @param drawer
     */
    private void populateDailyPowersList(Drawer drawer){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
                .withName("Add new daily power list")
                .withIdentifier(ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER));
    }

    private class PowerListDrawerItemListener implements Drawer.OnDrawerItemClickListener{

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            //tell the current activity that an item has been clicked
            Log.d(TAG, "PowerListDrawerItemListener: power list tag (should be ID): " + drawerItem.getTag());
            mDrawerListener.powerListClicked(drawerItem);
            return true;
        }
    }

    private class DailyPowerListDrawerItemListener implements Drawer.OnDrawerItemClickListener{

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            //tell the current activity that an item has been clicked
            Log.d(TAG, "DailyPowerListDrawerItemListener: power list tag (should be ID): " + drawerItem.getTag());
            mDrawerListener.dailyPowerListClicked(drawerItem);
            return true;
        }
    }


}