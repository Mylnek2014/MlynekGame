package mro.de.mlynek;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by Sony on 08.09.2014.
 */
public class MenuActivity extends FragmentActivity implements View.OnClickListener
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    @Override
    public void onClick(View view)
    {
        if(view.getId() == R.id.play)
        {
            Intent gameIntent = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(gameIntent);
            finish();
        }
    }
}
