package tomi.piipposoft.blankspellbook.dailypowerlist;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import tomi.piipposoft.blankspellbook.dialog_fragments.SetSpellbookNameDialog;
import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.drawer.DrawerHelper;

public class DailySpellsActivity extends AppCompatActivity
        implements SetSpellbookNameDialog.NoticeDialogListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_spells);


        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_daily_spells, menu);
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
    protected void onResume() {
        super.onResume();
//        DrawerHelper.createDrawer(this, (Toolbar)findViewById(R.id.my_toolbar));
    }

    // The method that is called when positive button on SetSpellbookNameDialog is clicked
    @Override
    public void onSetSpellBookNameDialogPositiveClick(DialogFragment dialog, String powerListName) {
//        DrawerHelper.updateSpellBookList();
    }
}
