package edu.cs4730.bluetoothledemo;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends Fragment {
    private final static String TAG = "HelpFragment";
    //bluetooth device and code to turn the device on if needed.
    BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;
    TextView logger;

    public HelpFragment() {
        // Required empty public constructor
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            logthis("The bluetooth is ready to use.");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_help, container, false);
        logger = myView.findViewById(R.id.loggerh);
        myView.findViewById(R.id.btn_advertise).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_advertise));
        myView.findViewById(R.id.btn_discover).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_discover));
        myView.findViewById(R.id.btn_perm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkpermissions();
            }
        });
        checkpermissions();
        startbt();
        return myView;
    }

    void checkpermissions() {
        //needs fine location for API 28+ or coarse location below 28 for the discovery only.
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            logthis("asking for permissions");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MainActivity.REQUEST_ACCESS_COURSE_LOCATION);
            logthis("We don't have permission to fine location");
        } else {
            logthis("We have permission to fine location");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MainActivity.REQUEST_ACCESS_COURSE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logthis("permission granted to fine location");
            }
        }
    }

    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

}
