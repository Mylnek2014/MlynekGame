package mro.de.mlynek.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by kfg on 9/1/14.
 */
public class BluetoothConnectThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private String NAME="My Application";
    private UUID MY_UUID= UUID.fromString("5e5ae04e-50ad-4604-89aa-db5c1f86bc92");

    public BluetoothConnectThread(BluetoothAdapter adapter, BluetoothDevice device) {
        if(adapter == null) {
            throw new IllegalArgumentException("No Bluetooth Adapter");
        }
        if(device == null) {
            throw new IllegalArgumentException("No Device");
        }
        BluetoothSocket tmp = null;
        mBluetoothAdapter = adapter;
        mmDevice = device;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    @Override
    public void run() {
       mBluetoothAdapter.cancelDiscovery();
       try {
           mmSocket.connect();
       } catch (IOException connectException) {
           try {
               mmSocket.close();
           } catch (IOException closeException) {

           }
           return;
       }

        //manageConnectedSocket(mmSocket);
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {

        }
    }
}
