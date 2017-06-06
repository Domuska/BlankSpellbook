package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by OMISTAJA on 26.5.2017.
 */

public class PowersFragment extends Fragment {

    private final String TAG = "PowersFragment";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    //private ArrayMap<String, Spell> powers = new ArrayMap<>();
    ArrayList<Spell> powers = new ArrayList<>();

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


        return rootView;
    }

    public void handleNewPower(@NonNull Spell power){
        //powers.put(power.getSpellId(), power);
        powers.add(power);
        adapter.notifyItemInserted(powers.size()-1);
    }

    public void removePower(@NonNull Spell power){
        powers.remove(power);
        adapter.notifyItemRemoved(powers.size());
    }

    public void removeAllPowers(){
        int listSize = powers.size()-1;
        powers = new ArrayList<>();
        adapter.notifyItemRangeRemoved(0, listSize);
    }

    class PowerListAdapter extends RecyclerView.Adapter<PowerListAdapter.ViewHolder> {


        class ViewHolder extends RecyclerView.ViewHolder {

            TextView powerName, groupName;

            ViewHolder(View v){
                super(v);
                powerName = (TextView) v.findViewById(R.id.power_name);
                groupName = (TextView) v.findViewById(R.id.power_group_name);
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
            if(!"".equals(power.getGroupName()))
                holder.groupName.setText(power.getGroupName());

        }

        @Override
        public int getItemCount() {
            return powers.size();
        }
    }

}
