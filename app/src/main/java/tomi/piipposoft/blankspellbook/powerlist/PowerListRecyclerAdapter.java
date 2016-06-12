package tomi.piipposoft.blankspellbook.powerlist;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Spell;

/**
 * Created by Domu on 11-Jun-16.
 */
public class PowerListRecyclerAdapter extends RecyclerView.Adapter<PowerListRecyclerAdapter.ViewHolder>{

    private ArrayList<Spell> dataSet;

    // ADAPTER
    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView textView;

        public ViewHolder(View view){
            super(view);
            textView = (TextView) view.findViewById(R.id.recycler_row_text_view);
        }
    }

    public PowerListRecyclerAdapter(ArrayList<Spell> dataSet){
        this.dataSet = dataSet;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(dataSet.get(position).getName());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.spell_book_recycler_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
