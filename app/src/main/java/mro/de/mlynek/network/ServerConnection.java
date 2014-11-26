package mro.de.mlynek.network;

import android.content.BroadcastReceiver;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

/**
 * Created by kfg on 9/19/14.
 */
public class ServerConnection implements Connection {
    private GenericServer gs;
    //private BluetoothConnectedThread;
    //private boolean isBluetooth;
    // Wifi P2p State
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private WifiP2pDnsSdServiceInfo mServiceInfo;

    //TODO: Check if Port is already used
    public static ServerConnection createConnection(int port) {
        //if(!portUsed) {
            Log.i("ServerConnection", "Server connection created on Port: " + port);
            return new ServerConnection(new GenericServer(port));
        /*} else {
            throw new IllegalArgumentException("ServerConnection already created");
        }*/
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
        if(gs == null) {
            Log.e("ServerConnection", "GenericServer ist null");
            return "";
        }
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

    public void setWifiServiceState(BroadcastReceiver receiver, WifiP2pDnsSdServiceInfo serviceInfo) {
        mReceiver = receiver;
        mServiceInfo = serviceInfo;
    }

    public BroadcastReceiver getWifiReceiver() {
        return mReceiver;
    }

    public WifiP2pDnsSdServiceInfo getServiceInfo() {
        return mServiceInfo;
    }

    public void setConnectionListener(ServerConnectionListener scl) {
        if(gs != null) {
            gs.setConnectionListener(scl);
        }
    }

    public void close() {
        Log.i("ServerConnection", "Server connection closed.");
        if (gs != null) {
            gs.close();
            gs = null;
        }
        //FIXME Try closing wifi stuff here?
        mReceiver = null;
        mServiceInfo = null;
    }
}
