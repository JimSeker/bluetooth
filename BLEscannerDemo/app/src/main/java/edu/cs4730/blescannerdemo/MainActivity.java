package edu.cs4730.blescannerdemo;

import android.bluetooth.BluetoothDevice;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//https://arnhem.luminis.eu/creating-a-bluetooth-low-energy-central-implementation-on-android/
public class MainActivity extends AppCompatActivity implements Help_Fragment.OnFragmentInteractionListener,
    ListenerFragment.OnFragmentInteractionListener {

    FragmentManager fragmentManager;
    String TAG = "MainActivity";
    ListenerFragment listfrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listfrag = new ListenerFragment();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frag_container, new Help_Fragment()).commit();


    }

    @Override
    public void onButtonSelected(int id) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        if (id == 2) { //client
            transaction.replace(R.id.frag_container, listfrag);
        }
        // and add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onDeviceSelected(BluetoothDevice d) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        GattFragment gf = new GattFragment();
        transaction.replace(R.id.frag_container, gf);
        gf.setDevice(d);

        // and add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}
