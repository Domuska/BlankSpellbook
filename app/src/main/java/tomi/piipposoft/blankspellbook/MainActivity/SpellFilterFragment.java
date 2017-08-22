package tomi.piipposoft.blankspellbook.MainActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 13.7.2017.
 */

public class SpellFilterFragment extends Fragment {

    private final String TAG = "SpellFilterFragment";
    public static final String GROUP_NAMES_BUNDLE = "groupNames";
    public static final String GROUP_NAMES_SELECTED_BUNDLE = "selectedGroupNames";
    public static final String POWER_LIST_NAMES_BUNDLE = "powerLists";
    public static final String POWER_LIST_NAMES_SELECTED_BUNDLE = "selectedPowerListNames";

    private final int HEADER_VIEW = -1;

    //store names of the power lists and groups visible, boolean to tell if list is selected currently
    private List<AbstractMap.SimpleEntry<String, Boolean>> powerListNamesMap = new ArrayList<>();
    private List<AbstractMap.SimpleEntry<String, Boolean>> powerGroupNamesMap = new ArrayList<>();

    private RecyclerView.Adapter powerListsAdapter, groupsAdapter;

    private MainActivityContract.FilterFragmentUserActionListener mActionListener;

    interface FragmentExpandedInterface{
        void fragmentExpanded(float fragmentHeight);
    }

    SpellFilterFragment.FragmentExpandedInterface listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main_filters, container, false);
        if(getArguments() != null) {
            ArrayList<String> groupNames = getArguments().getStringArrayList(GROUP_NAMES_BUNDLE);
            ArrayList<String> selectedGroups = getArguments().getStringArrayList(GROUP_NAMES_SELECTED_BUNDLE);
            ArrayList<String> powerListNames = getArguments().getStringArrayList(POWER_LIST_NAMES_BUNDLE);
            ArrayList<String> selectedPowerLists = getArguments().getStringArrayList(POWER_LIST_NAMES_SELECTED_BUNDLE);
            if(groupNames != null && selectedGroups != null) {
                for (String name : groupNames) {
                    //groupNamesMap.put(name, false);
                    if(selectedGroups.contains(name))
                        powerGroupNamesMap.add(createMapEntry(name, true));
                    else
                        powerGroupNamesMap.add(createMapEntry(name, false));
                }
            }
            if (powerListNames != null && selectedPowerLists != null) {
                for(String name : powerListNames){
                    //powerListNamesMap.put(name, false);
                    if(selectedPowerLists.contains(name))
                        powerListNamesMap.add(createMapEntry(name, true));
                    else
                        powerListNamesMap.add(createMapEntry(name, false));
                }
            }
        }
        else
            Log.e(TAG, "Error in onCreateView, bundle is null");

        //recyclerview for displaying the power list filters
        final RecyclerView powerListRecyclerView = rootView.findViewById(R.id.powerListFilterRecyclerView);
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

        //disable nested scrolling so the list behind is not scrolled when this list is
        groupRecyclerView.setNestedScrollingEnabled(false);
        powerListRecyclerView.setNestedScrollingEnabled(false);

        //add a listener to the rootview, when all is inflated we need the fragment height, which is same
        //as the recyclerview height
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        //Remove the listener before proceeding
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        Log.d(TAG, "OnGlobalLayoutListener recycler height" + powerListRecyclerView.getHeight());
                        listener.fragmentExpanded(powerListRecyclerView.getHeight());
                    }
                });

        Log.d(TAG, "power list names size: " + powerListNamesMap.size());
        Log.d(TAG, "group names list size: " + powerGroupNamesMap.size());

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (SpellFilterFragment.FragmentExpandedInterface) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void attachActionListener(MainActivityContract.FilterFragmentUserActionListener listener){
        mActionListener = listener;
    }


    /**
     * Used for telling the fragment to update the list of group names visible in the list
     * @param groupNames a list of group names that should be displayed
     */
    public void setDisplayedGroupNames(TreeSet<String> groupNames){
        Log.d(TAG, "group names that should be displayed: " + groupNames);
        //remove the
        /*for(Iterator<AbstractMap.SimpleEntry<String, Boolean>> iterator
                            = powerGroupNamesMap.iterator(); iterator.hasNext();){
            AbstractMap.SimpleEntry<String, Boolean> entry = iterator.next();
            if(!groupNames.contains(entry.getKey()))
                iterator.remove();
        }*/
        powerGroupNamesMap = createNewList(powerGroupNamesMap, groupNames);
        groupsAdapter.notifyDataSetChanged();
    }

    /**
     * Used for setting which power lists should be visible in the list
     * @param powerListNames Names of the power lists that should be displayed
     */
    public void setDisplayedPowerListNames(TreeSet<String> powerListNames){
        Log.d(TAG, "power lists that should be displayed: " + powerListNames);
        powerListNamesMap = createNewList(powerListNamesMap, powerListNames);
        powerListsAdapter.notifyDataSetChanged();
    }


    private ArrayList<AbstractMap.SimpleEntry<String, Boolean>> createNewList(
            List<AbstractMap.SimpleEntry<String, Boolean>> originalList,
            TreeSet<String> newListEntries) {

        ArrayList<AbstractMap.SimpleEntry<String, Boolean>> temporaryList = new ArrayList<>();
        //go through the new names and add them to temporary list
        for(String name : newListEntries){
            AbstractMap.SimpleEntry<String, Boolean> entry = createMapEntry(name, true);
            //if the group names list has elements that have been selected, remember the selections
            if(originalList.contains(entry))
                temporaryList.add(entry);
            else {
                entry.setValue(false);
                temporaryList.add(entry);
            }
        }
        return temporaryList;
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
            RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private boolean isPowerListAdapter;

        private FilterListAdapter(boolean isPowerListAdapter){
            this.isPowerListAdapter = isPowerListAdapter;
        }

        class NormalViewHolder extends RecyclerView.ViewHolder{
            private TextView rowText;
            private View rowBackground;
            private NormalViewHolder(View view){
                super(view);
                rowText = view.findViewById(R.id.recycler_row_text);
                rowBackground = view;
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder{

            public HeaderViewHolder(View itemView) {
                super(itemView);
                TextView tx = itemView.findViewById(R.id.filter_list_header_textview);
                if(isPowerListAdapter)
                    tx.setText(getString(R.string.mainactivity_filter_power_list));
                else
                    tx.setText(getString(R.string.mainactivity_filter_group));
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            if(viewType == HEADER_VIEW){
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.filter_list_header, parent, false);
                return new HeaderViewHolder(view);
            }
            else {
                view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.filter_list_row, parent, false);
                return new NormalViewHolder(view);
            }

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder vh, final int position) {

            //don't do anything if the row is the first row (the header)
            if(position != 0) {
                final NormalViewHolder holder = (NormalViewHolder)vh;
                final String rowText;
                final int adapterPosition = holder.getAdapterPosition() -1;
                //adapter is for power list names
                if (isPowerListAdapter) {
                    rowText = powerListNamesMap.get(adapterPosition).getKey();
                    //holder recycles the rows, they might be incorrectly "selected"
                    holder.rowBackground.setSelected(powerListNamesMap.get(adapterPosition).getValue());

                }
                //adapter is for power group names
                else {
                    rowText = powerGroupNamesMap.get(adapterPosition).getKey();
                    //holder recycles the rows, they might be incorrectly "selected"
                    holder.rowBackground.setSelected(powerGroupNamesMap.get(adapterPosition).getValue());
                }
                holder.rowText.setText(rowText);

                //color every other row with darker background
                /*if (adapterPosition % 2 == 1) {
                    holder.rowBackground.setBackground(ContextCompat.getDrawable(
                            getActivity(),
                            R.drawable.filter_row_background_dark
                    ));
                }*/

                holder.rowBackground.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //row is already selected, de-select it
                        if (holder.rowBackground.isSelected()) {
                            holder.rowBackground.setSelected(false);
                            //set the selection boolean in the map of group/power list names
                            if (isPowerListAdapter) {
                                //powerListNamesMap.put(rowText, false);
                                powerListNamesMap.get(adapterPosition).setValue(false);
                                mActionListener.removeFilter(rowText, MainActivityPresenter.FILTER_BY_POWER_LIST_NAME);
                            } else {
                                //groupNamesMap.put(rowText, false);
                                powerGroupNamesMap.get(adapterPosition).setValue(false);
                                mActionListener.removeFilter(rowText, MainActivityPresenter.FILTER_BY_GROUP_NAME);
                            }
                        }
                        //set the row as selected
                        else {
                            holder.rowBackground.setSelected(true);
                            if (isPowerListAdapter) {
                                //powerListNamesMap.put(rowText, true);
                                powerListNamesMap.get(adapterPosition).setValue(true);
                                mActionListener.filterGroupsAndPowersWithPowerListName(rowText);
                            } else {
                                //groupNamesMap.put(rowText, true);
                                powerGroupNamesMap.get(adapterPosition).setValue(true);
                                mActionListener.filterPowerListsAndPowersWithGroupName(rowText);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            //account for the headers -> size +1
            if(isPowerListAdapter)
                return powerListNamesMap.size() + 1;
            else
                return powerGroupNamesMap.size() + 1;

        }

        @Override
        public int getItemViewType(int position) {
            //if we are looking at first item, it's the header
            if(position == 0)
                return HEADER_VIEW;
            else
                return super.getItemViewType(position);
        }
    }


}
