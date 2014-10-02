package mro.de.mlynek;

import java.util.TimerTask;

import mro.de.mlynek.network.Connection;

/**
 * Created by kfg on 10/20/14.
 */
public class RecvTask extends TimerTask {
    private GameView gv;
    private Connection c;

    public RecvTask(Connection conn, GameView gv) {
        this.gv = gv;
        this.c = conn;
    }

    @Override
    public void run() {
        String tmp = c.read();
        if(tmp.length() > 0) {
            gv.handleMessage(tmp);
        }
    }
}
