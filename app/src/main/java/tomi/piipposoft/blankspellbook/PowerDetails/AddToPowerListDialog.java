package tomi.piipposoft.blankspellbook.PowerDetails;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 15.5.2017.
 * Used for displaying the popup in PowerDetailsActivity when user wants
 * to add a power to another list of powers or a list of daily powers
 *
 * Works as a state machine, controlled by enumerated selectedList.
 */

public class AddToPowerListDialog extends DialogFragment {

    // the lists are used to populate the recyclerview's checkboxes
    // and to know the ID of the elements we get
    // the lists should be ordered so that
    // in the entry in 1st index of powerListIds is the ID of the
    // power list named in powerListNames
    // same goes for the daily power lists
    // could be done with a map but left like this for now
    private String[] powerListNames;
    private String[] powerListIds;
    private String[] dailyPowerListNames;
    private String[] dailyPowerListIds;
    private final String TAG = "AddToPowerListDialog";


    private enum Selected {POWER_LISTS, DAILY_POWER_LISTS}
    Selected selectedList = Selected.POWER_LISTS;
    final ArrayList<String> selectedListIds = new ArrayList<>();


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener{
        void onAddToListPositiveClick(DialogFragment dialog, ArrayList<String> listId);
    }

    // Use this instance of the interface to deliver action events
    AddToPowerListDialog.NoticeDialogListener mListener;

    // could maybe pass in parcelable or somesuch, this is easier for now though
    public static AddToPowerListDialog newInstance(String[] powerListNames,
                                                   String[] powerListIds,
                                                   String[] dailyPowerListNames,
                                                   String[] dailyPowerListIds){

        AddToPowerListDialog dialog = new AddToPowerListDialog();
        //supply the arguments
        Bundle args = new Bundle();
        args.putStringArray("powerListNames", powerListNames);
        args.putStringArray("powerListIds", powerListIds);
        args.putStringArray("dailyPowerListNames", dailyPowerListNames);
        args.putStringArray("dailyPowerListIds", dailyPowerListIds);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddToPowerListDialog.NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        powerListNames = bundle.getStringArray("powerListNames");
        powerListIds = bundle.getStringArray("powerListIds");
        dailyPowerListNames = bundle.getStringArray("dailyPowerListNames");
        dailyPowerListIds = bundle.getStringArray("dailyPowerListIds");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_to_power_list, null);

        //create the recyclerview where power lists and daily power lists are shown
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_add_to_power_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        final AddToPowerListAdapter adapter =
                new AddToPowerListAdapter(powerListNames, dailyPowerListNames);
        recyclerView.setAdapter(adapter);

        final RadioButton powerListButton = (RadioButton) view.findViewById(R.id.radio_power_list);
        powerListButton.setChecked(true);
        powerListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setState(Selected.POWER_LISTS);
                //notify the adapter so the list of items is refreshed
                adapter.notifyDataSetChanged();
            }
        });

        final RadioButton dailyPowerListButton = (RadioButton) view.findViewById(R.id.radio_daily_power_list);
        dailyPowerListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setState(Selected.DAILY_POWER_LISTS);
                //notify the adapter so the list of items is refreshed
                adapter.notifyDataSetChanged();
            }
        });

        builder.setTitle(R.string.add_to_list_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAddToListPositiveClick(
                                AddToPowerListDialog.this, selectedListIds);
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddToPowerListDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * Add an element to the selectedListIds
     * * @param elementPosition position in powerListIds or dailyPowerListIds of the element that is to be added
     */
    private void addSelectedItem(int elementPosition){
        Log.d(TAG, "adding item at position: " + elementPosition);
        if(selectedList == Selected.POWER_LISTS) {
            Log.d(TAG, "adding item with ID from selected lists: " + powerListIds[elementPosition]);
            selectedListIds.add(powerListIds[elementPosition]);
        }
        else {
            Log.d(TAG, "adding item with ID from selected lists: " + dailyPowerListIds[elementPosition]);
            selectedListIds.add(dailyPowerListIds[elementPosition]);
        }
    }

    /**
     * Remove an element from the list of selected list ids
     * @param elementPosition position in powerListIds or dailyPowerListIds of the element that is to be removed
     */
    private void removeSelectedItem(int elementPosition){
        if(selectedList == Selected.POWER_LISTS)
            selectedListIds.remove(powerListIds[elementPosition]);
        else
            selectedListIds.remove(dailyPowerListIds[elementPosition]);
    }

    /**
     * Used to control the state of the recycler's items
     * @param selected the state that has been selected, see Selected enum
     */
    private void setState(Selected selected){
        this.selectedList = selected;
        //empty the list since we are not saving which items were selected previously
        selectedListIds.clear();
    }


    /**
     * Adapter for the recyclerview shown in this fragment
     */
    private class AddToPowerListAdapter extends
            RecyclerView.Adapter<AddToPowerListAdapter.ViewHolder>{

        private String[] powerListNames, dailyPowerListNames;

        class ViewHolder extends RecyclerView.ViewHolder{
            private CheckBox checkBox;
            private ViewHolder(View view){
                super(view);
                checkBox = (CheckBox)view.findViewById(R.id.myCheckBox);
            }
        }

        private AddToPowerListAdapter(String[] powerListNames, String[] dailyPowerListNames){
            this.powerListNames = powerListNames;
            this.dailyPowerListNames = dailyPowerListNames;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //set the checkboxes unchecked here, otherwise when changing
            //the dataset the recycled checkboxes occasionally stay checked
            final int checkBoxPosition = holder.getAdapterPosition();

            if(selectedList == Selected.POWER_LISTS)
                holder.checkBox.setText(powerListNames[position]);
            else
                holder.checkBox.setText(dailyPowerListNames[position]);

            holder.checkBox.setChecked(false);

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                        addSelectedItem(checkBoxPosition);
                    else
                        removeSelectedItem(checkBoxPosition);
                }
            });
        }

        @Override
        public AddToPowerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.add_to_power_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            if(selectedList == Selected.POWER_LISTS)
                return powerListNames.length;
            else
                return dailyPowerListNames.length;
        }
    }
}
