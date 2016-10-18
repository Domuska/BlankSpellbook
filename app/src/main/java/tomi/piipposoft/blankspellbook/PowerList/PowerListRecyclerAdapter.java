package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.R;
import tomi.piipposoft.blankspellbook.Utils.Spell;
import tomi.piipposoft.blankspellbook.PowerDetails.PowerDetailsActivity;

/**
 * Created by Domu on 11-Jun-16.
 *
 * Created with help from https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * Has parent and child ViewHolders, when parent is clicked it is expanded
 * and child is brought below parent
 */
public class PowerListRecyclerAdapter extends ExpandableRecyclerAdapter
        <PowerListRecyclerAdapter.SpellGroupViewHolder, PowerListRecyclerAdapter.SpellViewHolder>{

    private ArrayList<Spell> dataSet;
    private LayoutInflater inflater;
    private PowerListContract.UserActionListener actionListener;


    public PowerListRecyclerAdapter(Context context, List<? extends ParentListItem> spellGroups,
                                    PowerListContract.UserActionListener listener){
        super(spellGroups);
        actionListener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public SpellGroupViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View spellGroupView = inflater.inflate(R.layout.spell_book_recycler_parent_row, parentViewGroup, false);
        return new SpellGroupViewHolder(spellGroupView);
    }

    @Override
    public SpellViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View spellView = inflater.inflate(R.layout.spell_book_recycler_child_row, childViewGroup, false);
        return new SpellViewHolder(spellView);
    }

    @Override
    public void onBindParentViewHolder(SpellGroupViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        SpellGroup spellGroup = (SpellGroup) parentListItem;
        parentViewHolder.bind(spellGroup);
    }

    @Override
    public void onBindChildViewHolder(SpellViewHolder childViewHolder, int position, Object childListItem) {
        Spell spell = (Spell) childListItem;
        childViewHolder.bind(spell);

    }




    // ADAPTERS
    public class SpellGroupViewHolder extends ParentViewHolder {

        public TextView parentTextView;
        public ImageButton parentDropDownArrow;

        public SpellGroupViewHolder(View view){
            super(view);
            parentTextView = (TextView) view.findViewById(R.id.recycler_parent_text_view);
            parentDropDownArrow = (ImageButton) view.findViewById(R.id.recycler_parent_expand_arrow);
        }

        public void bind(SpellGroup spellGroup){
            parentTextView.setText(spellGroup.getGroupName());
        }

        @Override
        public void setExpanded(boolean expanded) {
            super.setExpanded(expanded);
            // TODO: 12-Jun-16 some animation could go here
        }

        @Override
        public void onExpansionToggled(boolean expanded) {
            super.onExpansionToggled(expanded);
            //todo some animations could go here
        }
    }

    public class SpellViewHolder extends ChildViewHolder{

        public TextView childTextView;

        public SpellViewHolder(View view){
            super(view);
            childTextView = (TextView) view.findViewById(R.id.recycler_child_text_view);
        }

        public void bind(final Spell spell) {
            childTextView.setText(spell.getName());
            childTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PowerListRecyclerAdapter.this.actionListener.openPowerDetails(spell.getSpellId(), false);
                }
            });
        }
    }
}
