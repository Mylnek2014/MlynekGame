package mro.de.mlynek;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.Timer;
import java.util.TimerTask;

import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.ServerConnectionListener;
import mro.de.mlynek.network.wifidirect.WifiDeviceManager;
import mro.de.mlynek.network.wifidirect.WifiDeviceManagerListener;

/**
 * Created by Sony on 08.09.2014.
 */
public class MenuActivity extends FragmentActivity implements View.OnClickListener, ServerConnectionListener, WifiDeviceManagerListener {
    private WifiDeviceManager wifidevmanager;
    private ServerConnection servConn;
    private AlertDialog connectionWait;
    private AlertDialog gameTypeDialog;
    private boolean localGame;
    private Timer refreshButtonTimer;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        localGame = true;
        wifidevmanager = new WifiDeviceManager(this);
        wifidevmanager.setListener(this);
        setContentView(R.layout.activity_menu);
        AlertDialog.Builder gameTypeDialogBuilder = new AlertDialog.Builder(this);
        gameTypeDialogBuilder.setMessage("Spielart");
        gameTypeDialogBuilder.setNegativeButton("Lokales Spiel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("INFO", "Lokales Spiel Button");
                localGame = true;
                startGameActivity();
                gameTypeDialog.dismiss();
            }
        });
        gameTypeDialogBuilder.setPositiveButton("Netzwerk Spiel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("INFO", "Netzwerk Spiel Button");
                localGame = false;
                startWifiServer();
                connectionWait.show();
                gameTypeDialog.dismiss();
            }
        });
        gameTypeDialog = gameTypeDialogBuilder.create();
        AlertDialog.Builder connectionWaitBuilder = new AlertDialog.Builder(this);
        connectionWaitBuilder.setMessage("Warte auf Verbindung");
        connectionWaitBuilder.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d("INFO", "Abbruch Button");
                if(!wifidevmanager.isWifiEnabled()) {
                    servConn.close();
                } else {
                    wifidevmanager.setDiscoverable(false, "Młyneck");
                    wifidevmanager.stopWifiServer();
                }
            }
        });
        connectionWaitBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("INFO", "Connection Wait Dialog Cancelled");
                if(!wifidevmanager.isWifiEnabled()) {
                    servConn.close();
                } else {
                    wifidevmanager.setDiscoverable(false, "Młyneck");
                    wifidevmanager.stopWifiServer();
                }
            }
        });
        connectionWait = connectionWaitBuilder.create();
    }

    public WifiDeviceManager getWifiDeviceManager() {
        return wifidevmanager;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.play)
        {
            gameTypeDialog.show();
        }
        if(view.getId() == R.id.refresh) {
            wifidevmanager.searchDevices();
            wifidevmanager.getAdapter().insert("Test", 0);
            wifidevmanager.getAdapter().notifyDataSetChanged();
            ImageButton b = (ImageButton)view;
            b.setEnabled(false);
            refreshButtonTimer = new Timer();
            refreshButtonTimer.schedule(new RefreshButtonEnableTask(b), 2000);
        }
    }

    protected void startWifiServer() {
        // For testing in the Emulator
        if(!wifidevmanager.isWifiEnabled()) {
            if(servConn != null) {
                servConn.close();
                servConn = null;
            }
            servConn = ServerConnection.createConnection(3333);
            servConn.setConnectionListener(this);
            servConn.start();
        } else {
            wifidevmanager.setDiscoverable(true, "Młyneck");
        }
    }

    @Override
    protected void onDestroy() {
        if(refreshButtonTimer != null) {
            refreshButtonTimer.cancel();
            refreshButtonTimer = null;
        }
        if(connectionWait != null) {
            if(connectionWait.isShowing()) {
                connectionWait.dismiss();
            }
            connectionWait = null;
        }
        if(gameTypeDialog != null) {
            if (gameTypeDialog.isShowing()) {
                gameTypeDialog.dismiss();
            }
            gameTypeDialog = null;
        }
        if(wifidevmanager != null) {
            wifidevmanager.close();
            wifidevmanager = null;
        }
        if(servConn != null) {
            servConn.close();
            servConn = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConnect() {
        startGameActivity();
    }

    @Override
    public void onDisconnect() {
        Log.d("Info", "Client disconnected from Server or Server was closed");
    }

    @Override
    public void onWifiConnect() {
        startGameActivity();
    }

    @Override
    public void onWifiConnectFailed() {
        Log.d("INFO","onWifiConnectFailed");
    }

    @Override
    public void onWifiDisconnect() {
        Log.d("Info", "Client disconnected from Server (Wifi)");
    }

    @Override
    public void onWifiClientConnect() {
        onClientConnect();
    }

    // Wird vom GameFragment oder WifiClientConnect aufgerufen
    public void onClientConnect() {
        // Wenn man als Client connected möchte man immer ein Netzwerkspiel
        localGame = false;
        startGameActivity();
    }

    @Override
    public void onWifiClientConnectionFailed() {
        Log.d("INFO","onWifiClientConnectFailed");
    }

    @Override
    public void onWifiClientDisconnect() {
        Log.d("Info", "Client(me) disconnected from Server");
    }

    public void startGameActivity() {
        Intent gameIntent = new Intent(MenuActivity.this, GameActivity.class);
        gameIntent.putExtra("localGame", localGame);
        gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(gameIntent);
        finish();
    }
}
