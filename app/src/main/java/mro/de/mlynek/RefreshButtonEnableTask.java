package mro.de.mlynek;

import android.app.Activity;
import android.util.Log;
import android.widget.ImageButton;

import java.util.TimerTask;

import mro.de.mlynek.network.Connection;

/**
 * Created by kfg on 10/20/14.
 */
public class RefreshButtonEnableTask extends TimerTask {
    private ImageButton b;

    public RefreshButtonEnableTask(ImageButton b) {
        this.b = b;
    }

    @Override
    public void run() {
        Log.d("Info", "Reenabling Refresh Button");
        ((Activity)b.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b.setEnabled(true);
            }
        });
    }
}
