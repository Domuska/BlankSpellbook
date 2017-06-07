package tomi.piipposoft.blankspellbook.MainActivity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Helper;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 26.5.2017.
 */

public class PowersFragment extends Fragment {

    private final String TAG = "PowersFragment";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    //private ArrayMap<String, Spell> powers = new ArrayMap<>();
    //just use two arraylists for this, map would be preferred but maps either put
    //elements in random order (not preferred) or you can't query element by index,
    //which needs to be done in onBindViewHolder
    ArrayList<Spell> powers = new ArrayList<>();
    ArrayList<String> powerListNames = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_main_activity_powers, container, false);

        //Bundle args = getArguments();
        //Log.d(TAG, "got args: " + args.getString("key"));

        recyclerView = (RecyclerView) rootView.findViewById(R.id.main_activity_powers_recyclerview);
        adapter = new PowerListAdapter();
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(
                recyclerView.getContext(),
                DividerItemDecoration.VERTICAL
        );
        recyclerView.addItemDecoration(divider);
        return rootView;
    }

    public void handleNewPower(@NonNull Spell power, String powerListName){
        //powers.put(power.getSpellId(), power);
        powers.add(power);
        //add the power list name nevertheless if it's empty to keep the lists in sync
        if(powerListName == null)
            powerListNames.add("");
        else
            powerListNames.add(powerListName);

        adapter.notifyItemInserted(powers.size()-1);
    }

    public void removePower(@NonNull Spell power){
        powers.remove(power);
        adapter.notifyItemRemoved(powers.size());
    }

    public void removeAllPowers(){
        int listSize = powers.size();
        powers = new ArrayList<>();
        adapter.notifyItemRangeRemoved(0, listSize);
    }

    class PowerListAdapter extends RecyclerView.Adapter<PowerListAdapter.ViewHolder> {


        class ViewHolder extends RecyclerView.ViewHolder {

            TextView powerName, powerListName, groupName;
            View splotchView;

            ViewHolder(View v){
                super(v);
                powerName = (TextView) v.findViewById(R.id.power_name);
                powerListName = (TextView) v.findViewById(R.id.power_list_name);
                groupName = (TextView) v.findViewById(R.id.power_group_name);
                splotchView = v.findViewById(R.id.splotchView);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.main_activity_powers_list_child_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final int itemPosition = holder.getAdapterPosition();
            //String powerId = powers.keyAt(itemPosition);
            //holder.powerName.setText(powers.get(powerId));
            Spell power = powers.get(itemPosition);
            holder.powerName.setText(power.getName());
            //set the group name text field if power has group
            if(!"".equals(power.getGroupName()))
                holder.groupName.setText(power.getGroupName());
            //set the power list name and colour for splotch if the power is in a group
            String powerListName = powerListNames.get(itemPosition);
            holder.powerListName.setText(powerListName);


            //set splotch colour
            if(!powerListName.equals("")){
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.recycler_child_rectangle);
                drawable.setColorFilter(Helper.getRandomColorFromString(powerListName), PorterDuff.Mode.SRC_IN);

                //set background for the splotch, seems like this really has to be done like this
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.splotchView.setBackground(drawable);
                }
                else {
                    //we can call this since only on ancient devices we get here, those still have this method
                    holder.splotchView.setBackgroundDrawable(drawable);
                }
            }

        }

        @Override
        public int getItemCount() {
            return powers.size();
        }
    }

}
