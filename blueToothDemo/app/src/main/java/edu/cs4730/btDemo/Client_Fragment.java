package edu.cs4730.btDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class Client_Fragment extends Fragment {
	String TAG = "client";
	TextView output;
	Button btn_start, btn_device;
	BluetoothAdapter mBluetoothAdapter =null;
	BluetoothDevice device;
	BluetoothDevice remoteDevice;
	
	public Client_Fragment() {
		// Required empty public constructor
	}


    private Handler handler =  new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
        	output.append(msg.getData().getString("msg"));
            return true;
        }

    });
    public void mkmsg(String str) {
		//handler junk, because thread can't update screen!
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
	    handler.sendMessage(msg);
    }
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View myView = inflater.inflate(R.layout.fragment_client, container, false);

		//output textview
		output = (TextView) myView.findViewById(R.id.ct_output);
		btn_device = (Button) myView.findViewById(R.id.which_device);
		btn_device.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				querypaired();
				
			}
		});
		btn_start = (Button) myView.findViewById(R.id.start_client);
		btn_start.setEnabled(false);
		btn_start.setOnClickListener( new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				output.append("Starting client\n");
				 startClient();
			}
		});
		//setup the bluetooth adapter.
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			output.append("No bluetooth device.\n");
			btn_start.setEnabled(false);
			btn_device.setEnabled(false);
		}
		Log.v(TAG, "bluetooth");

		return myView;
	}

	  
    public void querypaired() {
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	// If there are paired devices
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    		output.append("at least 1 paired device\n");
    		final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices.size()];
    		String[] items = new String[blueDev.length];
    		int i =0;
    		for (BluetoothDevice devicel : pairedDevices) {
    			blueDev[i] = devicel;
    	    	items[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
    	    	output.append("Device: "+items[i]+"\n");
    	    	//mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    	    	i++;
    	    }
    		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    		builder.setTitle("Choose Bluetooth:");
    		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    				dialog.dismiss();
    				if (item >= 0 && item <blueDev.length) { 
    					device = blueDev[item];
    					btn_device.setText("device: "+blueDev[item].getName());
    					btn_start.setEnabled(true);
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
                mkmsg("Client connection failed: "+e.getMessage()+"\n");
            }
            socket = tmp;
 
        }

        public void run() {
            mkmsg("Client running\n");
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                socket.connect();
            } catch (IOException e) {
            	mkmsg("Connect failed\n");
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e2) {
                    mkmsg("unable to close() socket during connection failure: "+e2.getMessage()+"\n");
                    socket = null;
                }
                // Start the service over to restart listening mode   
            }
           	// If a connection was accepted
        	if (socket != null) {
        		mkmsg("Connection made\n");
        		mkmsg("Remote device address: "+socket.getRemoteDevice().getAddress()+"\n");
        		//Note this is copied from the TCPdemo code.
        		try {
        			PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
        			mkmsg("Attempting to send message ...\n");                 
        			out.println("hello from Bluetooth Demo Client");
        			out.flush();
        			mkmsg("Message sent...\n");
        			
        			mkmsg("Attempting to receive a message ...\n"); 
        			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
        			String str = in.readLine();
        			mkmsg("received a message:\n" + str+"\n");



        			mkmsg("We are done, closing connection\n");
        		} catch(Exception e) {
        			mkmsg("Error happened sending/receiving\n");

        		} finally {
        			try {
						socket.close();
					} catch (IOException e) {
						mkmsg("Unable to close socket"+e.getMessage()+"\n");
					}
        		}
        	} else {
        		mkmsg("Made connection, but socket is null\n");
        	}
        	mkmsg("Client ending \n");

        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
               mkmsg( "close() of connect socket failed: "+e.getMessage() +"\n");
            }
        }
    }

}
