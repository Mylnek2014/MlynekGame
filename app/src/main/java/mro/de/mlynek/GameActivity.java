package mro.de.mlynek;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.Timer;

import mro.de.mlynek.network.Connection;
import mro.de.mlynek.network.ClientConnection;
import mro.de.mlynek.network.ClientConnectionListener;
import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.ServerConnectionListener;
import mro.de.mlynek.network.wifidirect.WifiDeviceManager;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

/**
 * Created by Sony on 09.09.2014.
 */
public class GameActivity extends FragmentActivity implements ServerConnectionListener, ClientConnectionListener, View.OnClickListener {
    private GameView gameView;
    private Dialog m_dialog;
    private ImageView m_teamImage;
    private ImageButton m_newTry;
    private ImageButton m_mainMenu;
    private Connection conn;
    private Timer timer;
    private boolean localGame;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        m_dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        m_dialog.setContentView(R.layout.gameoverscreen);
        m_teamImage = (ImageView) m_dialog.findViewById(R.id.teamImage);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        m_newTry = (ImageButton) m_dialog.findViewById(R.id.bNewTry);
        m_newTry.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.replaybtn, size.x / 2, size.y / 2));
        m_newTry.setOnClickListener(this);
        m_mainMenu = (ImageButton) m_dialog.findViewById(R.id.bMainMenu);
        m_mainMenu.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.menubtn, size.x / 2, size.y / 2));
        m_mainMenu.setOnClickListener(this);
        m_dialog.hide();

        localGame = getIntent().getBooleanExtra("localGame", false);
        ClientConnection clientConn = null;
        ServerConnection servConn = null;
        conn = null;
        if(!localGame) {
            clientConn = ((MlynekApplication)getApplicationContext()).getClientConnection();
            if (clientConn == null || !clientConn.isConnected()) {
                servConn = ((MlynekApplication)getApplicationContext()).getServerConnection();
                if (servConn != null) {
                    servConn.setConnectionListener(this);
                    conn = servConn;
                }
                if (clientConn != null) {
                    ((MlynekApplication)getApplicationContext()).disconnectClientConnection();
                    clientConn = null;
                }
            } else {
                clientConn.setListener(this);
                conn = clientConn;
            }
        }
        if(!localGame && conn == null) {
            Log.d("Debug", "Not a local Game but connection is null");
            localGame = true;
        }
        setContentView(R.layout.activity_game);
    }

    public Connection getConnection() {
        if(conn != null) {
            return conn;
        }
        return null;
    }

    // TODO: Cleanup
    public void setView(GameView v) {
        if(v == null) {
            Log.d("DEBUG", "Failed to get gameView");
            return;
        }
        gameView = v;
        if(!localGame) {
            timer = new Timer();
            timer.schedule(new RecvTask(conn, gameView), 200, 200);
        }

    }

    public boolean isLocalGame() {
        return localGame;
    }

    @Override
    protected void onDestroy() {
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        if(conn != null) {
            conn = null;
        }
        ((MlynekApplication)getApplicationContext()).disconnectClientConnection();
        if(((MlynekApplication)getApplicationContext()).getServerConnection() != null) {
            WifiDeviceManager.disconnect(this, ((MlynekApplication)getApplicationContext()).getServerConnection().getWifiP2pChannel());
            ((MlynekApplication)getApplicationContext()).disconnectServerConnection();
        }
        super.onDestroy();
    }


    // Should only happen on reconnect anyway
    @Override
    public void onConnect() {
        //TODO Do something when connected (unlock game etc.)
        Log.i("Info", "Client connected");
    }

    @Override
    public void onDisconnect() {
        Log.d("Debug", "Client disconnected from Server (in GameActivity)");
    }

    @Override
    public void onClientConnect() {
        Log.i("Info", "Client connected");
    }

    @Override
    public void onClientConnectionFailed() {
        Log.i("Info", "Connection failed");
    }

    public void setTeamImage(Bitmap image)
    {
        m_teamImage.setImageBitmap(image);
    }

    public void gameOver()
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_dialog.show();
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bMainMenu:
                m_dialog.dismiss();
                Intent menuIntent = new Intent(GameActivity.this, MenuActivity.class);
                startActivity(menuIntent);
                finish();
                break;
            case R.id.bNewTry:
                Intent newGameScreen = new Intent(GameActivity.this, GameActivity.class);
                startActivity(newGameScreen);
                m_dialog.dismiss();
                finish();
        }

    }

    @Override
    public void onClientDisconnect() {
        Log.d("Debug", "Client(me) disconnected from Server (in GameActivity)");
    }
}
