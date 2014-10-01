package mro.de.mlynek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import mro.de.mlynek.network.ServerConnection;
import mro.de.mlynek.network.wifidirect.WifiDeviceManager;

/**
 * Created by Sony on 08.09.2014.
 */
public class MenuActivity extends FragmentActivity implements View.OnClickListener
{
    private WifiDeviceManager wifidevmanager;
    private ServerConnection servConn;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        wifidevmanager = new WifiDeviceManager(this, GameActivity.class);
        setContentView(R.layout.activity_menu);
    }

    public WifiDeviceManager getWifiDeviceManager() {
        return wifidevmanager;
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.play)
        {
            // For testing in the Emulator
            if(!wifidevmanager.isWifiEnabled()) {
                servConn = ServerConnection.createConnection(3333);
                servConn.start();
            } else {
                wifidevmanager.setDiscoverable(true, "Młyneck");
            }
            Intent gameIntent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(gameIntent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
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
}
