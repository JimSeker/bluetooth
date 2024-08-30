package edu.cs4730.androidbeaconlibrarydemo2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.cs4730.androidbeaconlibrarydemo2.databinding.FragmentRangeBinding;

/**
 * This needs to have a recycler view associated with it and display the data that way, otherwise,
 * it's impossible to follow.
 */

public class RangeFragment extends Fragment {
    FragmentRangeBinding binding;
    private myViewModel mViewModel;
    myAdapter mAdapter;
    String TAG = "RangeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRangeBinding.inflate(inflater, container, false);

        //setup recyclerview here
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerview.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new myAdapter(null, R.layout.row_layout, getContext());
        //add the adapter to the recyclerview
        binding.recyclerview.setAdapter(mAdapter);


        //setup the view model first.
        mViewModel = new ViewModelProvider(requireActivity()).get(myViewModel.class);
        mViewModel.getBeaconlist().observe(getViewLifecycleOwner(), new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(@Nullable Collection<Beacon> data) {
                Log.v(TAG, "Data changed, updating!");
                mAdapter.setMyList(data);
            }
        });

        return binding.getRoot();
    }

    public void logthis(String item) {
        if (binding != null)  //it's pcould be called, because onCreateView is called.
            binding.textNotifications.setText(item);
    }

}