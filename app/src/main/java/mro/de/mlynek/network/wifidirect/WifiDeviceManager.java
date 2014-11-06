package mro.de.mlynek.network.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mro.de.mlynek.network.*;

//FIXME Receiver must be unregistered at onPause and reregistered at onResume in the Activity

/**
 * Created by kfg on 9/1/14.
 */
public class WifiDeviceManager implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener, ServerConnectionListener, ClientConnectionListener {
    private ArrayAdapter<String> mArrayAdapter;
    private WifiDeviceManager mDeviceManager;
    private WifiManager mWifiManager;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDnsSdServiceInfo mServiceInfo;
    private Activity mActivity;
    private Set<String> addresses;
    private boolean mWifiEnabled;
    private ArrayList<WifiP2pDevice> mDevices;
    private WifiP2pManager.DnsSdTxtRecordListener gameListener;
    private WifiP2pManager.DnsSdServiceResponseListener responseListener;
    private WifiP2pDnsSdServiceRequest servRequest;
    private final IntentFilter filter = new IntentFilter();
    private Class<? extends Activity> mOnConnectActivity;
    //FIXME Seperate?
    private ClientConnection mClientconn;
    private ServerConnection mServerconn;
    //TXT Record Properties
    static final int PORT=3333;
    static final String SERVICE_INSTANCE = "_test";
    static final String SERVICE_REG_TYPE = "_presence._tcp";
    static final String GAMENAME="Młynek";

    public WifiDeviceManager(Activity activity, Class<? extends Activity> onConnectActivity) {
        if(activity == null) {
            throw new IllegalArgumentException(("No Activity"));
        }

        mDeviceManager = this;
        mActivity = activity;
        mWifiEnabled = false;
        if(onConnectActivity == null) {
            throw new IllegalArgumentException("No onConnectActivity");
        }
        mOnConnectActivity = onConnectActivity;
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if(!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        mWifiP2pManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(activity, activity.getMainLooper(), null);
        mActivity.registerReceiver(mReceiver, filter);
        mClientconn = null;
        mServerconn = null;
        mServiceInfo = null;
        mDevices = new ArrayList<WifiP2pDevice>();
    }

    public void setAdapter(ArrayAdapter<String> arrayAdapter) {
        mArrayAdapter = arrayAdapter;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.d("INFO", "WIFI P2P Enabled");
                    Toast.makeText(mActivity.getApplicationContext(), "WIFI P2P Enabled", Toast.LENGTH_SHORT).show();
                    mWifiEnabled = true;
                } else {
                    Log.d("INFO", "WIFI P2P Disabled");
                    Toast.makeText(mActivity.getApplicationContext(), "WIFI P2P Disabled", Toast.LENGTH_SHORT).show();
                    mWifiEnabled = false;
                }
            }

            if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if(mWifiP2pManager == null) {
                    return;
                }
                mWifiP2pManager.requestPeers(mChannel, mDeviceManager);
            }

            if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                if(mWifiManager == null) {
                    return;
                }

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(networkInfo == null) {
                    Log.d("INFO", "Nope, Nope, Nope no network info");
                    Toast.makeText(mActivity.getApplicationContext(), "Nope, Nope, Nope no network info", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Log.i("INFO", "Got Network info");
                    Toast.makeText(mActivity.getApplicationContext(), "Got Network Info!!!!", Toast.LENGTH_SHORT).show();
                }

                if(networkInfo.isConnected()) {
                    Log.i("INFO", "Networkinfo.isConnected true");
                    mWifiP2pManager.requestConnectionInfo(mChannel, mDeviceManager);
                } else {
                    Log.i("INFO", "Networkinfo.isConnected false");
                }
            }
        }
    };

    public void searchDevices() {
        mArrayAdapter.clear();
        mDevices.clear();
        /*mArrayAdapter.add("Test");
        mArrayAdapter.notifyDataSetChanged();*/
        Log.d("INFO", "Searching Devices");
        Toast.makeText(mActivity.getApplicationContext(), "Searching Devices", Toast.LENGTH_SHORT).show();
        if(mWifiEnabled) {
           gameListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                @Override
                public void onDnsSdTxtRecordAvailable(String s, Map<String, String> record, WifiP2pDevice p2pdevice) {
                    Log.d("INFO", "DnsSdTxtRecord available -" + record.toString());
                    Toast.makeText(mActivity.getApplicationContext(), "DnsSdTxtRecord available" + record.toString(), Toast.LENGTH_SHORT).show();
                    if(!mDevices.contains(p2pdevice)) {
                        if (record.containsKey("game") && record.get("game").equals(GAMENAME)) {
                            if (record.containsKey("servername")) {
                                mArrayAdapter.add(/*p2pdevice.deviceName + " " + p2pdevice.deviceAddress*/ record.get("servername"));
                            } else {
                                mArrayAdapter.add("Dieses Gerät ist dumm");
                            }
                            mDevices.add(p2pdevice);
                            mArrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            };

            if(gameListener == null) {
                Log.d("INFO", "Error while creating DnsSdTxtRecord listener");
                Toast.makeText(mActivity.getApplicationContext(), "Error while creating DnsSdTxtRecord listener", Toast.LENGTH_SHORT).show();
                return;
            }

            responseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                @Override
                public void onDnsSdServiceAvailable(String s, String s2, WifiP2pDevice device) {
                    //device.deviceName =
                    Log.d("INFO", "DnsSdService available");
                    Toast.makeText(mActivity.getApplicationContext(), "DnsSdService available", Toast.LENGTH_SHORT).show();
                }
            };

            if(responseListener == null) {
                Log.d("INFO", "Error while creating DnsSdServiceResponse listener");
                Toast.makeText(mActivity.getApplicationContext(), "Error while creating DnsSdServiceResponse listener", Toast.LENGTH_SHORT).show();
                return;
            }

            servRequest = WifiP2pDnsSdServiceRequest.newInstance();

            if(servRequest == null) {
                Log.d("INFO", "Error while creating new WifiP2pDnsSdServiceRequest");
                Toast.makeText(mActivity.getApplicationContext(), "Error while creating new WifiP2pDnsSdServiceRequest", Toast.LENGTH_SHORT).show();
                return;
            }

            mWifiP2pManager.addServiceRequest(mChannel, servRequest, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d("INFO", "Service Request added");
                    Toast.makeText(mActivity.getApplicationContext(), "Service Request added", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int code) {
                    if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                        Log.d("INFO", "P2P isn't supported on this device");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P isn't supported on this device", Toast.LENGTH_SHORT).show();
                    } else if (code == WifiP2pManager.BUSY) {
                        Log.d("INFO", "P2P device busy");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P device busy", Toast.LENGTH_SHORT).show();
                    } else if (code == WifiP2pManager.ERROR) {
                        Log.d("INFO", "P2P device error");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P device error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mWifiP2pManager.setDnsSdResponseListeners(mChannel, responseListener, gameListener);

            mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Success!
                    Log.d("INFO", "Successfully started Discovering Services!");
                    Toast.makeText(mActivity.getApplicationContext(), "Successfully started Discovering Services!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int code) {
                    // Fail
                    if (code == WifiP2pManager.P2P_UNSUPPORTED) {
                        Log.d("INFO", "P2P isn't supported on this device");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P isn't supported on this device", Toast.LENGTH_SHORT).show();
                    } else if (code == WifiP2pManager.BUSY) {
                        Log.d("INFO", "P2P device busy");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P device busy", Toast.LENGTH_SHORT).show();
                    } else if (code == WifiP2pManager.ERROR) {
                        Log.d("INFO", "P2P device error");
                        Toast.makeText(mActivity.getApplicationContext(), "P2P device error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Not used replaced by Service Discovery
            /*mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener()  {
                @Override
                public void onSuccess() {
                    Log.d("INFO", "Wifi P2P Discover Peer Success");
                }

                @Override
                public void onFailure(int reasonCode) {
                    Log.d("INFO", "Wifi P2P Discover Peer Failure");
                }
            });*/
        } else {
            Log.d("INFO", "Wifi P2P not Enabled (yet)");
            //Toast.makeText(mActivity.getApplicationContext(), "Wifi not Enabled (l.222)", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isWifiEnabled() {
        return mWifiEnabled;
    }

    public void setDiscoverable(boolean discoverable, String name) {
        if(discoverable) {
            //Register Service
            Map<String, String> info = new HashMap<String, String>();
            info.put("listenport", String.valueOf(PORT));
            info.put("game", GAMENAME);
            //FIXME Limit number of characters
            info.put("servername", name);
            mServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, info);

            mWifiP2pManager.addLocalService(mChannel, mServiceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(mActivity.getApplicationContext(), GAMENAME + " WIfi P2P Service Registered", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Log.d("ERROR", "Could not register " + GAMENAME + " WIfi P2P Service");
                    Toast.makeText(mActivity.getApplicationContext(), "Could not register" + GAMENAME + " WIfi P2P Service", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            //Unregister Service
            try {
                mActivity.unregisterReceiver(mReceiver);
            } catch(IllegalArgumentException iae) {
                //Service was not registered
            }
            try {
                mWifiP2pManager.removeLocalService(mChannel, mServiceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(mActivity.getApplicationContext(), GAMENAME + " WIfi P2P Service Unregistered", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.d("ERROR", "Could not unregister " + GAMENAME + " WIfi P2P Service");
                        Toast.makeText(mActivity.getApplicationContext(), "Could not unregister" + GAMENAME + " WIfi P2P Service", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch(IllegalArgumentException iae) {
                //Service was not registered
            }
        }
    }

    public static void unregisterService(final Activity activity, WifiP2pManager.Channel channel, BroadcastReceiver receiver, WifiP2pDnsSdServiceInfo serviceInfo) {
        //Unregister Service
        try {
            activity.unregisterReceiver(receiver);
        } catch(IllegalArgumentException iae) {
            //Service was not registered
        }
        try {
            WifiP2pManager tmpManager = (WifiP2pManager)activity.getSystemService(Context.WIFI_P2P_SERVICE);

            if(tmpManager == null) {
                Log.i("Info", "FAIL: Could not get WifiP2pManager");
                return;
            }
            tmpManager.removeLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(activity.getApplicationContext(), GAMENAME + " WIfi P2P Service Unregistered", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Log.d("ERROR", "Could not unregister " + GAMENAME + " WIfi P2P Service");
                    Toast.makeText(activity.getApplicationContext(), "Could not unregister" + GAMENAME + " WIfi P2P Service", Toast.LENGTH_SHORT).show();
                }
            });
        } catch(IllegalArgumentException iae) {
            //Service was not registered
        }
    }

    public void close() {
        setDiscoverable(false, "");
        if(gameListener != null) {
            gameListener = null;
        }
        if(mServerconn != null) {
            mServerconn.close();
        }
        if(mClientconn != null) {
            mClientconn.close();
        }
    }

    public void connectToDevice(int id) {
        if(!mWifiEnabled) {
            Log.d("INFO", "Wifi not enabled");
            Toast.makeText(mActivity.getApplicationContext(),"Wifi not enabled (l. 280)" , Toast.LENGTH_SHORT).show();
            return;
        }

        if(id >= mDevices.size()) {
            Log.d("INFO", "No id: "+id);
            Toast.makeText(mActivity.getApplicationContext(),"No id: "+id , Toast.LENGTH_SHORT).show();
            return;
        }

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = mDevices.get(id).deviceAddress;

        mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("INFO", "Success connecting to device "/*+config.deviceAddress*/);
                Toast.makeText(mActivity.getApplicationContext(), "Success connecting to device ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Log.d("INFO", "Failed to connect to device "/*+config.deviceAddress*/);
                Toast.makeText(mActivity.getApplicationContext(), "Failed to connect to device ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        mArrayAdapter.clear();
        for(WifiP2pDevice p2pdevice: wifiP2pDeviceList.getDeviceList()) {
            mArrayAdapter.add(p2pdevice.deviceName + " " + p2pdevice.deviceAddress);
            mDevices.add(p2pdevice);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        Log.i("Info", "Got Connection Info");
        if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.i("Info", "Server");
            //Start server thread
            mServerconn = ServerConnection.createConnection(PORT);
            mServerconn.setConnectionListener(this);
            mServerconn.setWifiP2pChannel(mChannel);
            mServerconn.setWifiServiceState(mReceiver, mServiceInfo);
            mServerconn.start();
            setDiscoverable(false, "");
        } else if(wifiP2pInfo.groupFormed) {
            Log.i("Info", "Client");
            //Connect as client
            mClientconn = ClientConnection.createConnection(wifiP2pInfo.groupOwnerAddress.getHostAddress(), PORT, this);
            mClientconn.setListener(this);
            mClientconn.setWifiP2pChannel(mChannel);
            mClientconn.start();
        }
    }

    public static void disconnect(Activity activity, WifiP2pManager.Channel channel) {
        WifiP2pManager tmpManager = (WifiP2pManager)activity.getSystemService(Context.WIFI_P2P_SERVICE);
        if(tmpManager != null ) {
            try {
                tmpManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //TODO
                    }

                    @Override
                    public void onFailure(int i) {
                        //TODO
                    }
                });
            } catch(IllegalArgumentException iae) {
                // Happens if we didn't use wifi (or something very nasty is going on)
            }
        }
    }

    @Override
    public void onConnect() {
        Intent intent = new Intent(mActivity, mOnConnectActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
    }

    @Override
    public void onClientConnect() {
        mClientconn.write("msg Hello World");
        Intent intent = new Intent(mActivity, mOnConnectActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
    }

    @Override
    public void onClientConnectionFailed() {
        //FIXME Propagate somehow?
    }
}
