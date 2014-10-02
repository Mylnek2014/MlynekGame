package mro.de.mlynek.network;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by kfg on 9/19/14.
 */
public class ServerConnection implements Connection {
    private GenericServer gs;
    //private BluetoothConnectedThread;
    //private boolean isBluetooth;
    private static ServerConnection self = null;
    private static int refCount = 0;
    private WifiP2pManager.Channel mChannel;

    public static ServerConnection createConnection(int port) {
        if(self == null) {
            refCount++;
            Log.i("INFO", "Server connection created. RefCount: " + refCount);
            self = new ServerConnection(new GenericServer(port));
            return self;
        } else {
            throw new IllegalArgumentException("ServerConnection already created");
        }
    }

    public static ServerConnection getConnection() {
        if(self != null) {
            refCount++;
            Log.i("INFO", "Server connection requested. RefCount: "+refCount);
        }
        return self;
    }

    private ServerConnection(GenericServer gs) {
        if(gs == null) {
            throw new IllegalArgumentException("Generic Server is null");
        }
        this.mChannel = null;
        this.gs = gs;
    }

    public void start() {
        gs.start();
    }

    public String read() {
        byte[] tmp = gs.recv();
        if(tmp == null) {
            return new String("");
        }
        return new String(tmp);
    }

    public boolean write(String send) {
            return gs.send(send.getBytes());
    }

    @Override
    public boolean isConnected() {
        if(gs == null) {
            return false;
        }
        return gs.isConnected();
    }

    public void setWifiP2pChannel(WifiP2pManager.Channel channel) {
        mChannel = channel;
    }

    public WifiP2pManager.Channel getWifiP2pChannel() {
        return mChannel;
    }

    public void setConnectionListener(ServerConnectionListener scl) {
        if(gs != null) {
            gs.setConnectionListener(scl);
        }
    }

    public void close() {
        refCount--;
        Log.i("INFO", "Server connection closed. RefCount: "+refCount);
        if(refCount == 0) {
            if(gs != null) {
                gs.close();
                gs = null;
            }
            self = null;
        }
    }
}
