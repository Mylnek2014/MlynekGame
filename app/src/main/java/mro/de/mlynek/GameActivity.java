package mro.de.mlynek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import mro.de.mlynek.network.ClientConnection;
import mro.de.mlynek.network.ClientConnectionListener;
import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.ServerConnectionListener;

/**
 * Created by Sony on 09.09.2014.
 */
public class GameActivity extends FragmentActivity implements ServerConnectionListener, ClientConnectionListener {
    private GameView gameView;
    private ClientConnection clientConn;
    private ServerConnection servConn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(R.layout.activity_game);
        clientConn = ClientConnection.getConnection();
        if(clientConn == null || !clientConn.isConnected()) {
            servConn = ServerConnection.getConnection();
            if(servConn != null) {
                servConn.setConnectionListener(this);
            }
            if(clientConn != null) {
                clientConn.close();
                clientConn = null;
            }
        } else {
            clientConn.setListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        if(clientConn != null) {
            clientConn.close();
            clientConn = null;
        }
        if(servConn != null) {
            servConn.close();
            servConn = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConnect() {
        //TODO Do something when connected (unlock game etc.)
        Log.i("Info", "Client connected");
    }

    @Override
    public void onClientConnect() {
        Log.i("Info", "Client connected");
    }

    @Override
    public void onClientConnectionFailed() {
        Log.i("Info", "Connection failed");
    }

    public void gameOver()
    {
        Intent intent = new Intent(GameActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}
