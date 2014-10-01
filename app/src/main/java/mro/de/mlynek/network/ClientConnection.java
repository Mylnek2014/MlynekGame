package mro.de.mlynek.network;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by kfg on 9/19/14.
 */
public class ClientConnection implements Connection, ClientConnectionListener {
    private ThreadClient tc;
    //private BluetoothConnectedThread;
    //private boolean isBluetooth;
    private static ClientConnection self = null;
    private static int refCount = 0;
    private ClientConnectionListener mListener;
    private WifiP2pManager.Channel mChannel;

    public static ClientConnection createConnection(String host, int port, ClientConnectionListener listener) {
        if(self == null) {
            refCount++;
            Log.i("INFO", "Client connection created. RefCount: "+refCount);
            self = new ClientConnection(new ThreadClient(host, port));
            self.setListener(listener);
            return self;
        } else {
            throw new IllegalArgumentException("ClientConnection already created");
        }
    }

    public static ClientConnection getConnection() {
        if(self != null) {
            refCount++;
            Log.i("INFO", "Client connection requested. RefCount: "+refCount);
        }
        return self;
    }

    private ClientConnection(ThreadClient tc) {
        if(tc == null) {
            throw new IllegalArgumentException("Thread Client is null");
        }
        this.tc = tc;
        this.mChannel = null;
        tc.setConnectionListener(this);
    }

    public void setListener(ClientConnectionListener listener) {
        this.mListener = listener;
    }

    public void start() {
        tc.start();
    }

    public String read() {
        byte[] tmp = tc.recv();
        if(tmp == null) {
            return new String("");
        }
        return new String(tmp);
    }

    public boolean write(String send) {
            return tc.send(send.getBytes());
    }

    @Override
    public boolean isConnected() {
        if(tc == null) {
            return false;
        }
        return tc.isConnected();
    }

    public void setWifiP2pChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public WifiP2pManager.Channel getWifiP2pChannel() {
        return mChannel;
    }

    public void close() {
        refCount--;
        Log.i("INFO", "Client connection closed. RefCount: "+refCount);
        if(refCount == 0) {
            if(tc != null) {
                tc.close();
                tc = null;
            }
            self = null;
        }
    }

    @Override
    public void onClientConnect() {
        if(mListener != null) {
            mListener.onClientConnect();
        }
    }

    @Override
    public void onClientConnectionFailed() {
        if(mListener != null) {
            mListener.onClientConnectionFailed();
        }
    }
}
