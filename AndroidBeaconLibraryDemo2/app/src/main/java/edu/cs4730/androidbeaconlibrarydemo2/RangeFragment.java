package edu.cs4730.androidbeaconlibrarydemo2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This needs to have a recycler view associated with it and display the data that way, otherwise,
 * it's impossible to follow.
 */

public class RangeFragment extends Fragment {
    TextView logger;
    private myViewModel mViewModel;
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    String TAG = "RangeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_range, container, false);
        logger = root.findViewById(R.id.text_notifications);
        //setup recyclerview here
        mRecyclerView = root.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new myAdapter(null, R.layout.row_layout, getContext());
        //add the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);


        //setup the view model first.
        mViewModel = new ViewModelProvider(getActivity()).get(myViewModel.class);
        mViewModel.getBeaconlist().observe(getViewLifecycleOwner(), new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(@Nullable Collection<Beacon> data) {
                Log.v(TAG, "Data changed, updating!");
                mAdapter.setMyList(data);
            }
        });


        return root;
    }
    public void logthis(String item) {
        if (logger != null)
        logger.setText(item);
    }

}