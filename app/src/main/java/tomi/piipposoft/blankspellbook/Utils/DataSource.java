package tomi.piipposoft.blankspellbook.Utils;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import tomi.piipposoft.blankspellbook.Database.BlankSpellBookContract;

/**
 * Created by Domu on 17-Apr-16.
 */
public class DataSource {

    public static BlankSpellBookContract.DBHelper getDatasource(Activity activity){
        return new BlankSpellBookContract.DBHelper(activity.getApplicationContext());
    }

}
