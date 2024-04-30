package edu.cs4730.btDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


import edu.cs4730.btDemo.databinding.FragmentServerBinding;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("MissingPermission") //I really do check.
public class Server_Fragment extends Fragment {
    static final String TAG = "Server";
    BluetoothAdapter mBluetoothAdapter = null;
    FragmentServerBinding binding;

    public Server_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentServerBinding.inflate(inflater, container, false);


        binding.startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logthis("Starting server\n");
                startServer();
            }
        });


        //setup the bluetooth adapter.
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            logthis("No bluetooth device.\n");
            binding.startServer.setEnabled(false);
        }

        return binding.getRoot();
    }

    public void startServer() {
        new Thread(new AcceptThread()).start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MainActivity.NAME, MainActivity.MY_UUID);
            } catch (IOException e) {
                logthis("Failed to start server\n");
            }
            mmServerSocket = tmp;
        }

        public void run() {
            logthis("waiting on accept");
            BluetoothSocket socket = null;
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                logthis("Failed to accept\n");
            }

            // If a connection was accepted
            if (socket != null) {
                logthis("Connection made\n");
                logthis("Remote device address: " + socket.getRemoteDevice().getAddress() + "\n");
                //Note this is copied from the TCPdemo code.
                try {
                    logthis("Attempting to receive a message ...\n");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String str = in.readLine();
                    logthis("received a message:\n" + str + "\n");

                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    logthis("Attempting to send message ...\n");
                    out.println("Hi from Bluetooth Demo Server");
                    out.flush();
                    logthis("Message sent...\n");

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
            logthis("Server ending \n");
        }

        public void cancel() {
            try {
                mmServerSocket.close();
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
                binding.svOutput.append(item);
            }
        });
    }

}
