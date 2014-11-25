package mro.de.mlynek;

import android.app.Application;
import android.util.Log;

import mro.de.mlynek.network.ClientConnection;
import mro.de.mlynek.network.ServerConnection;

/**
 * Created by kfg on 11/25/14.
 */
public class MlynekApplication extends Application {
    private ClientConnection clientConnection;
    private ServerConnection serverConnection;

    public void setClientConnection(ClientConnection clientConnection) {
        if(this.clientConnection != null) {
            Log.e("MlynekApplication", "Tried to change clientConnection without disconnecting first");
            return;
        }
        this.clientConnection = clientConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        if(this.serverConnection != null) {
            Log.e("MlynekApplication", "Tried to change serverConnection without disconnecting first");
            return;
        }
        this.serverConnection = serverConnection;
    }

    public ClientConnection getClientConnection() {
        return this.clientConnection;
    }

    public ServerConnection getServerConnection() {
        return this.serverConnection;
    }

    public void disconnectServerConnection() {
        if(this.serverConnection != null) {
            this.serverConnection.close();
            this.serverConnection = null;
        }
    }

    public void disconnectClientConnection() {
        if(this.clientConnection != null) {
            this.clientConnection.close();
            this.clientConnection = null;
        }
    }

    @Override
    public void onTerminate() {
        disconnectClientConnection();
        disconnectServerConnection();
        super.onTerminate();
    }

    @Override
    protected void finalize() throws Throwable {
        disconnectClientConnection();
        disconnectServerConnection();
        super.finalize();
    }
}
