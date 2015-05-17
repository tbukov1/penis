package com.example.sylock;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.content.BroadcastReceiver;

public class MyService extends Service{

	private BluetoothAdapter adapter;
	private OutputStream output;
	private InputStream input;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	BluetoothDevice server;
	String address;
	
	DataOutputStream out;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null) {
		    //TODO do something useful
			adapter = BluetoothAdapter.getDefaultAdapter();
			Set<BluetoothDevice> devices = adapter.getBondedDevices();
			address = intent.getStringExtra("Server");
			
	        Log.i("PAIRING", "TEST TEST 222");

			
			for(BluetoothDevice d : devices) {
				if(d.getAddress().equals(address)) {
					server = d;
					break;
				}
			}

			ParcelUuid[] uuids = server.getUuids();

			for(ParcelUuid u : uuids) {
				Log.i("UUID", u.getUuid().toString() + " -- " + u.toString() );
			}
			
			try {
			
				Log.i("RESULT", uuids[0].getUuid().toString());
				UUID u = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
				
				BluetoothSocket sck = server.createRfcommSocketToServiceRecord(u);
				sck.connect();
				output = sck.getOutputStream();
				//out = new DataOutputStream(sck.getOutputStream());
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			BroadcastReceiver ttr = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					String signal = "" + intent.getShortExtra(server.EXTRA_RSSI, Short.MIN_VALUE);
					
					Log.i("SIGNAL", signal);
					
					try {
						output.write(signal.getBytes());
						//out.write(signal.getBytes());
						//out.flush();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
			registerReceiver(ttr, new IntentFilter(BluetoothDevice.ACTION_FOUND));
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					adapter.startDiscovery();
				}
			}, 10000, 10000);
			
		}
		
	    return Service.START_NOT_STICKY;
    }
	
}
