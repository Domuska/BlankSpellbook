package tomi.piipposoft.blankspellbook.MainActivity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Helper;

/**
 * Fragment used in MainActivity to show either Power Lists or Daily Power lists
 *
 * Fragment has a RecyclerView with a StaggeredGridLayoutManager that shows the
 * lists
 */

public class RecyclerListFragment extends Fragment {

    public static final String TAG = "RecyclerListFragment";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;

    //use arrayLists instead of map to make it easier to populate text views in adapter
    private ArrayList<String> listNames = new ArrayList<>();
    private ArrayList<String> listIds = new ArrayList<>();

    //map that has pairs: ID - list of power names this list has
    private ArrayMap<String, ArrayList<String>> listPowerNames = new ArrayMap<>();

    MainActivityContract.FragmentUserActionListener myClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View rootView = inflater.inflate(
                R.layout.fragment_main_recycler_list, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_activity_recycler_fragment_recyclerView);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        adapter = new FragmentListAdapter();
        recyclerView.setAdapter(adapter);

        //set staggered grid layout manager to be more funky
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    /**
     * Take in a new (daily) power list
     * @param name Name of the list
     * @param id ID of the list
     * @param powerNames Names of the powers under this list
     */
    public void handleNewListItem(String name, String id, ArrayList<String> powerNames){
        listNames.add(name);
        listIds.add(id);
        //only add the group to the map if there are groups under the spell list
        if(powerNames.size() > 0)
            listPowerNames.put(id, powerNames);
        //notify adapter that new item inserted
        adapter.notifyItemInserted(listNames.size()-1);
    }

    public void handleNewListItem(String listName, String id) {
        Log.d(TAG, "handleNewListItem: new item: " + listName);
        listNames.add(listName);
        listIds.add(id);
        adapter.notifyItemInserted(listNames.size()-1);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Add name of a power related to a list
     * @param powerName name of the power
     * @param powerListId ID of the power list this name is related to
     */
    public void handleNewPowerName(@NonNull String powerName, @NonNull String powerListId) {
        Log.d(TAG, "handleNewPowerName: new name got " + powerName + " for list " + powerListId);
        if(listPowerNames.containsKey(powerListId)){
            //add the name to the list in map
            listPowerNames.get(powerListId).add(powerName);
        }
        else{
            //add the list to the map
            ArrayList<String> list = new ArrayList<>();
            list.add(powerName);
            listPowerNames.put(powerListId, list);
        }
        //notify adapter to update the card
        adapter.notifyItemChanged(listIds.indexOf(powerListId));
    }


    public void removeListItem(String powerListName, String id) {
        //save the index so we can notify adapter
        int nameIndex = listNames.indexOf(powerListName);
        listNames.remove(powerListName);
        listIds.remove(id);
        // TODO: 9.6.2017 remove also from map the entry
        adapter.notifyItemRemoved(nameIndex);
    }

    public void removeAllLists() {
        listNames = new ArrayList<>();
        listIds = new ArrayList<>();
    }

    public void attachClickListener(MainActivityContract.FragmentUserActionListener listener) {
        this.myClickListener = listener;
    }




    /**
     * Adapter for the RecyclerView showing the power lists
     */
    class FragmentListAdapter extends RecyclerView.Adapter<FragmentListAdapter.ViewHolder> {


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textViewPrimary, textViewSecondary, textViewTertiary;
            View splotchView;
            CardView cardView;

            ViewHolder(View  v) {
                super(v);
                textViewPrimary = (TextView) v.findViewById(R.id.powerListName_textView);
                textViewSecondary = (TextView) v.findViewById(R.id.groupName1_textView);
                textViewTertiary = (TextView) v.findViewById(R.id.groupName2_textView);
                splotchView = v.findViewById(R.id.splotchView);
                cardView = (CardView) v.findViewById(R.id.cardView);
            }
        }

        FragmentListAdapter() {
            //empty constructor, no local variables
        }

        // Create new views (invoked by the layout manager)
        @Override
        public FragmentListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_activity_recycler_list_child_row, parent, false);
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String groupName = listNames.get(position);
            holder.textViewPrimary.setText(groupName);
            String id = listIds.get(position);

            final int itemPosition = holder.getAdapterPosition();

            //the map might not have entry with this ID, that means there's no groups under the spell list
            if (listPowerNames.containsKey(id)) {
                //add the group name to the first text view
                String grpName1 = listPowerNames.get(id).get(0);
                if(!"".equals(grpName1))
                    holder.textViewSecondary.setText(grpName1);

                //if the list has also second group, add second one too
                if(listPowerNames.get(id).size() > 1) {
                    String grpName2 = listPowerNames.get(id).get(1);
                    if (!"".equals(grpName2))
                        holder.textViewTertiary.setText(grpName2);
                }
            }
            else{
                holder.textViewSecondary.setVisibility(View.INVISIBLE);
                holder.textViewTertiary.setVisibility(View.INVISIBLE);
            }

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myClickListener.onPowerListClicked(
                            listNames.get(itemPosition),
                            listIds.get(itemPosition));
                }
            });

            //get the drawable and give it a random color
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.recycler_child_rectangle);
            drawable.setColorFilter(Helper.getRandomColorFromString(groupName), PorterDuff.Mode.SRC_IN);

            //set background for the splotch, seems like this really has to be done like this
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.splotchView.setBackground(drawable);
            }
            else {
                //we can call this since only on ancient devices we get here, those still have this method
                holder.splotchView.setBackgroundDrawable(drawable);
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return listNames.size();
        }
    }


}
