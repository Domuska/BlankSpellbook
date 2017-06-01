package tomi.piipposoft.blankspellbook.MainActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

    //map that has pairs: ID - list of groups this list has
    private ArrayMap<String, ArrayList<String>> listPowerGroups = new ArrayMap<>();

    View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        this.rootView = inflater.inflate(
                R.layout.fragment_main_activity_power_lists, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_activity_power_lists_recyclerView);
        adapter = new PowerListsAdapter();
        recyclerView.setAdapter(adapter);

        //set staggered grid layout manager to be more funky
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        adapter.notifyItemInserted(0);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    public void handleNewPowerList(String name, String id, ArrayList<String> groupNames){
        listNames.add(name);
        listIds.add(id);
        //only add the group to the map if there are groups under the spell list
        if(groupNames.size() > 0)
            listPowerGroups.put(id, groupNames);
        //notify adapter that new item inserted
        adapter.notifyItemInserted(listNames.size()-1);
    }

    public void removePowerList(String powerListName, String id) {
        //save the index so we can notify adapter
        int nameIndex = listNames.indexOf(powerListName);
        listNames.remove(powerListName);
        listIds.remove(id);
        adapter.notifyItemRemoved(nameIndex);
    }

    public void removeAllLists() {
        listNames = new ArrayList<>();
        listIds = new ArrayList<>();
    }

    /**
     * Adapter for the RecyclerView showing the power lists
     */
    class PowerListsAdapter extends RecyclerView.Adapter<PowerListsAdapter.ViewHolder> {


        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textViewPrimary, textViewSecondary, textViewTertiary;
            View splotchView;

            ViewHolder(View  v) {
                super(v);
                textViewPrimary = (TextView) v.findViewById(R.id.powerListName_textView);
                textViewSecondary = (TextView) v.findViewById(R.id.groupName1_textView);
                textViewTertiary = (TextView) v.findViewById(R.id.groupName2_textView);
                splotchView = v.findViewById(R.id.splotchView);
            }
        }

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
            return new ViewHolder(v);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            String groupName = listNames.get(position);
            holder.textViewPrimary.setText(groupName);
            String id = listIds.get(position);

            //the map might not have entry with this ID, that means there's no groups under the spell list
            if (listPowerGroups.containsKey(id)) {
                //add the group name to the first text view
                String grpName1 = listPowerGroups.get(id).get(0);
                if(!"".equals(grpName1))
                    holder.textViewSecondary.setText(grpName1);

                //if the list has also second group, add second one too
                if(listPowerGroups.get(id).size() > 1) {
                    String grpName2 = listPowerGroups.get(id).get(1);
                    if (!"".equals(grpName2))
                        holder.textViewTertiary.setText(grpName2);
                }
            }
            else{
                holder.textViewSecondary.setVisibility(View.INVISIBLE);
                holder.textViewTertiary.setVisibility(View.INVISIBLE);
            }

            //get the drawable and give it a random color
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.recycler_child_rectangle);
            drawable.setColorFilter(getRandomColorFromString(groupName), PorterDuff.Mode.SRC_IN);

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

    private int getRandomColorFromString(String name){

        String color = String.format("#%X", name.hashCode());
        Log.d(TAG, "name in hex:" + color);
        int red = Integer.valueOf( color.substring( 1, 3 ), 16 );
        int green = Integer.valueOf( color.substring( 3, 5 ), 16 );
        int blue = Integer.valueOf( color.substring( 5, 7 ), 16 );
        Log.d(TAG, "red: " + red + " green:" + green +  " blue: " + blue);

        //tint the random color with white to get pastel colors
        red = (red + 255) / 2;
        green = (green + 255) / 2;
        blue = (blue + 255) / 2;

        return Color.rgb(red, green, blue);


        //https://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
        //generate a random color
        //should we define ranges to avoid certain colours?
        /*Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);*/

        //use some color to mix the random color to get a tint, here lightBlue is used
        /*red = (red + 173) / 2;
        green = (green + 216) / 2;
        blue = (blue + 230) / 2;*/

        //orangeRed
        /*red = (red + 255) / 2;
        green = (green + 69) / 2;
        blue = (blue + 0) / 2;*/

        //light orange
        /*red = (red + 255) / 2;
        green = (green + 106) / 2;
        blue = (blue + 50) / 2;*/

        //green
        /*red = (red + 153) / 2;
        green = (green + 204) / 2;
        blue = (blue + 153) / 2;*/
    }
}
