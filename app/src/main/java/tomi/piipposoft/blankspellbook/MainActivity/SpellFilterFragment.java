package tomi.piipposoft.blankspellbook.MainActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

        RecyclerView.Adapter classAdapter = new ClassListAdapter();
        classRecyclerView.setAdapter(classAdapter);

        RecyclerView groupRecyclerView = rootView.findViewById(R.id.groupFilterRecyclerView);
        LinearLayoutManager groupLinearLayoutManager = new LinearLayoutManager(getActivity());
        groupRecyclerView.setLayoutManager(groupLinearLayoutManager);

        RecyclerView.Adapter groupListAdapter = new GroupListAdapter();
        groupRecyclerView.setAdapter(groupListAdapter);


        return rootView;
    }

    //https://developer.android.com/guide/components/fragments.html


    private class ClassListAdapter extends
            RecyclerView.Adapter<ClassListAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView className;
            private ViewHolder(View view){
                super(view);
                className = view.findViewById(R.id.recycler_row_text);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.filter_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.className.setText(classNames.get(position));
        }

        @Override
        public int getItemCount() {
            return classNames.size();
        }
    }

    private class GroupListAdapter extends
            RecyclerView.Adapter<GroupListAdapter.ViewHolder>{

        class ViewHolder extends RecyclerView.ViewHolder{
            private TextView groupName;
            private ViewHolder(View view){
                super(view);
                groupName = view.findViewById(R.id.recycler_row_text);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.filter_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.groupName.setText(groupNames.get(position));
        }

        @Override
        public int getItemCount() {
            return groupNames.size();
        }
    }
}
