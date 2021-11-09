package edu.cs4730.androidbeaconlibrarydemo2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


public class HomeFragment extends Fragment {

    TextView logger;
    private myViewModel mViewModel;
    String TAG = "RangeFragment";
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        logger = root.findViewById(R.id.logger);
        mViewModel = new ViewModelProvider(requireActivity()).get(myViewModel.class);
        mViewModel.getItemLD().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.wtf(TAG, "new data");
                if (logger != null)
                    logger.setText(s);
            }
        });
        return root;
    }


}