package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.util.Log;
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

/**
 * Created by Domu on 11-Jun-16.
 *
 * Created with help from https://www.bignerdranch.com/blog/expand-a-recyclerview-in-four-steps/
 * Has parent and child ViewHolders, when parent is clicked it is expanded
 * and child is brought below parent
 */
class PowerListRecyclerAdapter extends ExpandableRecyclerAdapter
        <PowerListRecyclerAdapter.SpellGroupViewHolder, PowerListRecyclerAdapter.SpellViewHolder>{

    private ArrayList<Spell> dataSet;
    private LayoutInflater inflater;
    private PowerListContract.UserActionListener actionListener;

    //map for keeping track which spells have been selected by user
    private static ArrayList<Spell> isSpellSelected = new ArrayList<>();


    PowerListRecyclerAdapter(Context context, List<? extends ParentListItem> spellGroups,
                                    PowerListContract.UserActionListener listener){
        super(spellGroups);
        actionListener = listener;
        inflater = LayoutInflater.from(context);
    }

    static ArrayList<Spell> getSelectedSpells(){
        return isSpellSelected;
    }

    @Override
    public SpellGroupViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View spellGroupView = inflater.inflate(R.layout.power_list_list_parent_row, parentViewGroup, false);
        return new SpellGroupViewHolder(spellGroupView);
    }

    @Override
    public SpellViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View spellView = inflater.inflate(R.layout.power_list_list_child_row, childViewGroup, false);
        return new SpellViewHolder(spellView);
    }

    @Override
    public void onBindParentViewHolder(SpellGroupViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        SpellGroup spellGroup = (SpellGroup) parentListItem;
        parentViewHolder.bind(spellGroup);
    }

    @Override
    public void onBindChildViewHolder(final SpellViewHolder childViewHolder, int position, Object childListItem) {
        final Spell spell = (Spell) childListItem;
        childViewHolder.bind(spell);

        //onClickListener for when the row is not "selected": start new activity
        final View.OnClickListener startActivityOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PowerListRecyclerAdapter.this.actionListener.openPowerDetails(spell.getSpellId());
            }
        };

        //onClickListener when the row is "selected": unselect the row
        final View.OnClickListener selectedClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                childViewHolder.recyclerRowBackground.setSelected(false);
                isSpellSelected.remove(spell);
                //since row is not now selected, return the default onClickListener
                childViewHolder.recyclerRowBackground.setOnClickListener(startActivityOnClickListener);
            }
        };

        //onLongClickListener: select or unselect the row
        View.OnLongClickListener unSelectedLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(childViewHolder.recyclerRowBackground.isSelected()) {
                    //item already selected, set unselected
                    childViewHolder.recyclerRowBackground.setSelected(false);
                    //remove the selected item from map of selected items
                    isSpellSelected.remove(spell);
                }
                else {
                    //item not selected, set selected
                    childViewHolder.recyclerRowBackground.setSelected(true);
                    //add item to map of selected items
                    isSpellSelected.add(spell);
                    //since row is now selected, set the click listener which removes the selection
                    childViewHolder.recyclerRowBackground.setOnClickListener(selectedClickListener);

                }
                return true;
            }
        };

        //set the onClickListener on the row so user doesn't have to aim to the text
        childViewHolder.recyclerRowBackground.setOnClickListener(startActivityOnClickListener);
        childViewHolder.recyclerRowBackground.setOnLongClickListener(unSelectedLongClickListener);
    }





    // ADAPTERS
    class SpellGroupViewHolder extends ParentViewHolder {

        TextView parentTextView;
        ImageButton parentDropDownArrow;

        SpellGroupViewHolder(View view){
            super(view);
            parentTextView = (TextView) view.findViewById(R.id.recycler_parent_text_view);
            parentDropDownArrow = (ImageButton) view.findViewById(R.id.recycler_parent_expand_arrow);
        }

        void bind(SpellGroup spellGroup){
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

    class SpellViewHolder extends ChildViewHolder{

        TextView childTextView;
        View recyclerRowBackground;

        SpellViewHolder(View view){
            super(view);
            recyclerRowBackground = view;
            childTextView = (TextView) view.findViewById(R.id.recycler_child_text_view);
        }

        void bind(final Spell spell) {
            childTextView.setText(spell.getName());

            //if the item is in map of selected items, add set it as selected
            if(isSpellSelected.contains(spell)) {
                recyclerRowBackground.setSelected(true);
                Log.d("PowerListsAdapter", "spell " + spell.getName() +  " should be selected");
            }
            else{
                recyclerRowBackground.setSelected(false);
                Log.d("PowerListsAdapter", "spell " + spell.getName() + " should not be selected");
            }
        }
    }
}
