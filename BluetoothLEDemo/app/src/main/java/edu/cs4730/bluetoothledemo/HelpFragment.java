package edu.cs4730.bluetoothledemo;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.Map;

import edu.cs4730.bluetoothledemo.databinding.FragmentHelpBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends Fragment {
    private final static String TAG = "HelpFragment";
    ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;
    //bluetooth device and code to turn the device on if needed.
    BluetoothAdapter mBluetoothAdapter = null;
    FragmentHelpBinding binding;

    public HelpFragment() {
        bluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        logthis("Bluetooth is on.");
                    } else {
                        logthis("Please turn the bluetooth on.");
                    }
                }
            });
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                        logthis(x.getKey() + " is " + x.getValue());
                        if (!x.getValue()) granted = false;
                    }
                    if (granted) startbt();
                }
            }
        );
    }

    //This code will check to see if there is a bluetooth device and
    //turn it on if is it turned off.
    public void startbt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            logthis("This device does not support bluetooth");
            return;
        }
        //make sure bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            logthis("There is bluetooth, but turned off");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivityResultLauncher.launch(enableBtIntent);
        } else {
            logthis("The bluetooth is ready to use.");
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHelpBinding.inflate(inflater, container, false);

        //setup the correct permissions needed, depending on which version. (31 changed the permissions.).
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE};
            logthis("Android 12+, we need scan, advertise, and connect.");
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION};
            logthis("Android 11 or less, bluetooth permissions only ");
        }


        binding.btnAdvertise.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_advertise));
        binding.btnDiscover.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_discover));
        binding.btnPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rpl.launch(REQUIRED_PERMISSIONS);
            }
        });
        return binding.getRoot();
    }

    public void logthis(String msg) {
        binding.loggerh.append(msg + "\n");
        Log.d(TAG, msg);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!allPermissionsGranted())
            rpl.launch(REQUIRED_PERMISSIONS);
        else {
            logthis("All permissions have been granted already.");
            startbt();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
