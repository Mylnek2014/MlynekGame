package mro.de.mlynek.network.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;

import java.util.Set;

/**
 * Created by kfg on 9/1/14.
 */
public class BluetoothDeviceManager {
    private ArrayAdapter<String> mArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private Set<String> addresses;
    //FIXME: Move to seperate Class?
    private BluetoothAcceptThread mServer;
    private BluetoothConnectThread mClient;

    public BluetoothDeviceManager(Activity activity, BluetoothAdapter adapter, ArrayAdapter<String> arrayAdapter) {
        if(activity == null) {
            throw new IllegalArgumentException(("No Activity"));
        }
        if(adapter == null) {
            throw new IllegalArgumentException("No Bluetooth Adapter");
        }
        mActivity = activity;
        mBluetoothAdapter = adapter;
        mArrayAdapter = arrayAdapter;
        mServer = new BluetoothAcceptThread(mBluetoothAdapter);
        mServer.start();
        mClient = null;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!addresses.contains(device.getAddress())) {
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    addresses.add(device.getAddress());
                }
            }
        }
    };

    public void searchDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                addresses.add(device.getAddress());
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mActivity.registerReceiver(mReceiver, filter);
    }

    public void connect(int id) {
        // Get Bluetooth device from id
        //mClient = new BluetoothConnectThread(mBluetoothAdapter, device);
        //FIXME Stop server here or when the connection succeeds?
    }

    public void setDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        mActivity.startActivity(discoverableIntent);
    }

    public void close() {
        try {
            mActivity.unregisterReceiver(mReceiver);
        } catch(IllegalArgumentException iae) {

        }
        if(mServer != null) {
            mServer.cancel();
            mServer = null;
        }
    }
}
