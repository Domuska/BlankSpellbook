package tomi.piipposoft.blankspellbook.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 8.6.2017.
 */

public class SharedPreferencesHandler {

    public static final String PREFERENCES = "preferences";
    public static final String DATABASE_PERSISTANCE = "databasePersistance";


    public static boolean isDatabasePersistanceSet(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREFERENCES, 0);
        return prefs.getBoolean(DATABASE_PERSISTANCE, false);
    }

    public static void setDatabasePersistance(boolean persistance, Context c){
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putBoolean(DATABASE_PERSISTANCE, persistance);
        //use commit rather than apply, app could crash if user quickly opens next activity
        //and prefs have not been written yet.
        //change back to commit if crashes occur!
        editor.apply();
    }

    /**
     * For setting if spell filter should work by cross-section or join
     * @param type true for cross-section, false for join
     * @param c context for shared preferences
     */
    /*public static void setFilterSpellByCrossSection(boolean type, Context c){
        SharedPreferences.Editor editor = c.getSharedPreferences(PREFERENCES, 0).edit();
        editor.putBoolean(FILTER_BY_CROSS_SECTION, type);
        editor.apply();
    }*/

    /**
     *
     * @param c context for shared preferences
     * @return true if spells should be filtered by cross-section, false if filtering by join. Default is true.
     */
    public static boolean getFilterSPellByCrossSection(Context c){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(c.getString(R.string.preferences_filter_by_cross_section), true);
    }
}
