package mro.de.mlynek;

import android.util.Log;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import mro.de.mlynek.network.Connection;

/**
 * Created by kfg on 10/20/14.
 */
public class SendTask extends TimerTask {
    private GameView gv;
    private Connection c;
    String message;

    private static final int CONNECTION_PROBLEM_TIMEOUT = 2000;

    public SendTask(Connection conn, GameView gv, String message) {
        this.gv = gv;
        this.c = conn;
        this.message = message;
    }

    @Override
    public void run() {
        Calendar cal = new GregorianCalendar();
        int timeoutstart = (cal.get(Calendar.MINUTE)*100) + cal.get(Calendar.SECOND);
        //TODO: Timeout that deems the connection problematic
        while(true) {
            if((timeoutstart+CONNECTION_PROBLEM_TIMEOUT) < ((cal.get(Calendar.MINUTE)*100)+cal.get(Calendar.SECOND))) {
                //Fixme: Do something on timeout
                Log.d("SendTask", "Send reached timeout");
                timeoutstart = (cal.get(Calendar.MINUTE)*100) + cal.get(Calendar.SECOND);
            }

            if(c.write(message)) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
