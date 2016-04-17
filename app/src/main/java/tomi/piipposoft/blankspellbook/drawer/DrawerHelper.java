package tomi.piipposoft.blankspellbook.drawer;

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
import tomi.piipposoft.blankspellbook.powerlist.PowerListActivity;

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



    private DrawerListener mDrawerListener;

    public interface DrawerListener {
        void dailyPowerListProfileSelected();
        void powerListProfileSelected();
    }

    public DrawerHelper(Activity activity, Toolbar toolbar ){
        createDrawer(activity, toolbar);
        mDrawerListener = (DrawerListener) activity;

    }

    public void createDrawer(Activity activity, Toolbar toolbar) {

        callerActivity = (AppCompatActivity) activity;
        TAG = "createDrawer, called by " + activity.getLocalClassName();


        //Create the drawer itself
        createDrawer(toolbar);

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


    private void createDrawer(Toolbar toolbar){
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

    /**
     * From drawerContract
     */
    @Override
    public void showPowerListItems() {

    }

    /**
     * From drawerContract
     */
    @Override
    public void showDailyPowerListItems() {

    }

    /**
     * From drawerContract
     */
    @Override
    public void showDailyPowerList(List<IDrawerItem> drawerItems) {
        populateDailyPowersList(mDrawer, drawerItems);
    }

    /**
     * From drawerContract
     */
    @Override
    public void showPowerList(List<IDrawerItem> drawerItems) {
        Log.d(TAG, "showpowerlist called");
        populateSpellBooksList(mDrawer, drawerItems);
    }

    /**
     * Helper method to populate the drawer spell books side
     *
     * @param drawer
     */
    private void populateSpellBooksList(Drawer drawer, List<IDrawerItem> drawerItems){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
                .withName("Add new spell book")
                .withIdentifier(ADD_POWER_LIST_FOOTER_IDENTIFIER));


        for (int i = 0; i < drawerItems.size(); i++) {
            final PrimaryDrawerItem item = (PrimaryDrawerItem)drawerItems.get(i);
            item.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    Intent i = new Intent(callerActivity, PowerListActivity.class);
                    Log.d(TAG, "PowerListActivity launching, supplying extra ID: " + item.getIdentifier()
                            + " item name: " + item.getName());
                    i.putExtra(PowerListActivity.EXTRA_POWER_BOOK_ID, item.getIdentifier());
                    mDrawer.closeDrawer();
                    callerActivity.startActivity(i);
                    return true;
                }
            });

            drawer.addItem(drawerItems.get(i));
        }
    }

    /**
     * Helper method to populate the drawer daily powers side
     *
     * @param drawer
     */
    private void populateDailyPowersList(Drawer drawer, List<IDrawerItem> drawerItems){

        drawer.removeAllItems();
        drawer.removeAllStickyFooterItems();

        drawer.addStickyFooterItem(new PrimaryDrawerItem()
            .withName("Add new daily power list")
            .withIdentifier(ADD_DAILY_POWER_LIST_FOOTER_IDENTIFIER));

        for(int i = 0; i < drawerItems.size(); i++){

            final PrimaryDrawerItem item = (PrimaryDrawerItem)drawerItems.get(i);
            item.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    // TODO: 17-Apr-16 Handle moving to DailyPowerListActivity
                    Log.d(TAG, "dailyPowerListActivity launching, supplying extra ID: " + item.getIdentifier()
                    + " item name: " + item.getName());
                    return true;
                }
            });

            drawer.addItem(drawerItems.get(i));
        }
    }


}