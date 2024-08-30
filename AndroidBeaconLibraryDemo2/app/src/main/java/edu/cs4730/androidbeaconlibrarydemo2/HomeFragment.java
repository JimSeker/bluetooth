package edu.cs4730.androidbeaconlibrarydemo2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import edu.cs4730.androidbeaconlibrarydemo2.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    private myViewModel mViewModel;
    String TAG = "RangeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(requireActivity()).get(myViewModel.class);

        return binding.getRoot();
    }

    // This method is called when the fragment is visible to the user, after onCreateView, so binding won't be null.
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.getItemLD().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.wtf(TAG, s);
                binding.logger.setText(s);
            }
        });
    }


}