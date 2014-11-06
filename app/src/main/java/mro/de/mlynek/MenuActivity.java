package mro.de.mlynek;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.ServerConnectionListener;
import mro.de.mlynek.network.wifidirect.WifiDeviceManager;

/**
 * Created by Sony on 08.09.2014.
 */
public class MenuActivity extends FragmentActivity implements View.OnClickListener, ServerConnectionListener {
    private WifiDeviceManager wifidevmanager;
    private ServerConnection servConn;
    private AlertDialog connectionWait;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        wifidevmanager = new WifiDeviceManager(this, GameActivity.class);
        setContentView(R.layout.activity_menu);
        AlertDialog.Builder connectionWaitBuilder = new AlertDialog.Builder(this);
        connectionWaitBuilder.setMessage("Warte auf Verbindung");
        connectionWaitBuilder.setNegativeButton("Abbruch", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(!wifidevmanager.isWifiEnabled()) {
                    servConn.close();
                } else {
                    wifidevmanager.setDiscoverable(false, "Młyneck");
                }
            }
        });
        connectionWait = connectionWaitBuilder.create();
    }

    public WifiDeviceManager getWifiDeviceManager() {
        return wifidevmanager;
    }

    //FIXME Overlay till client connected

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.play)
        {
            // For testing in the Emulator
            if(!wifidevmanager.isWifiEnabled()) {
                servConn = ServerConnection.createConnection(3333);
                servConn.setConnectionListener(this);
                servConn.start();
            } else {
                wifidevmanager.setDiscoverable(true, "Młyneck");
            }
            connectionWait.show();
        }
        if(view.getId() == R.id.refresh) {
            wifidevmanager.searchDevices();
        }
    }

    @Override
    protected void onDestroy() {
        if(connectionWait.isShowing()) {
            connectionWait.dismiss();
            connectionWait = null;
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
        Intent gameIntent = new Intent(MenuActivity.this, GameActivity.class);
        startActivity(gameIntent);
        finish();
    }
}
