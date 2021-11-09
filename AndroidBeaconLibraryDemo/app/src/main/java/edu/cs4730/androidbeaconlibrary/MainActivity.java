package edu.cs4730.androidbeaconlibrary;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * very beginning of a beacon replacement example since google is dropping all support.
 * <p>
 * https://altbeacon.github.io/android-beacon-library/index.html
 * https://github.com/AltBeacon
 */


public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    TextView logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = findViewById(R.id.logger);
        logthis("App Starting");
        //check for permissions and start the beacons.
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //added eddystone, since I'm moving from google's beacons to altbeacon.  RedBeacon can broadcast both.
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        checkpermissions();

    }


    void startexample() {
        logthis("Starting up!");
        // Set up a Live Data observer so this Activity can get monitoring callbacks
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        Region myRegion = new Region("myMonitoringUniqueId", null, null, null);
        //first the monitor
        logthis("beacon monitor observer has been added.");
        beaconManager.getRegionViewModel(myRegion).getRegionState().observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer state) {
                    if (state == MonitorNotifier.INSIDE) {
                        Log.d(TAG, "Detected beacons(s)");
                        logthis("I can see at least one beacon");
                    }
                    else {
                        Log.d(TAG, "Stopped detecting beacons");
                        logthis("I no longer see an beacon");

                    }
                }
            }


        );
        beaconManager.startMonitoring(myRegion);

        //now the ranged information.
        logthis("beacon range observer has been added.");
        beaconManager.getRegionViewModel(myRegion).getRangedBeacons().observe(this, new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(Collection<Beacon> beacons) {
                logthis("didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                for (Beacon beacon : beacons) {
                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                        // This is a Eddystone-UID frame
                        Identifier namespaceId = beacon.getId1();
                        Identifier instanceId = beacon.getId2();
                        logthis("I see a beacon transmitting namespace id: " + namespaceId +
                            " and instance id: " + instanceId +
                            " approximately " + beacon.getDistance() + " meters away.");

                        // Do we have telemetry data?
                        if (beacon.getExtraDataFields().size() > 0) {
                            long telemetryVersion = beacon.getExtraDataFields().get(0);
                            long batteryMilliVolts = beacon.getExtraDataFields().get(1);
                            long pduCount = beacon.getExtraDataFields().get(3);
                            long uptime = beacon.getExtraDataFields().get(4);

                            logthis(
                                "The above beacon is sending telemetry version " + telemetryVersion +
                                    ", has been up for : " + uptime + " seconds" +
                                    ", has a battery level of " + batteryMilliVolts + " mV" +
                                    ", and has transmitted " + pduCount + " advertisements.");

                        }

                    } else if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                        // This is a Eddystone-URL frame
                        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                        logthis("I see a beacon transmitting a url: " + url +
                            " approximately " + beacon.getDistance() + " meters away.");
                    } else {
                        //no clue what we found here.
                        logthis("found a beacon, (not eddy) " + beacon.toString() + " and is approximately " + beacon.getDistance() + "meters away");
                    }

                }
            }
        });
        beaconManager.startRangingBeacons(myRegion);
    }

    /**
     * helper method to send strings to both the logcat and to a textview call logger.
     */
    private void logthis(String item) {
        Log.v(TAG, item);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logger.append("\n" + item);
            }
        });
    }

    /**
     * below is all the pieces to get the permissions setup correctly and basically have nothing to do with beacons
     * See the manifest file for all the permissions this app is using.
     */
    private ActivityResultLauncher<String[]> mPermissionResult = registerForActivityResult(
        new ActivityResultContracts.RequestMultiplePermissions(),
        new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    Log.wtf(TAG, entry.getKey() + " " + entry.getValue());
                    startexample();
                }
            }
        });

    //until this runs as api31, don't know if needs scan or connect, so just leaving it.
    void checkpermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                mPermissionResult.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION});
                logthis("Android 12: Asking for  have permissions ");
            } else {
                logthis("Android 12: We have permissions ");
                startexample();
            }

        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            mPermissionResult.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
        } else {
            logthis("We have permissions ");
            startexample();
        }
    }

}