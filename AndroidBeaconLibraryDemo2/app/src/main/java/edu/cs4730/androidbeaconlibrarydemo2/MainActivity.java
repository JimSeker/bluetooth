package edu.cs4730.androidbeaconlibrarydemo2;

import android.Manifest;
import android.content.Context;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    HomeFragment homeFrag;
    RangeFragment rangeFrag;
    myViewModel mViewModel;
    Region myRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup the view model first.
        mViewModel = new ViewModelProvider(this).get(myViewModel.class);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.navigation_home) {
                        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, homeFrag)
                            .commit();
                        return true;
                    } else if (item.getItemId() == R.id.navigation_notifications) {
                        getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, rangeFrag)
                            .commit();
                        return true;
                    }
                    return false;
                }

            }
        );


        logthis("App Starting", 0);
        //check for permissions and start the beacons.
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //added eddystone, since I'm moving from google's beacons to altbeacon.  RedBeacon can broadcast both.
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));


        homeFrag = new HomeFragment();
        rangeFrag = new RangeFragment();
        checkpermissions();

        getSupportFragmentManager().beginTransaction()
            .add(R.id.container, homeFrag)
            .commit();

    }

    /**
     * helper method to send strings to both the logcat and to a textview call logger.
     */
    private void logthis(String item, int which) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, item);
                if (which == 1) {
                    mViewModel.setItem(item);
                } else if (which == 2) {
                    if (rangeFrag != null) {
                        rangeFrag.logthis(item);
                    }
                }
            }
        });

    }


    /**
     * Setup and start the observers here.
     */
    void startexample() {
        logthis("Starting up!", 1);
        // Set up a Live Data observer so this Activity can get monitoring callbacks
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        myRegion = new Region("myMonitoringUniqueId", null, null, null);
        //first the monitor

        beaconManager.getRegionViewModel(myRegion).getRegionState().observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer state) {
                    logthis("monitor change", 1);
                    if (state == MonitorNotifier.INSIDE) {
                        logthis("I can see at least one or more beacons.", 1);
                        // all the region variables appear to be null, because I set them that way?
                        logthis("the region ID is " + myRegion.getUniqueId(), 1);
                    } else {
                        logthis("I no longer see any beacons", 1);

                    }
                }
            }


        );
        beaconManager.startMonitoring(myRegion);
        logthis("beacon monitor observer has been added.", 1);


        //now the ranged information.
        logthis("beacon range observer has been added.", 2);
        beaconManager.getRegionViewModel(myRegion).getRangedBeacons().observe(this, new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(Collection<Beacon> beacons) {
                logthis("didRangeBeaconsInRegion called with beacon count:  " + beacons.size(), 2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setMlist(beacons);
                    }
                });
            }
        });
        beaconManager.startRangingBeacons(myRegion);
    }


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
                logthis("Android 12: Asking for  have permissions ", 1);
            } else {
                logthis("Android 12: We have permissions ", 1);
                startexample();
            }

        } else if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            mPermissionResult.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.ACCESS_BACKGROUND_LOCATION});
        } else {
            logthis("We have permissions ", 1);
            startexample();
        }
    }


    @Override
    protected void onStop() {
        beaconManager.stopMonitoring( myRegion );
        beaconManager.stopRangingBeacons( myRegion );
        super.onStop();
    }



}