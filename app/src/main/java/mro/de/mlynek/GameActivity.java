package mro.de.mlynek;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by Sony on 09.09.2014.
 */
public class GameActivity extends FragmentActivity
{
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(R.layout.activity_game);

    }
}
