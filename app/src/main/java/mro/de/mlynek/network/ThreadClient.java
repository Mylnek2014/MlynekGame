package mro.de.mlynek.network;

import android.util.Log;

import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kfg on 9/2/14.
 */
public class ThreadClient extends Thread {
    private GenericClient mClient;
    private byte[] mBuffer;
    private byte[] mOutBuffer;
    private boolean connected;
    private ClientConnectionListener mListener;
    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

    public ThreadClient(String host, int port) {
        mClient = new GenericClient(host, port);
        mBuffer = null;
        mOutBuffer = null;
        connected = false; //FIXME would need a lock too if i would care ...
        mListener = null;
    }

    @Override
    public void run() {
        Log.d("INFO", "In Generic Client Connect");
        if(mClient.connect()) {
            Log.d("INFO", "Client connected");
            connected = true;
            if(mListener != null) {
                mListener.onClientConnect();
            }
        } else {
            Log.d("INFO", "Client failed to connect");
            if(mListener != null) {
                mListener.onClientConnectionFailed();
            }
            return;
        }

        /*byte[] bytes=new byte[1];
        bytes[0]=1;

        mClient.send(bytes);*/

        while(true) {

            //Send Message
            if(writeLock.tryLock()) {
                try {
                    if(mBuffer != null) {
                        mClient.send(mBuffer);
                        mBuffer = null;
                    }
                } catch(SocketException se) {
                    break;
                } catch(IOException ioe) {
                    break;
                } finally {
                    writeLock.unlock();
                }
            }

            //FIXME is it ok to just not read if buffer is full
            // or should we take the time to read all of it?
            //Receive Message
            if(readLock.tryLock()) {
                try {
                    if(mOutBuffer == null) {
                        int maxRead = 1024;
                        byte[] readBytes = new byte[maxRead];
                        int bytesread = 0;
                        Arrays.fill(readBytes, (byte) 0);
                        bytesread = mClient.recv(readBytes, maxRead);
                        if (bytesread > 0) {
                            String tmp = "";
                            mOutBuffer = new byte[bytesread];
                            Arrays.fill(mOutBuffer, (byte) 0);

                            mOutBuffer[0] = (byte) 0;

                            for (int i = 0; i < mOutBuffer.length; i++) {
                                mOutBuffer[i] = (byte) 1;
                            }

                            for (int i = 0; i < bytesread; i++) {
                                tmp += " " + readBytes[i];
                                mOutBuffer[i] = readBytes[i];
                            }
                        /*Log.d("INFO", "Read Bytes: " + tmp);
                        Log.d("INFO", "As String " + new String(Arrays.copyOf(readBytes, bytesread)));*/
                        }
                    }
                } catch(SocketException se) {
                    break;
                } catch(IOException ioe) {
                    break;
                } finally {
                    readLock.unlock();
                }
            }

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        connected = false;
        Log.i("INFO", "Disconnected");
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnectionListener(ClientConnectionListener ccl) {
        this.mListener = ccl;
    }

    public boolean send(byte[] message) {
        if(writeLock.tryLock()) {
            try {
                if (mBuffer == null) {
                    mBuffer = message.clone();
                    return true;
                }
            } finally {
                writeLock.unlock();
            }
            return false;
        } else {
            return false;
        }
    }

    public byte[] recv() {
        if(readLock.tryLock()) {
            byte[] out = null;
            try {
                if(mOutBuffer != null) {
                    out = mOutBuffer.clone();
                    mOutBuffer = null;
                }
            } finally {
                readLock.unlock();
            }
            return out;
        } else {
            return null;
        }
    }

    public void close() {
        connected = false;
        if(mClient != null) {
            mClient.close();
        }
    }
}
