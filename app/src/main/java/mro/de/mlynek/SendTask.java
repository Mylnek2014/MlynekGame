package mro.de.mlynek;

import java.util.TimerTask;

import mro.de.mlynek.network.Connection;

/**
 * Created by kfg on 10/20/14.
 */
public class SendTask extends TimerTask {
    private GameView gv;
    private Connection c;
    String message;

    public SendTask(Connection conn, GameView gv, String message) {
        this.gv = gv;
        this.c = conn;
        this.message = message;
    }

    @Override
    public void run() {
        //TODO: Timeout that deems the connection problematic
        while(true) {
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
