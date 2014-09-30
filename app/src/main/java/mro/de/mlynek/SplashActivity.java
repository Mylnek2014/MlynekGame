package mro.de.mlynek;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import mro.de.mlynek.R;

/**
 * Created by Sony on 08.09.2014.
 */
public class SplashActivity extends FragmentActivity
{
    private MediaPlayer splashSound;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        splashSound = MediaPlayer.create(this, R.raw.splashsound);

        Thread logoTimer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(750);
                    splashSound.start();
                    sleep(1250);
                    Intent gameIntent = new Intent(SplashActivity.this, MenuActivity.class);
                    startActivity(gameIntent);
                    finish();
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {

                }
            }
        };
        logoTimer.start();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        splashSound.release();
    }
}
