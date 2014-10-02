package mro.de.mlynek;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Sony on 10.09.2014.
 */
public class GameView extends SurfaceView
{
    private Point m_size = new Point();
    private int m_edge;
    private int m_columnHeight;
    private int m_menHeight;

    private SurfaceHolder m_surfaceHolder;
    private Bitmap m_backgroundImage;
    private Bitmap m_pitch;
    private Bitmap m_menImage;
    private Bitmap m_men2Image;

    private MenPosition[] m_menPositions;
    private int[][] m_mills;
    private int m_turnCount;
    private int m_team = 0;
    private boolean m_clearMen;

    public GameView(Context context)
    {
        super(context);

        // Höhen und Breiten berechnen
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(m_size);
        m_edge = (m_size.y - m_size.x) / 2;
        m_columnHeight = m_size.x / 9;
        m_menHeight = m_columnHeight - m_columnHeight / 4;

        // Steuervarialen fürs Spiel
        m_turnCount = 0;
        m_clearMen = false;

        // Bilder...
        //m_backgroundImage = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.bgmenu, m_size.x, m_size.y);
        m_pitch = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.pitch, m_size.x, m_size.y);
        m_menImage = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.german, m_menHeight, m_menHeight);
        m_men2Image = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.polski, m_menHeight, m_menHeight);

        // Setzen der Mühlenpositionen, Steinpositionen
        m_mills = setMillPositions();
        createMenPositions();

        m_surfaceHolder = getHolder();
        m_surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceDestroyed(SurfaceHolder holder)
            {
            }

            @SuppressLint("WrongCall")
            public void surfaceCreated(SurfaceHolder holder)
            {
                drawView();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
//        Log.d("OnTouchEvent", "start");
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            Log.d("OnTouchEvent", "ActionDown");
            int clickedIndex = checkClickPosition(event.getX(), event.getY());
            Log.d("OnTouchEvent", "ClickedIndex: " + clickedIndex);
            if(clickedIndex > -1)
            {
                Log.d("OnTouchEvent", "ClearMen: " + m_clearMen);
                if(m_clearMen)
                {
                    if(m_menPositions[clickedIndex].hasMen() && !m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()))
                    {
                        if(!checkForMill(clickedIndex))
                        {
                            m_menPositions[clickedIndex].setImage(null);
                            m_team = (m_team + 1) % 2;
                            m_clearMen = false;
                        }
                    }
                }
                else
                {
                    if(m_turnCount < 18 && !m_menPositions[clickedIndex].hasMen())
                    {
                        m_menPositions[clickedIndex].setImage(getCurrentTeamImage());
                        m_turnCount++;
                    }
                    else
                    {

                    }

                    if(checkForMill(clickedIndex))
                    {
                        Log.d("OnTouchEvent", "CheckForMill");
                        m_clearMen = true;
                    }
                    else
                    {
                        m_team = (m_team + 1) % 2;
                    }
                }

                drawView();
            }
        }
        return true;
    }

    private Bitmap getCurrentTeamImage()
    {
        Bitmap image = null;
        if(m_team == 0)
        {
            image = m_menImage;
        }
        else
        {
            image = m_men2Image;
        }
        return image;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.argb(255,92,141,229));
        canvas.drawBitmap(m_pitch, 0, m_edge, null);

        Log.d("MRO GameView", "Check Positions");
        for(MenPosition mp : m_menPositions)
        {
            if(mp.hasMen())
            {
                canvas.drawBitmap(mp.getImage(), mp.getXPosition(), mp.getYPosition(), null);
            }
        }

    }

    @SuppressLint("WrongCall")
    private void drawView()
    {
        Canvas canvas = getHolder().lockCanvas();
        this.onDraw(canvas);
        getHolder().unlockCanvasAndPost(canvas);
    }

    private void createMenPositions()
    {
        int columnWidth = m_pitch.getWidth() / 9;
        int displayWidth = m_pitch.getWidth();


        m_menPositions = new MenPosition[24];

        // 1 Reihe
        m_menPositions[0] = new MenPosition(columnWidth, m_edge + columnWidth, m_menImage.getHeight());
        m_menPositions[1] = new MenPosition(displayWidth / 2, m_edge + columnWidth, m_menImage.getHeight());
        m_menPositions[2] = new MenPosition(columnWidth * 8, m_edge + columnWidth, m_menImage.getHeight());

        // 2 Reihe
        m_menPositions[3] = new MenPosition(columnWidth * 2, m_edge + columnWidth * 2, m_menImage.getHeight());
        m_menPositions[4] = new MenPosition(displayWidth / 2, m_edge + columnWidth * 2, m_menImage.getHeight());
        m_menPositions[5] = new MenPosition(columnWidth * 7, m_edge + columnWidth * 2, m_menImage.getHeight());

        // 3 Reihe
        m_menPositions[6] = new MenPosition(columnWidth * 3, m_edge + columnWidth * 3, m_menImage.getHeight());
        m_menPositions[7] = new MenPosition(displayWidth / 2, m_edge + columnWidth * 3, m_menImage.getHeight());
        m_menPositions[8] = new MenPosition(columnWidth * 6, m_edge + columnWidth * 3, m_menImage.getHeight());

        // 4 Reihe
        // links
        m_menPositions[9] = new MenPosition(columnWidth, m_edge + displayWidth / 2, m_menImage.getHeight());
        m_menPositions[10] = new MenPosition(columnWidth * 2, m_edge + displayWidth / 2, m_menImage.getHeight());
        m_menPositions[11] = new MenPosition(columnWidth * 3, m_edge + displayWidth / 2, m_menImage.getHeight());

        // rechts
        m_menPositions[12] = new MenPosition(columnWidth * 6, m_edge + displayWidth / 2, m_menImage.getHeight());
        m_menPositions[13] = new MenPosition(columnWidth * 7, m_edge + displayWidth / 2, m_menImage.getHeight());
        m_menPositions[14] = new MenPosition(columnWidth * 8, m_edge + displayWidth / 2, m_menImage.getHeight());

        // 5 Reihe
        m_menPositions[15] = new MenPosition(columnWidth * 3, m_edge + columnWidth * 6, m_menImage.getHeight());
        m_menPositions[16] = new MenPosition(displayWidth / 2, m_edge + columnWidth * 6, m_menImage.getHeight());
        m_menPositions[17] = new MenPosition(columnWidth * 6, m_edge + columnWidth * 6, m_menImage.getHeight());

        // 6 Reihe
        m_menPositions[18] = new MenPosition(columnWidth * 2, m_edge + columnWidth * 7, m_menImage.getHeight());
        m_menPositions[19] = new MenPosition(displayWidth / 2, m_edge + columnWidth * 7, m_menImage.getHeight());
        m_menPositions[20] = new MenPosition(columnWidth * 7, m_edge + columnWidth * 7, m_menImage.getHeight());

        // 7 Reihe
        m_menPositions[21] = new MenPosition(columnWidth, m_edge + columnWidth * 8, m_menImage.getHeight());
        m_menPositions[22] = new MenPosition(displayWidth / 2, m_edge + columnWidth * 8, m_menImage.getHeight());
        m_menPositions[23] = new MenPosition(columnWidth * 8, m_edge + columnWidth * 8, m_menImage.getHeight());
    }

    private int[][] setMillPositions()
    {
        int[][] mills = new int[16][3];

        mills[0][0] = 0;
        mills[0][1] = 1;
        mills[0][2] = 2;

        mills[1][0] = 2;
        mills[1][1] = 14;
        mills[1][2] = 23;

        mills[2][0] = 21;
        mills[2][1] = 22;
        mills[2][2] = 23;

        mills[3][0] = 0;
        mills[3][1] = 9;
        mills[3][2] = 21;

        mills[4][0] = 3;
        mills[4][1] = 4;
        mills[4][2] = 5;

        mills[5][0] = 5;
        mills[5][1] = 13;
        mills[5][2] = 20;

        mills[6][0] = 18;
        mills[6][1] = 19;
        mills[6][2] = 20;

        mills[7][0] = 3;
        mills[7][1] = 10;
        mills[7][2] = 18;

        mills[8][0] = 6;
        mills[8][1] = 7;
        mills[8][2] = 8;

        mills[9][0] = 8;
        mills[9][1] = 12;
        mills[9][2] = 17;

        mills[10][0] = 15;
        mills[10][1] = 16;
        mills[10][2] = 17;

        mills[11][0] = 6;
        mills[11][1] = 11;
        mills[11][2] = 15;

        mills[12][0] = 1;
        mills[12][1] = 4;
        mills[12][2] = 7;

        mills[13][0] = 12;
        mills[13][1] = 13;
        mills[13][2] = 14;

        mills[14][0] = 16;
        mills[14][1] = 19;
        mills[14][2] = 22;

        mills[15][0] = 9;
        mills[15][1] = 10;
        mills[15][2] = 11;

        return mills;
    }

    private int checkClickPosition(float x, float y)
    {
        int index = -1;
        for(int i = 0; i < m_menPositions.length; i++)
        {
            if(m_menPositions[i].isTouched(x,y))
            {
                index = i;
//                if(m_turnCount < 18)
//                {
//                    m_turnCount++;
//                    if(m_team == 0)
//                    {
//                        m_menPositions[i].setImage(m_menImage);
//                    }
//                    else
//                    {
//                        m_menPositions[i].setImage(m_men2Image);
//                    }
//                    m_team = (m_team + 1) % 2;
//                }
            }
        }
        return index;
    }

    private boolean checkForMill(int clickedIndex)
    {
        Log.d("CheckForMill", "Prüfen");
        boolean millExist = false;

        for(int i = 0; i < 16; i++)
        {
            for(int y = 0; y < 3; y++)
            {
                if(clickedIndex == m_mills[i][y])
                {
                    if(m_menPositions[m_mills[i][0]].hasMen() && m_menPositions[m_mills[i][1]].hasMen() && m_menPositions[m_mills[i][2]].hasMen())
                    {
                        if(m_menPositions[m_mills[i][0]].getImage().equals(m_menPositions[m_mills[i][1]].getImage()) && m_menPositions[m_mills[i][0]].getImage().equals(m_menPositions[m_mills[i][2]].getImage()))
                        {
                            millExist = true;
                        }
                    }
                }
            }
        }
        Log.d("CheckForMill", "MillExist: " + millExist);

        return millExist;
    }

}
