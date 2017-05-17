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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 15.5.2017.
 */

public class AddToPowerListDialog extends DialogFragment {

    private String[] powerListNames;
    private String[] powerListIds;
    private String[] dailyPowerListNames;
    private String[] dailyPowerListIds;
    private final String TAG = "AddToPowerListDialog";
    private static int selectedItem;

    private FragmentTabHost tabHost;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener{
        void onAddToListPositiveClick(DialogFragment dialog, String listId);
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
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_add_to_power_list);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);

        AddToPowerListAdapter adapter = new AddToPowerListAdapter(powerListNames);
        recyclerView.setAdapter(adapter);

        final RadioButton powerListButton = (RadioButton) view.findViewById(R.id.radio_power_list);
        powerListButton.setChecked(true);
        final RadioButton dailyPowerListButton = (RadioButton) view.findViewById(R.id.radio_daily_power_list);

        builder.setTitle(R.string.add_to_list_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listId;
                        Log.d(TAG, "which at onClick: " + selectedItem);
                        if(powerListButton.isChecked()){
                            listId = powerListIds[selectedItem];
                        }
                        else
                            listId = dailyPowerListIds[selectedItem];

                        mListener.onAddToListPositiveClick(
                                AddToPowerListDialog.this, listId);
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

    private class AddToPowerListAdapter extends
            RecyclerView.Adapter<AddToPowerListAdapter.ViewHolder>{

        private String[] dataset;

        class ViewHolder extends RecyclerView.ViewHolder{
            private CheckBox checkBox;
            private ViewHolder(View view){
                super(view);
                checkBox = (CheckBox)view.findViewById(R.id.myCheckBox);
            }
        }

        private AddToPowerListAdapter(String[] dataset){
            this.dataset = dataset;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.checkBox.setText(dataset[position]);
        }

        @Override
        public AddToPowerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.add_to_power_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return dataset.length;
        }
    }
}
