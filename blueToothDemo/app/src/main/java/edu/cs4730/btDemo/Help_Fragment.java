package edu.cs4730.btDemo;

import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;


/**
 * simple helper to turn on bluetooth and check permissions.
 */
public class Help_Fragment extends Fragment {

    ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;
    String TAG = "HelpFragment";
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;

    //bluetooth device and code to turn the device on if needed.
    BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;
    Button btn_client, btn_server;
    TextView logger;

    public Help_Fragment() {
        bluetoothActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        logthis("Bluetooth is on.");
                        querypaired();
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

    //A simple method to append data to the logger textview.
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
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
            //bluetooth is on, so list paired devices from here.
            querypaired();
        }
    }

    /**
     * This method will query the bluetooth device and ask for a list of all
     * paired devices.  It will then display to the screen the name of the device and the address
     * In client fragment we need this address to so we can connect to the bluetooth device that is acting as the server.
     */
    @SuppressLint("MissingPermission")  //already done in onResume.
    public void querypaired() {
        logthis("Paired Devices:");
       Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            final BluetoothDevice[] blueDev = new BluetoothDevice[pairedDevices.size()];
            String item;
            int i = 0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                item = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                logthis("Device: " + item);
                i++;
            }

        } else {
            logthis("There are no paired devices");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_help, container, false);
        logger = myView.findViewById(R.id.logger1);

        //setup the correct permissions needed, depending on which version. (31 changed the permissions.).
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{"android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"};
            logthis("Android 12+, we need scan and connect.");
        } else {
            REQUIRED_PERMISSIONS = new String[]{ "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"};
            logthis("Android 11 or less, bluetooth permissions only ");
        }

        btn_client = myView.findViewById(R.id.button2);
        btn_client.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_client, null));
        btn_server = myView.findViewById(R.id.button1);
        btn_server.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_help_to_server, null));
        

        return myView;
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
