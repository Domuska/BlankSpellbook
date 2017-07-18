package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 13.7.2017.
 */

public class SpellFilterFragment extends Fragment {

    private final String TAG = "SpellFilterFragment";
    public static final String GROUP_NAMES_BUNDLE = "groupNames";
    public static final String POWER_LIST_NAMES_BUNDLE = "powerLists";

    //master lists of what groups and power lists in total are in db
    private ArrayMap<String, Boolean> powerListNamesMap = new ArrayMap<>();
    private ArrayMap<String, Boolean> groupNamesMap = new ArrayMap<>();

    //
    private List<AbstractMap.SimpleEntry<String, Boolean>> powerListNamesMap2 = new ArrayList<>();
    private List<AbstractMap.SimpleEntry<String, Boolean>> powerGroupNamesMap2 = new ArrayList<>();

    //lists for displaying the items that should be actually be visible in the list
    private ArrayList<String> displayedPowerListNames, displayedGroupNames;

    private RecyclerView.Adapter powerListsAdapter, groupsAdapter;

    private MainActivityContract.FilterFragmentUserActionListener mActionListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_filters, container, false);
        if(getArguments() != null) {
            displayedGroupNames = getArguments().getStringArrayList(GROUP_NAMES_BUNDLE);
            displayedPowerListNames = getArguments().getStringArrayList(POWER_LIST_NAMES_BUNDLE);
            if(displayedGroupNames != null) {
                for (String name : displayedGroupNames) {
                    //groupNamesMap.put(name, false);
                    powerGroupNamesMap2.add(createMapEntry(name, false));
                }
            }
            if (displayedPowerListNames != null) {
                for(String name : displayedPowerListNames){
                    powerListNamesMap.put(name, false);
                    powerListNamesMap2.add(createMapEntry(name, false));
                }
            }
        }
        else
            Log.e(TAG, "Error in onCreateView, bundle is null");

        //recyclerview for displaying the power list filters
        RecyclerView powerListRecyclerView = rootView.findViewById(R.id.powerListFilterRecyclerView);
        LinearLayoutManager powerListLayoutManager = new LinearLayoutManager(getActivity());
        powerListRecyclerView.setLayoutManager(powerListLayoutManager);
        powerListsAdapter = new FilterListAdapter(true);
        powerListRecyclerView.setAdapter(powerListsAdapter);

        //recyclerview for displaying the group filters
        RecyclerView groupRecyclerView = rootView.findViewById(R.id.groupFilterRecyclerView);
        LinearLayoutManager groupLinearLayoutManager = new LinearLayoutManager(getActivity());
        groupRecyclerView.setLayoutManager(groupLinearLayoutManager);
        groupsAdapter = new FilterListAdapter(false);
        groupRecyclerView.setAdapter(groupsAdapter);

        return rootView;
    }

    public void attachActionListener(MainActivityContract.FilterFragmentUserActionListener listener){
        mActionListener = listener;
    }


    /**
     * Used for telling the fragment to update the list of group names visible in the list
     * @param groupNames a list of group names that should be displayed
     */
    public void setDisplayedGroupNames(ArrayList<String> groupNames){
        Log.d(TAG, "group names that should be displayed: " + groupNames);
        //remove the
        /*for(Iterator<AbstractMap.SimpleEntry<String, Boolean>> iterator
                            = powerGroupNamesMap2.iterator(); iterator.hasNext();){
            AbstractMap.SimpleEntry<String, Boolean> entry = iterator.next();
            if(!groupNames.contains(entry.getKey()))
                iterator.remove();
        }*/
        ArrayList<AbstractMap.SimpleEntry<String, Boolean>> temporaryList = new ArrayList<>();
        //go through the new names and add them to temporary list
        for(String name : groupNames){
            AbstractMap.SimpleEntry<String, Boolean> entry = createMapEntry(name, true);
            //if the group names list has elements that have been selected, remember the selections
            if(powerGroupNamesMap2.contains(entry))
                temporaryList.add(entry);
            else {
                entry.setValue(false);
                temporaryList.add(entry);
            }
        }
        powerGroupNamesMap2 = temporaryList;
        groupsAdapter.notifyDataSetChanged();
    }

    public void setDisplayedPowerListNames(ArrayList<String> powerListNames){
        // TODO: 17.7.2017 do the same as above
    }

    //utility method for creating new entries for the group names and list names arrays
    private AbstractMap.SimpleEntry<String, Boolean> createMapEntry(String left, Boolean right){
        return new AbstractMap.SimpleEntry<>(left, right);
    }

    //https://developer.android.com/guide/components/fragments.html

    /**
     * Adapter for the power lists and groups RecyclerViews
     * The constructor is passed a boolean to indicate whether this is a power lists adapter (true)
     * or a groups adapter (false). The adapters are so similar that there is no point
     * to make two separate, at least for now.
     */
    private class FilterListAdapter extends
            RecyclerView.Adapter<FilterListAdapter.ViewHolder>{

        private boolean isPowerListAdapter;

        private FilterListAdapter(boolean isPowerListAdapter){
            this.isPowerListAdapter = isPowerListAdapter;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView rowText;
            private View rowBackground;
            private ViewHolder(View view){
                super(view);
                rowText = view.findViewById(R.id.recycler_row_text);
                rowBackground = view;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.filter_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final String rowText;
            final int adapterPosition = holder.getAdapterPosition();
            //adapter is for power list names
            if(isPowerListAdapter) {
                rowText = displayedPowerListNames.get(adapterPosition);
                //holder recycles the rows, they might be incorrectly "selected"
                holder.rowBackground.setSelected(powerListNamesMap.get(rowText));
            }
            //adapter is for power group names
            else {
                Log.d(TAG, "row: " + adapterPosition);
                rowText = powerGroupNamesMap2.get(adapterPosition).getKey();
                //holder recycles the rows, they might be incorrectly "selected"
                holder.rowBackground.setSelected(powerGroupNamesMap2.get(adapterPosition).getValue());
            }
            holder.rowText.setText(rowText);

            //color every other row with darker background
            if(adapterPosition % 2 == 1) {
                    holder.rowBackground.setBackground(ContextCompat.getDrawable(
                            getActivity(),
                            R.drawable.filter_row_background_dark
                    ));
            }

            holder.rowBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //row is already selected, de-select it
                    if(holder.rowBackground.isSelected()) {
                        holder.rowBackground.setSelected(false);
                        //set the selection boolean in the map of group/power list names
                        if(isPowerListAdapter) {
                            powerListNamesMap.put(rowText, false);
                            //// TODO: 14.7.2017 remove the filtering
                        }
                        else {
                            //groupNamesMap.put(rowText, false);
                            powerGroupNamesMap2.get(adapterPosition).setValue(false);
                            //// TODO: 14.7.2017 remove the filtering
                        }
                    }
                    //set the row as selected
                    else{
                        holder.rowBackground.setSelected(true);
                        if(isPowerListAdapter) {
                            powerListNamesMap.put(rowText, true);
                            mActionListener.filterGroupsAndPowersWithPowerListName(rowText);
                        }
                        else {
                            //groupNamesMap.put(rowText, true);
                            powerGroupNamesMap2.get(adapterPosition).setValue(true);
                            mActionListener.filterPowerListsAndPowersWithGroupName(rowText);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            if(isPowerListAdapter)
                return powerListNamesMap.size();
            else
                return powerGroupNamesMap2.size();
        }
    }
}
