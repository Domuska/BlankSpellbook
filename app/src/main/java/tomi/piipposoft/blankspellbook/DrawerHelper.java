package tomi.piipposoft.blankspellbook;

import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

/**
 * Created by Domu on 11-Apr-16.
 */
public class DrawerHelper {

    public static String createDrawer(Activity activity, Toolbar toolbar) {

        final Activity callerActivity = activity;
        final String TAG = "createDrawer, called by " + activity.getLocalClassName();

        //TODO: muutetaan nämä dynaamisesti luoduiksi tietokannasta
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Sepon Spellbook");
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withName("Askon Spellbook");
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withName("Sepon daily spellit");


        //Create the drawer itself
        //TODO: Notice, the position you get in onItemClick might not be correct, headers are apparently also items in it and might screw up things if you use positions

        //TODO: we can add a custom view as header of the drawer with .withHeader, could use this instead of monkeying around with accountHeader:
        //http://stackoverflow.com/questions/7838921/android-listview-addheaderview-nullpointerexception-for-predefined-views-defin/7839013#7839013
        //https://github.com/mikepenz/MaterialDrawer/issues/760
        final com.mikepenz.materialdrawer.Drawer drawer = new DrawerBuilder()
                .withActivity(callerActivity)
                .withToolbar(toolbar)
                //.withAccountHeader(drawerHeader)
                /*.addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2
                )*/
                .withOnDrawerItemClickListener(new com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Toast.makeText(callerActivity, "painoit nappulaa kohdassa " + position, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "painoit nappulaa kohdassa " + position);
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
                            drawer.addItem(item1);
                            drawer.addItem(item2);
                        } else if (profile.equals(dailySpellsProfile)) {
                            drawer.addItem(item3);
                        }
                        return true;
                    }
                })
                .build();

        return null;
    }
}
