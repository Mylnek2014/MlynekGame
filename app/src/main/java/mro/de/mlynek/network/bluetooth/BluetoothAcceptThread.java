package mro.de.mlynek.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by kfg on 9/1/14.
 */
public class BluetoothAcceptThread extends Thread {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mmServerSocket;
    private String NAME="My Application";
    private UUID MY_UUID= UUID.fromString("5e5ae04e-50ad-4604-89aa-db5c1f86bc92");

    public BluetoothAcceptThread(BluetoothAdapter adapter) {
        if(adapter == null) {
            throw new IllegalArgumentException("No Bluetooth Adapter");
        }
        BluetoothServerSocket tmp = null;
        mBluetoothAdapter = adapter;
        try {
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    @Override
    public void run() {
       BluetoothSocket socket = null;
       while (true) {
           try {
               socket = mmServerSocket.accept();
           } catch (IOException e) {
               break;
           }

           if(socket != null) {
               //Send data
               //manageConnectedSocket(socket);
               try {
                   mmServerSocket.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               break;
           }
       }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {

        }
    }
}
