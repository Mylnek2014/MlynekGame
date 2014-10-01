package mro.de.mlynek;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.ServerConnectionListener;

/**
 * Created by Sony on 09.09.2014.
 */
public class GameActivity extends FragmentActivity implements ServerConnectionListener {
    private GameView gameView;
    private ServerConnection servConn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(R.layout.activity_game);
        servConn = ServerConnection.getConnection();
        if(servConn != null) {
            servConn.setConnectionListener(this);
        }
    }

    @Override
    protected void onDestroy() {
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
}
