package mro.de.mlynek;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by Sony on 09.09.2014.
 */
public class GameActivity extends FragmentActivity implements View.OnClickListener
{
    private GameView gameView;
    private Dialog m_dialog;
    private ImageView m_teamImage;
    private Button m_newTry;
    private Button m_mainMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this);
        setContentView(R.layout.activity_game);
        m_dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        m_dialog.setContentView(R.layout.gameoverscreen);
        m_teamImage = (ImageView) m_dialog.findViewById(R.id.teamImage);
        m_newTry = (Button) m_dialog.findViewById(R.id.bNewTry);
        m_mainMenu = (Button) m_dialog.findViewById(R.id.bMainMenu);
        m_newTry.setOnClickListener(this);
        m_mainMenu.setOnClickListener(this);
        m_dialog.hide();
    }

    public void setTeamImage(Bitmap image)
    {
        m_teamImage.setImageBitmap(image);
    }

    public void gameOver()
    {
        m_dialog.show();
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.bMainMenu:
                m_dialog.dismiss();
                Intent menuIntent = new Intent(GameActivity.this, MenuActivity.class);
                startActivity(menuIntent);
                finish();
                break;
            case R.id.bNewTry:
                Intent newGameScreen = new Intent(GameActivity.this, GameActivity.class);
                startActivity(newGameScreen);
                m_dialog.dismiss();
                finish();
        }

    }
}
