package edu.cs4730.btDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import edu.cs4730.btDemo.databinding.FragmentClientBinding;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("MissingPermission") //I really do check.
public class Client_Fragment extends Fragment {
    static final String TAG = "client";
    FragmentClientBinding binding;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothDevice device;
    BluetoothDevice remoteDevice;

    public Client_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentClientBinding.inflate(inflater, container, false);

        binding.whichDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querypaired();

            }
        });

        binding.startClient.setEnabled(false);
        binding.startClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logthis("Starting client\n");
                startClient();
            }
        });
        //setup the bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            logthis("No bluetooth device.\n");
            binding.startClient.setEnabled(false);
            binding.whichDevice.setEnabled(false);
        }
        Log.v(TAG, "bluetooth");

        return binding.getRoot();
    }


    public void querypaired() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (!pairedDevices.isEmpty()) {
            // Loop through paired devices
            logthis("at least 1 paired device\n");
            final BluetoothDevice[] blueDev = new BluetoothDevice[pairedDevices.size()];
            String[] items = new String[blueDev.length];
            int i = 0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                items[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                logthis("Device: " + items[i] + "\n");
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                i++;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose Bluetooth:");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    if (item >= 0 && item < blueDev.length) {
                        device = blueDev[item];
                        binding.whichDevice.setText("device: " + blueDev[item].getName());
                        binding.startClient.setEnabled(true);
                    }

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }


    public void startClient() {
        if (device != null) {
            new Thread(new ConnectThread(device)).start();
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */

    private class ConnectThread extends Thread {
        private BluetoothSocket socket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MainActivity.MY_UUID);
            } catch (IOException e) {
                logthis("Client connection failed: " + e.getMessage() + "\n");
            }
            socket = tmp;

        }

        public void run() {
            logthis("Client running\n");
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();
            } catch (IOException e) {
                logthis("Connect failed\n");
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e2) {
                    logthis("unable to close() socket during connection failure: " + e2.getMessage() + "\n");
                    socket = null;
                }
                // Start the service over to restart listening mode   
            }
            // If a connection was accepted
            if (socket != null) {
                logthis("Connection made\n");
                logthis("Remote device address: " + socket.getRemoteDevice().getAddress() + "\n");
                //Note this is copied from the TCPdemo code.
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    logthis("Attempting to send message ...\n");
                    out.println("hello from Bluetooth Demo Client");
                    out.flush();
                    logthis("Message sent...\n");

                    logthis("Attempting to receive a message ...\n");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String str = in.readLine();
                    logthis("received a message:\n" + str + "\n");


                    logthis("We are done, closing connection\n");
                } catch (Exception e) {
                    logthis("Error happened sending/receiving\n");

                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logthis("Unable to close socket" + e.getMessage() + "\n");
                    }
                }
            } else {
                logthis("Made connection, but socket is null\n");
            }
            logthis("Client ending \n");

        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                logthis("close() of connect socket failed: " + e.getMessage() + "\n");
            }
        }
    }

    void logthis(String item) {
        Log.v(TAG, item);
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.ctOutput.append(item);
            }
        });
    }

}
