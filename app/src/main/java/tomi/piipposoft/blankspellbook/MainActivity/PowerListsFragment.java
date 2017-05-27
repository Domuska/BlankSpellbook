package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 27.5.2017.
 */

public class PowerListsFragment extends Fragment {

    public static final String TAG = "PowerListsFragment";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    //use arrayLists instead of map to make it easier to populate text views in adapter
    private ArrayList<String> listNames = new ArrayList<>();
    private ArrayList<String> listIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_main_activity_power_lists, container, false);
        Bundle args = getArguments();
        Log.d(TAG, "got args: " + args.getString("key"));


        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_activity_power_lists_recyclerView);
        adapter = new PowerListsAdapter();
        recyclerView.setAdapter(adapter);

        layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        //((TextView) rootView.findViewById(R.id.textfield)).setText("hei maailma!");
        return rootView;
    }

    public void handleNewPowerList(String name, String id){
        listNames.add(name);
        listIds.add(id);
        //notify adapter that new item inserted
        adapter.notifyItemInserted(listNames.size()-1);
    }

    /**
     * Adapter for the RecyclerView showing the power lists
     */
    class PowerListsAdapter extends RecyclerView.Adapter<PowerListsAdapter.ViewHolder> {


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            TextView textViewPrimary, textViewSecondary, textViewTertiary;

            ViewHolder(View  v) {
                super(v);
                textViewPrimary = (TextView) v.findViewById(R.id.textPrimary);
                textViewSecondary = (TextView) v.findViewById(R.id.textSecondaryBlack);
                textViewTertiary = (TextView) v.findViewById(R.id.textSecondaryGray);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        PowerListsAdapter() {
            //empty constructor, no local variables
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PowerListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_activity_power_lists_child_row, parent, false);
            // set the view's size, margins, paddings and layout parameters

            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textViewPrimary.setText(listNames.get(position));
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return listNames.size();
        }
    }
}
