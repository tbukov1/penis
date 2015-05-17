package com.example.sylock;

import java.lang.reflect.Method;
import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
 
 private static final int REQUEST_ENABLE_BT = 1;
 
    ListView listDevicesFound;
 Button btnScanDevice;
 TextView stateBluetooth;
 BluetoothAdapter bluetoothAdapter;
 
 ArrayAdapter<String> btArrayAdapter;
 
 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnScanDevice = (Button)findViewById(R.id.scandevice);
        
        stateBluetooth = (TextView)findViewById(R.id.bluetoothstate);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        listDevicesFound = (ListView)findViewById(R.id.devicesfound);
        listDevicesFound.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String txt = ((TextView)view).getText().toString();
				Toast.makeText(getApplicationContext(),
          				txt, Toast.LENGTH_SHORT).show();
				try {
					pairDevice(bluetoothDevices.get(txt));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			}
		});
        btArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1);
        listDevicesFound.setAdapter(btArrayAdapter);
        
        CheckBlueToothState();
        
        btnScanDevice.setOnClickListener(btnScanDeviceOnClickListener);

        registerReceiver(ActionFoundReceiver, 
          new IntentFilter(BluetoothDevice.ACTION_FOUND));
        
        IntentFilter iFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    	this.registerReceiver(mReceiver, iFilter);
        
    }
    
    public HashMap<String, BluetoothDevice> bluetoothDevices = new HashMap<String, BluetoothDevice>();
    
    @Override
 protected void onDestroy() {
  // TODO Auto-generated method stub
  super.onDestroy();
  unregisterReceiver(ActionFoundReceiver);
 }

    
    public boolean pairDevice(BluetoothDevice btDevice)  
    	    throws Exception  
    	    { 
    	
    	        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
    	        Method createBondMethod = class1.getMethod("createBond");  
    	        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);  
    	        
    	        Log.i("PAIRING", "TEST TEST "+returnValue);
    	        if(!returnValue){
    	        	Intent srv = new Intent(this, MyService.class);
        	        srv.putExtra("Server", btDevice.getAddress());
        	        startService(srv);	
    	        }
    	        
    	        
    	        
    	        return returnValue.booleanValue();  
    	        
    	    }
    
    

    
    private int currentPosition;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    
	    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
		int prevBondState = intent.getIntExtra(
			BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
		int bondState = intent.getIntExtra(
			BluetoothDevice.EXTRA_BOND_STATE, -1);

		
		
		if (prevBondState == BluetoothDevice.BOND_BONDING
			&& bondState == BluetoothDevice.BOND_BONDED) {
		    BluetoothDevice device = intent
			    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    
		    Intent service = new Intent(context, MyService.class);
		    service.putExtra("Server", device.getAddress());
		    context.startService(service);
		    
		}
	    }
	}
    };

    
    
 private void CheckBlueToothState(){
     if (bluetoothAdapter == null){
         stateBluetooth.setText("Bluetooth NOT support");
        }else{
         if (bluetoothAdapter.isEnabled()){
          if(bluetoothAdapter.isDiscovering()){
           stateBluetooth.setText("Bluetooth is currently in device discovery process.");
          }else{
           stateBluetooth.setText("Bluetooth is Enabled.");
           btnScanDevice.setEnabled(true);
          }
         }else{
          stateBluetooth.setText("Bluetooth is NOT Enabled!");
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
             startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
         }
        }
    }
    
    private Button.OnClickListener btnScanDeviceOnClickListener
    = new Button.OnClickListener(){

  @Override
  public void onClick(View arg0) {
   // TODO Auto-generated method stub
   btArrayAdapter.clear();
   bluetoothAdapter.startDiscovery();
  }};

 @Override
 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  // TODO Auto-generated method stub
  if(requestCode == REQUEST_ENABLE_BT){
   CheckBlueToothState();
  }
 }
    
 private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver(){

  @Override
  public void onReceive(Context context, Intent intent) {
   // TODO Auto-generated method stub
   String action = intent.getAction();
   if(BluetoothDevice.ACTION_FOUND.equals(action)) {
             BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
             bluetoothDevices.put(device.getName(), device);
             btArrayAdapter.add(device.getName());
             btArrayAdapter.notifyDataSetChanged();
         }
  }};
    
}