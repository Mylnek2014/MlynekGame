package mro.de.mlynek.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Created by kfg on 9/1/14.
 */
public class GenericClient {
    private String mHost;
    private int mPort;
    private Socket socket;
    private static final int SOCKET_TIMEOUT = 300;

    public GenericClient(String host, int port) {
        mHost = host;
        mPort = port;
        socket = new Socket();
        try {
            socket.bind(null);
            socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean connect() {
        Log.d("INFO", "In Generic Client Connect");
        try {
            socket.connect(new InetSocketAddress(mHost, mPort), 500);
        } catch (IOException e) {
            Log.d("Info", "ServerConnection failed");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void send(byte[] data) throws IOException {
        OutputStream out = null;
        if(socket == null || data == null) {
            return;
        }
        out = socket.getOutputStream();
        int i=0;
        while(i<data.length) {
            if(data.length<i+1024) {
                out.write(data, i, data.length-i);
                break;
            } else {
                out.write(data, i, 1024);
                i+=1024;
            }
        }
    }

    public int recv(byte[] output, int max) throws IOException {
        InputStream in = null;
        int bytesread=0;
        if(socket == null) {
            return bytesread;
        }
        Arrays.fill(output, (byte) 0);
        in = socket.getInputStream();
        try {
            bytesread = in.read(output, 0, max);
        } catch(SocketTimeoutException ste) {
            //Log.i("INFO", "Socket timed out.");
        }
        return bytesread;
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        socket = null;
    }
}
