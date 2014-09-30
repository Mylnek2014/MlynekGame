package mro.de.mlynek.network;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by kfg on 9/1/14.
 */
public class GenericServer extends Thread {
    private int mPort;
    private ServerSocket serverSocket;
    private Socket mmSocket;
    private volatile boolean mRunning;
    private byte[] mBuffer;
    private byte[] mOutBuffer;
    private boolean connected;
    private ServerConnectionListener scl;
    private static final int SOCKET_TIMEOUT = 300;
    private static final int BUFFERSIZE = 1024;
    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

    public GenericServer(int port) {
        mPort = port;
        mRunning = false;
        serverSocket = null;
        mmSocket = null;
        mBuffer = null;
        mOutBuffer = null;
        connected = false; //FIXME would need a lock too if i would care ...
    }

    public void setConnectionListener(ServerConnectionListener scl) {
        this.scl = scl;
    }

    @Override
    public void run() {
        mRunning = true;
        try {
            serverSocket = new ServerSocket(mPort);
            try {
                mmSocket = serverSocket.accept();
                mmSocket.setSoTimeout(SOCKET_TIMEOUT);
                connected = true;
                if(scl != null) {
                    scl.onConnect();
                }
            } catch(SocketException se) {
                return;
            }
            InputStream in = mmSocket.getInputStream();
            OutputStream out = mmSocket.getOutputStream();

            byte[] buffer = new byte[BUFFERSIZE];
            int bytes = 0;

            while(mRunning) {

                //Send Message
                if (writeLock.tryLock()) {
                    try {
                        if(mBuffer != null) {
                            out.write(mBuffer);
                            mBuffer = null;
                        }
                    } catch(SocketTimeoutException ste) {
                        //Log.i("INFO", "Socket timed out.");
                    } catch(IOException e) {
                        break;
                    } finally {
                        writeLock.unlock();
                    }
                }

                //Receive Message
                if (readLock.tryLock()) {
                    try {
                        if(mOutBuffer == null) {
                            Arrays.fill(buffer, (byte) 0);
                            bytes = in.read(buffer, 0, BUFFERSIZE);
                            //Process Input
                            //mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget
                            String tmp = "";
                            if(bytes > 0) {
                                mOutBuffer = new byte[bytes];
                            }
                            for (int i = 0; i < bytes; i++) {
                                tmp += " " + buffer[i];
                                mOutBuffer[i] = buffer[i];
                            }
                            if (bytes > 0) {
                                Log.d("INFO", "Received Bytes" + tmp);
                                Log.d("INFO", "As String " + new String(Arrays.copyOf(buffer, bytes)));
                            }
                        }
                    } catch(SocketTimeoutException ste) {
                        //Log.i("INFO", "Socket timed out.");
                    } catch(IOException e) {
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
        } catch (BindException b) {
            //Toast.makeText("Could not bind Socket. Port already in use.")
            Log.d("INFO", "Could not bind Socket. Port already in use.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(serverSocket != null) {
                try {
                    serverSocket.close();
                    Log.i("INFO", "Disconnected");
                    connected = false;
                    serverSocket = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isConnected() {
        return connected;
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
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        } catch(IOException e) {

        }
        try {
            if(mmSocket != null) {
                mmSocket.close();
            }
        } catch(IOException e) {

        }
        mRunning = false;
        connected = false;
    }
}
