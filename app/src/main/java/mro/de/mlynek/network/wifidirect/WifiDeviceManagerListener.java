package mro.de.mlynek.network.wifidirect;

/**
 * Created by kfg on 9/19/14.
 */
public interface WifiDeviceManagerListener {
    public void onWifiConnect();
    public void onWifiConnectFailed();
    public void onWifiDisconnect();
    public void onWifiClientConnect();
    public void onWifiClientConnectionFailed();
    public void onWifiClientDisconnect();
}
