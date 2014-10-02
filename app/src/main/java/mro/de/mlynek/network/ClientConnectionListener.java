package mro.de.mlynek.network;

/**
 * Created by kfg on 9/19/14.
 */
public interface ClientConnectionListener {
    public void onClientConnect();
    public void onClientConnectionFailed();
}
