package mro.de.mlynek.network;

/**
 * Created by kfg on 9/19/14.
 */
public interface Connection {
    public String read();
    public boolean write(String send);
    public boolean isConnected();
}
