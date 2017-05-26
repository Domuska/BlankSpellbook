package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 26.5.2017.
 */

public class SpellsFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    private final String TAG = "SpellsFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_main_activity_spells, container, false);
        Bundle args = getArguments();
        Log.d(TAG, "got args: " + args.getString("key"));
        ((TextView) rootView.findViewById(R.id.textfield)).setText("hei maailma!");
        return rootView;
    }
}
