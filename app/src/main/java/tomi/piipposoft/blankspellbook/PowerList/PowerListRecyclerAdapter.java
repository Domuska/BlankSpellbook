package tomi.piipposoft.blankspellbook.PowerList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.Model.ParentWrapper;
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
class PowerListRecyclerAdapter
        extends ExpandableRecyclerAdapter
        <PowerListRecyclerAdapter.SpellGroupViewHolder, PowerListRecyclerAdapter.SpellViewHolder> {

    private final String TAG = "PowerListRecyclerAdapte";
    private LayoutInflater inflater;
    private PowerListContract.UserActionListener actionListener;

    //map for keeping track which spells have been selected by user
    private ArrayList<Spell> selectedSpellsList = new ArrayList<>();
    private Animation rotateAnimation, reverseRotateAnimation;
    private final int ARROW_ROTATION_DURATION = 300;


    //flag for setting when user is selecting power lists for deletion
    private boolean selectionMode;

    PowerListRecyclerAdapter(Context context, List<? extends ParentListItem> spellGroups,
                                    PowerListContract.UserActionListener listener){
        super(spellGroups);
        actionListener = listener;
        inflater = LayoutInflater.from(context);

        rotateAnimation =
                new RotateAnimation(0.0f, 180f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(ARROW_ROTATION_DURATION);
        rotateAnimation.setFillAfter(true);

        reverseRotateAnimation =
                new RotateAnimation(180f, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        reverseRotateAnimation.setDuration(ARROW_ROTATION_DURATION);
        reverseRotateAnimation.setFillAfter(true);
    }

    ArrayList<Spell> getSelectedSpells(){
        return selectedSpellsList;
    }

    void endSelectionMode(){
        selectionMode = false;
        selectedSpellsList = new ArrayList<>();
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


        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selection mode on, select row instead of opening activity
                if (selectionMode) {
                    if (childViewHolder.recyclerRowBackground.isSelected()) {
                        //item already selected, unselect it
                        childViewHolder.recyclerRowBackground.setSelected(false);
                        selectedSpellsList.remove(spell);
                    } else {
                        //select the item
                        childViewHolder.recyclerRowBackground.setSelected(true);
                        selectedSpellsList.add(spell);
                    }
                    //if the list is empty, end selection mode
                    if (selectedSpellsList.size() < 1) {
                        selectionMode = false;
                    }
                } else
                    PowerListRecyclerAdapter.this.actionListener.openPowerDetails(
                                    spell.getSpellId(),
                                    spell.getName(),
                                    childViewHolder.childTextView);
            }
        };

        //onLongClickListener: select or unselect the row
        View.OnLongClickListener unSelectedLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (childViewHolder.recyclerRowBackground.isSelected()) {
                    //item already selected, set unselected
                    childViewHolder.recyclerRowBackground.setSelected(false);
                    //remove the selected item from map of selected items
                    selectedSpellsList.remove(spell);
                    //set off "selection mode"
                    selectionMode = false;
                } else {
                    //item not selected, set selected
                    childViewHolder.recyclerRowBackground.setSelected(true);
                    //add item to map of selected items
                    selectedSpellsList.add(spell);
                    //enable selection mode
                    selectionMode = true;
                }
                return true;
            }
        };

        //set the onClickListener for the whole row to make clicking easier
        childViewHolder.recyclerRowBackground.setOnClickListener(onClickListener);
        childViewHolder.recyclerRowBackground.setOnLongClickListener(unSelectedLongClickListener);
    }

    // ADAPTERS
    class SpellGroupViewHolder extends ParentViewHolder {

        TextView parentTextView;
        ImageButton parentDropDownArrow;

        ArrayList<ImageButton> parentArrows = new ArrayList<>();

        SpellGroupViewHolder(View view){
            super(view);
            parentTextView = (TextView) view.findViewById(R.id.recycler_parent_text_view);
            parentDropDownArrow = (ImageButton) view.findViewById(R.id.recycler_parent_expand_arrow);
            parentArrows.add(parentDropDownArrow);

            parentDropDownArrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isExpanded()) {
                        collapseView();
                    }
                    else {
                        expandView();
                    }
                }
            });
        }

        void bind(SpellGroup spellGroup){
            parentTextView.setText(spellGroup.getGroupName());
        }

        @Override
        public void setExpanded(boolean expanded) {
            super.setExpanded(expanded);
            //this would be better with proper animations, but can't get them to work reasonably
            //the other arrows are animated too when one of the rows is expanded, leave this like this for now
            //not sure if this library will be even used in future, it has not been updated in a long time
            //12.6.2017
            //Log.d(TAG, "setExpanded: adapter position: " + getAdapterPosition());

            if(expanded){
                //parentArrows.get(getAdapterPosition()).startAnimation(rotateAnimation);
                //parentDropDownArrow.startAnimation(rotateAnimation);
                parentDropDownArrow.setRotation(180);
                Log.d(TAG, "row is now expanded");
            }
            else {
                //parentArrows.get(getAdapterPosition()).startAnimation(reverseRotateAnimation);
                //parentDropDownArrow.startAnimation(reverseRotateAnimation);
                parentDropDownArrow.setRotation(0);

                Log.d(TAG, "row is not expanded any more");
            }
        }

        @Override
        public void onExpansionToggled(boolean expanded) {
            super.onExpansionToggled(expanded);
            Log.d(TAG, "onExpansionToggled: expanded? " + expanded);
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
            if(selectedSpellsList.contains(spell)) {
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
