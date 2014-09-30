package mro.de.mlynek.network.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by kfg on 9/1/14.
 */
public class BluetoothConnectedThread extends Thread {
    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private String NAME="My Application";
    private UUID MY_UUID= UUID.fromString("5e5ae04e-50ad-4604-89aa-db5c1f86bc92");

    public BluetoothConnectedThread(BluetoothSocket socket) {
        if(socket == null) {
            throw new IllegalArgumentException("No Bluetooth Adapter");
        }
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while(true) {
            try {
                bytes = mmInStream.read(buffer);
                //Process Input
                //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget
            } catch(IOException e) {
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {

        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {

        }
    }
}
