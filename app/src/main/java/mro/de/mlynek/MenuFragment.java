package mro.de.mlynek;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import mro.de.mlynek.network.ClientConnection;
import mro.de.mlynek.network.ClientConnectionListener;
import mro.de.mlynek.network.wifidirect.WifiDeviceManager;

/**
 * Created by Sony on 09.09.2014.
 */
public class MenuFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, ClientConnectionListener {
    private ImageView imageView;
    private ImageButton play;
    private ImageButton refresh;
    private ListView deviceList;
    private ArrayAdapter deviceAdapter;
    private View.OnClickListener clickListener;
    private WifiDeviceManager wifidevmanager;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_menu, container, false);
        imageView = (ImageView) v.findViewById(R.id.backgroudView);
        play = (ImageButton) v.findViewById(R.id.play);
        play.setOnClickListener(this);
        refresh = (ImageButton) v.findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        deviceList = (ListView) v.findViewById(R.id.listView);
        deviceAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
        deviceList.setAdapter(deviceAdapter);
        deviceList.setOnItemClickListener(this);
        wifidevmanager = ((MenuActivity)getActivity()).getWifiDeviceManager();
        wifidevmanager.setAdapter(deviceAdapter);
        wifidevmanager.searchDevices();
        deviceAdapter.insert("Test", 0);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        imageView.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.bgmenu, size.x, size.y));
        play.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.startbtn, size.x / 2, size.y / 2 / 2));
        //TODO Neue Refresh Button Bitmap mit passender Größe
        refresh.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.refresh, size.x / 2, size.y / 2 / 2));
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            clickListener = (View.OnClickListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }

    }

    @Override
    public void onDetach() {
        wifidevmanager = null;
        super.onDetach();
    }

    @Override
    public void onClick(View view)
    {
        clickListener.onClick(view);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.listView) {
            //FIXME Testcode
            if(i == 0) {
                ClientConnection ipConn = ((MlynekApplication)getActivity().getApplicationContext()).getClientConnection();
                if(ipConn == null) {
                    ipConn = ClientConnection.createConnection("10.0.2.2", 3334, this);
                    ((MlynekApplication)getActivity().getApplicationContext()).setClientConnection(ipConn);
                    ipConn.setListener(this);
                    ipConn.start();
                }
            } else {
                //FIXME set to i when removing the testcode
                wifidevmanager.connectToDevice(i-1);
            }
        }
    }

    @Override
    public void onClientConnect() {
        ((MenuActivity)getActivity()).onClientConnect();
    }

    @Override
    public void onClientConnectionFailed() {
        ((MlynekApplication)getActivity().getApplicationContext()).disconnectClientConnection();
        //FIXME: Tell the Player and prevent starting a local Game
    }

    @Override
    public void onClientDisconnect() {
        Log.d("Info", "Client(me) disconnected from Server");
    }
}
