package edu.cs4730.btDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class blueToothDemo extends Activity implements Button.OnClickListener {
	BluetoothAdapter mBluetoothAdapter =null;
	BluetoothDevice device;
	private static final int REQUEST_ENABLE_BT = 2;  
	private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final String NAME = "BluetoothDemo";
	BluetoothDevice remoteDevice;
	TextView output;
	Button btnServer, btnScan, btnClient;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        output = (TextView) findViewById(R.id.output);
        output.append("\n");
        btnServer = (Button) findViewById(R.id.btnServer);
        btnServer.setOnClickListener(this);
        btnClient = (Button) findViewById(R.id.btnClient);
        btnClient.setOnClickListener(this);
        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
    	startbt();
    }
	@Override
	public void onClick(View v) {
		if ( (Button)v == btnServer) {
			startServer();
		} else if ( (Button)v == btnClient) {
			if (device != null) {
				output.append("button client\n");
				startClient();
			}
		} else if ( (Button)v == btnScan) {
			output.append("Started query\n");
			querypaired();
		}

	}
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	output.append(msg.getData().getString("msg"));
        }

    };
    public void mkmsg(String str) {
		//handler junk, because thread can't update screen!
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("msg", str);
		msg.setData(b);
	    handler.sendMessage(msg);
    }
    
    
    public boolean startbt() {
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if (mBluetoothAdapter == null) {
    	    // Device does not support Bluetooth
    		return false;
    	}
    	//make sure bluetooth is enabled.
    	 if (!mBluetoothAdapter.isEnabled()) {
    	     Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	     startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	 }
    	 return true;
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
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Choose Bluetooth:");
    		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    				dialog.dismiss();
    				if (item >= 0 && item <blueDev.length) { 
    					device = blueDev[item];
    				}

    		    }
    		});
    		AlertDialog alert = builder.create();
    		alert.show();
    	}
    }
    public void startServer() {
    	new Thread(new AcceptThread()).start();
    	
    }
    public void startClient() {
    	if (device != null) {   	
    		new Thread(new ConnectThread(device)).start();
    	}
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
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                mkmsg("Failed to start server\n");
            }
            mmServerSocket = tmp;
        }
        public void run() {
        	mkmsg("waiting on accept");
        	BluetoothSocket socket = null;
        	try {
        		// This is a blocking call and will only return on a
        		// successful connection or an exception
        		socket = mmServerSocket.accept();
        	} catch (IOException e) {
        		mkmsg("Failed to accept\n");
        	}

        	// If a connection was accepted
        	if (socket != null) {
        		mkmsg("Connection made\n");
        		mkmsg("Remote device address: "+socket.getRemoteDevice().getAddress().toString()+"\n");
        		//Note this is copied from the TCPdemo code.
        		try {
        			mkmsg("Attempting to receive a message ...\n"); 
        			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
        			String str = in.readLine();
        			mkmsg("received a message:\n" + str+"\n");

        			PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
        			mkmsg("Attempting to send message ...\n");                 
        			out.println("Reponse from Bluetooth Demo Server");
        			out.flush();
        			mkmsg("Message sent...\n");

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
        	mkmsg("Server ending \n");
        }

        public void cancel() {
        	try {
        		mmServerSocket.close();
        	} catch (IOException e) {

        	}
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
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                mkmsg("Client connection failed: "+e.getMessage().toString()+"\n");
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
                    mkmsg("unable to close() socket during connection failure: "+e2.getMessage().toString()+"\n");
                    socket = null;
                }
                // Start the service over to restart listening mode   
            }
           	// If a connection was accepted
        	if (socket != null) {
        		mkmsg("Connection made\n");
        		mkmsg("Remote device address: "+socket.getRemoteDevice().getAddress().toString()+"\n");
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
               mkmsg( "close() of connect socket failed: "+e.getMessage().toString()+"\n");
            }
        }
    }

}