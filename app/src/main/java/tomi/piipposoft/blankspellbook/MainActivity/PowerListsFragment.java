package tomi.piipposoft.blankspellbook.MainActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

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

    public String[] mColors = {
            "FFEBEE", "FFCDD2", "EF9A9A", "E57373", "EF5350", "F44336", "E53935",        //reds
            "D32F2F", "C62828", "B71C1C", "FF8A80", "FF5252", "FF1744", "D50000",
            "FCE4EC", "F8BBD0", "F48FB1", "F06292", "EC407A", "E91E63", "D81B60",        //pinks
            "C2185B", "AD1457", "880E4F", "FF80AB", "FF4081", "F50057", "C51162",
            "F3E5F5", "E1BEE7", "CE93D8", "BA68C8", "AB47BC", "9C27B0", "8E24AA",        //purples
            "7B1FA2", "6A1B9A", "4A148C", "EA80FC", "E040FB", "D500F9", "AA00FF",
            "EDE7F6", "D1C4E9", "B39DDB", "9575CD", "7E57C2", "673AB7", "5E35B1",        //deep purples
            "512DA8", "4527A0", "311B92", "B388FF", "7C4DFF", "651FFF", "6200EA",
            "E8EAF6", "C5CAE9", "9FA8DA", "7986CB", "5C6BC0", "3F51B5", "3949AB",        //indigo
            "303F9F", "283593", "1A237E", "8C9EFF", "536DFE", "3D5AFE", "304FFE",
            "E3F2FD", "BBDEFB", "90CAF9", "64B5F6", "42A5F5", "2196F3", "1E88E5",        //blue
            "1976D2", "1565C0", "0D47A1", "82B1FF", "448AFF", "2979FF", "2962FF",
            "E1F5FE", "B3E5FC", "81D4fA", "4fC3F7", "29B6FC", "03A9F4", "039BE5",        //light blue
            "0288D1", "0277BD", "01579B", "80D8FF", "40C4FF", "00B0FF", "0091EA",
            "E0F7FA", "B2EBF2", "80DEEA", "4DD0E1", "26C6DA", "00BCD4", "00ACC1",        //cyan
            "0097A7", "00838F", "006064", "84FFFF", "18FFFF", "00E5FF", "00B8D4",
            "E0F2F1", "B2DFDB", "80CBC4", "4DB6AC", "26A69A", "009688", "00897B",        //teal
            "00796B", "00695C", "004D40", "A7FFEB", "64FFDA", "1DE9B6", "00BFA5",
            "E8F5E9", "C8E6C9", "A5D6A7", "81C784", "66BB6A", "4CAF50", "43A047",        //green
            "388E3C", "2E7D32", "1B5E20", "B9F6CA", "69F0AE", "00E676", "00C853",
            "F1F8E9", "DCEDC8", "C5E1A5", "AED581", "9CCC65", "8BC34A", "7CB342",        //light green
            "689F38", "558B2F", "33691E", "CCFF90", "B2FF59", "76FF03", "64DD17",
            "F9FBE7", "F0F4C3", "E6EE9C", "DCE775", "D4E157", "CDDC39", "C0CA33",        //lime
            "A4B42B", "9E9D24", "827717", "F4FF81", "EEFF41", "C6FF00", "AEEA00",
            "FFFDE7", "FFF9C4", "FFF590", "FFF176", "FFEE58", "FFEB3B", "FDD835",        //yellow
            "FBC02D", "F9A825", "F57F17", "FFFF82", "FFFF00", "FFEA00", "FFD600",
            "FFF8E1", "FFECB3", "FFE082", "FFD54F", "FFCA28", "FFC107", "FFB300",        //amber
            "FFA000", "FF8F00", "FF6F00", "FFE57F", "FFD740", "FFC400", "FFAB00",
            "FFF3E0", "FFE0B2", "FFCC80", "FFB74D", "FFA726", "FF9800", "FB8C00",        //orange
            "F57C00", "EF6C00", "E65100", "FFD180", "FFAB40", "FF9100", "FF6D00",
            "FBE9A7", "FFCCBC", "FFAB91", "FF8A65", "FF7043", "FF5722", "F4511E",        //deep orange
            "E64A19", "D84315", "BF360C", "FF9E80", "FF6E40", "FF3D00", "DD2600",
            "EFEBE9", "D7CCC8", "BCAAA4", "A1887F", "8D6E63", "795548", "6D4C41",        //brown
            "5D4037", "4E342E", "3E2723",
            "FAFAFA", "F5F5F5", "EEEEEE", "E0E0E0", "BDBDBD", "9E9E9E", "757575",        //grey
            "616161", "424242", "212121",
            "ECEFF1", "CFD8DC", "B0BBC5", "90A4AE", "78909C", "607D8B", "546E7A",        //blue grey
            "455A64", "37474F", "263238"
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_main_activity_power_lists, container, false);
        //Bundle args = getArguments();
        //Log.d(TAG, "got args: " + args.getString("key"));


        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_activity_power_lists_recyclerView);
        adapter = new PowerListsAdapter();
        recyclerView.setAdapter(adapter);

        //layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

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

            //v.setBackgroundColor(getRandomColor());

            //get the drawable and give it a random color
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.recycler_child_circle);
            drawable.setColorFilter(getRandomColor(), PorterDuff.Mode.SRC_IN);

            //set background for the splotch, seems like this really has to be done like this
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                v.findViewById(R.id.splotchView).setBackground(drawable);
            } else {
                v.findViewById(R.id.splotchView).setBackgroundDrawable(drawable);
            }

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

    private int getRandomColor(){
        //might me nice if we would generate the color based on a string somehow to always get same color
        //https://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        //use some color to mix the random color to get a tint, here lightBlue is used
        red = (red + 173) / 2;
        green = (green + 216) / 2;
        blue = (blue + 230) / 2;

        return Color.rgb(red, green, blue);
    }
}
