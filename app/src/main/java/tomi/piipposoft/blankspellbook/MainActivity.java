package tomi.piipposoft.blankspellbook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import tomi.piipposoft.blankspellbook.Database.PowerContract;
import tomi.piipposoft.blankspellbook.Fragments.SetSpellbookNameDialog;

/**
 *
 *
 */

public class MainActivity extends AppCompatActivity {

    private Button spellBookButton, dailySpellsButton;
    private FloatingActionButton fab;
    private Activity thisActivity;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thisActivity = this;

        spellBookButton = (Button) findViewById(R.id.button_Spellbook);
        dailySpellsButton = (Button) findViewById(R.id.button_dailySpells);


        spellBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(thisActivity, SpellBookActivity.class);
                startActivity(i);
            }
        });

        //TODO add a navigation drawer to this activity to list power lists and daily power lists

        dailySpellsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(thisActivity, DailySpellsActivity.class);
                startActivity(i);
            }

        });


        //set the support library's toolbar as application toolbar

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        //handle the navigation drawer

        String[] textRows = { "Ithiel's spells", "Dromgar's abilities", "Owen's spells" };
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerListView = (ListView) findViewById(R.id.drawer_listView);

        mDrawerListView.setAdapter(new ArrayAdapter<String>(
                this, R.layout.drawer_list_item, textRows
        ));

        Button mAddSpellBookButton = (Button) findViewById(R.id.drawer_addSpellBookButton);
        mAddSpellBookButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                showSetSpellbookNameDialog();
            }
        });


        //add button to toolbar to open the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.open_drawer_info,
                R.string.close_drawer_info){

            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("Blank spellbook");

            }

            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle("Power lists");
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        toolbar.setNavigationIcon(R.mipmap.ic_menu_black_24dp);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }

        switch(id){


            case R.id.action_settings:
                return true;

            case R.id.action_about:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


    private void showSetSpellbookNameDialog(){

        DialogFragment dialog = new SetSpellbookNameDialog();
        dialog.show(getSupportFragmentManager(), "SetSpellBookDialogFragment");
    }


}
