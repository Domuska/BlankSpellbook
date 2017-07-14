package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 13.7.2017.
 */

public class SpellFilterFragment extends Fragment {

    private final String TAG = "SpellFilterFragment";
    public static final String GROUP_NAMES_BUNDLE = "groupNames";
    public static final String CLASS_NAMES_BUNDLE = "classNames";

    private ArrayList<String> classNames, groupNames;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_filters, container, false);
        if(getArguments() != null) {
            groupNames = getArguments().getStringArrayList(GROUP_NAMES_BUNDLE);
            classNames = getArguments().getStringArrayList(CLASS_NAMES_BUNDLE);
        }
        else
            Log.e(TAG, "Error in onCreateView, bundle is null");

        RecyclerView classRecyclerView = rootView.findViewById(R.id.classFilterRecyclerView);
        LinearLayoutManager classLinearLayoutManager = new LinearLayoutManager(getActivity());
        classRecyclerView.setLayoutManager(classLinearLayoutManager);

        RecyclerView.Adapter classAdapter = new FilterListAdapter(true);
        classRecyclerView.setAdapter(classAdapter);

        RecyclerView groupRecyclerView = rootView.findViewById(R.id.groupFilterRecyclerView);
        LinearLayoutManager groupLinearLayoutManager = new LinearLayoutManager(getActivity());
        groupRecyclerView.setLayoutManager(groupLinearLayoutManager);

        RecyclerView.Adapter groupListAdapter = new FilterListAdapter(false);
        groupRecyclerView.setAdapter(groupListAdapter);


        return rootView;
    }

    //https://developer.android.com/guide/components/fragments.html

    /**
     * Adapter for the classes and groups RecyclerViews
     * The constructor is passed a boolean to indicate whether this is a classes adapter (true)
     * or a groups adapter (false). The adapters are so similar that there is no point
     * to make two separate, at least for now.
     */
    private class FilterListAdapter extends
            RecyclerView.Adapter<FilterListAdapter.ViewHolder>{

        private boolean isClassAdapter;

        private FilterListAdapter(boolean isClassAdapter){
            this.isClassAdapter = isClassAdapter;
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView rowText;
            private View rowBackground;
            private ViewHolder(View view){
                super(view);
                rowText = view.findViewById(R.id.recycler_row_text);
                rowBackground = view;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.filter_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            if(isClassAdapter) {
                holder.rowText.setText(classNames.get(position));
            }
            else {
                holder.rowText.setText(groupNames.get(position));
            }

            //color every other row with darker background
            if(position % 2 == 0) {
                holder.rowBackground.setBackgroundColor(ContextCompat.getColor(
                        getActivity(), R.color.my_color_filter_row_background_dark
                ));
            }

            holder.rowBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "row with text " + holder.rowText.getText().toString() + " was clicked");
                }
            });
        }

        @Override
        public int getItemCount() {
            if(isClassAdapter)
                return classNames.size();
            else
                return groupNames.size();
        }
    }
}
