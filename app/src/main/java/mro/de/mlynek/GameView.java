package mro.de.mlynek;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;

import mro.de.mlynek.network.Connection;
import mro.de.mlynek.network.ServerConnection;

/**
 * Created by Sony on 10.09.2014.
 */
public class GameView extends SurfaceView
{
    private static final int MAXCLICKINDEX = 23;

    private Point m_size = new Point();
    private int m_edge;
    private int m_columnHeight;
    private int m_menHeight;
    private boolean m_lokalGame = true;

    private GameActivity gameActivity;
    private SurfaceHolder m_surfaceHolder;
    private Bitmap m_backgroundImage;
    private Bitmap m_pitch;
    private Bitmap m_menImage;
    private Bitmap m_men2Image;

    private MenPosition[] m_menPositions;
    private int[][] m_mills;
    private int[][] m_movePositions;
    private int m_turnCount;
    private int m_team = 0;
    private boolean m_clearMen;
    private int m_lastMenIndex;
    private boolean m_endGame;

    private int m_menCountTeamOne;
    private int m_menCountTeamTwo;

    private Connection connection;
    private int myTeam;
    Timer sendTimer;

    public GameView(Context context)
    {
        super(context);

        gameActivity = (GameActivity)context;

        // Höhen und Breiten berechnen
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(m_size);
        m_edge = (m_size.y - m_size.x) / 2;
        m_columnHeight = m_size.x / 9;
        m_menHeight = m_columnHeight - m_columnHeight / 4;

        // Steuervarialen fürs Spiel
        m_turnCount = 0;
        m_clearMen = false;
        m_lastMenIndex = -1;
        m_menCountTeamOne = 0;
        m_menCountTeamTwo = 0;
        m_endGame = false;

        // Bilder...
        //m_backgroundImage = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.bgmenu, m_size.x, m_size.y);
        m_pitch = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.pitch, m_size.x, m_size.y);
        m_menImage = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.german, m_menHeight, m_menHeight);
        m_men2Image = Util.decodeSampledBitmapFromResource(context.getResources(), R.drawable.polski, m_menHeight, m_menHeight);

        // Setzen der Mühlenpositionen, Steinpositionen
        m_mills = setMillPositions();
        m_movePositions = setMovePosition();
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

        Log.d("GameView", "Game view created");

        connection = ((GameActivity)context).getConnection();
        m_lokalGame = ((GameActivity)context).isLocalGame();
        if(m_lokalGame) {
            Log.d("GameView", "local Game");
        } else {
            Log.d("GameView", "Network Game");
        }

        //TODO: Fair team assignement (is m_team used for the same purpose?)
        if(connection instanceof ServerConnection) {
            myTeam = 0;
        } else {
            myTeam = 1;
        }

        sendTimer = new Timer();
    }

    //TODO: Better cheat protection
    public void handleMessage(String message) {
        Log.d("HandleMessage", "Received message: "+message);
        if(message.startsWith("set")) {
            if (isMyTurn()) {
                Log.d("HandleMessage", "Enemy tried to set men during my Turn!");
                return;
            }
            String[] tmp = message.split(" ");
            if (tmp.length >= 2) {
                for (String t : tmp) {
                    Log.d("Parts", t);
                }
                //TODO: Handle better (Error Dialog or something)
                try {
                    int clickIndex = Integer.parseInt(tmp[1].trim());
                    handleClick(clickIndex, false);
                } catch (NumberFormatException nfe) {
                    Log.d("HandleMessage", "Number Format Exception in set");
                    Log.d("HandleMessage", message);
                    nfe.printStackTrace();
                }
            }
        } else if(message.startsWith("select")) {
            if (isMyTurn()) {
                Log.d("HandleMessage", "Enemy tried to select men during my Turn!");
                return;
            }
            String[] tmp = message.split(" ");
            if (tmp.length >= 1) {
                for (String t : tmp) {
                    Log.d("Parts", t);
                }
                //TODO: Handle better (Error Dialog or something)
                try {
                    int clickindex = Integer.parseInt(tmp[1].trim());
                    if(clickindex > 0 && clickindex < MAXCLICKINDEX) {
                        m_lastMenIndex = clickindex;
                    }
                } catch (NumberFormatException nfe) {
                    Log.d("HandleMessage", "Number Format Exception in select");
                    Log.d("HandleMessage", message);
                    nfe.printStackTrace();
                }
            }
        } else if(message.startsWith("deselect")) {
            if (isMyTurn()) {
                Log.d("HandleMessage", "Enemy tried to deselect men during my Turn!");
                return;
            }
            m_lastMenIndex = -1;
        } else if (message.startsWith("move")) {
            if(isMyTurn()) {
                Log.d("HandleMessage", "Enemy tried to move men during my Turn!");
                return;
            }
            String[] tmp = message.split(" ");
            if(tmp.length >= 3) {
                for(String t: tmp) {
                    Log.d("Parts", t);
                }
                //TODO: Handle better (Error Dialog or something)
                try {
                    int lastIndex = Integer.parseInt(tmp[1].trim());
                    int clickIndex = Integer.parseInt(tmp[2].trim());
                    moveMen(lastIndex, clickIndex);
                    //TODO: Put this into moveMen (or an extra function that also calls moveMen)
                    if(!checkForMill(clickIndex)) {
                        changeTeam();
                    }
                } catch(NumberFormatException nfe) {
                    Log.d("HandleMessage", "Number Format Exception in move");
                    Log.d("HandleMessage", message);
                    nfe.printStackTrace();
                }
            }
        } else if(message.startsWith("clear")) {
            if(isMyTurn()) {
                Log.d("HandleMessage", "Enemy tried to clear men during my Turn!");
                return;
            }
            String[] tmp = message.split(" ");
            if(tmp.length >= 2) {
                for(String t: tmp) {
                    Log.d("Parts", t);
                }
                //TODO: Handle better (Error Dialog or something)
                try {
                    int clickIndex = Integer.parseInt(tmp[1].trim());
                    clearMen(clickIndex);
                } catch(NumberFormatException nfe) {
                    Log.d("HandleMessage", "Number Format Exception in clear");
                    Log.d("HandleMessage", message);
                    nfe.printStackTrace();
                }
            }
        } else if(message.startsWith("disconnect")) {
            //TODO: Show dialog instead of exiting
            gameActivity.finish();
        } else {
            Log.d("handleMessage", "Unhandled Message:\n"+message);
        }
        drawView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if(m_lokalGame)
            {
                touchLokal(event);
            }
            else
            {
                touchWifi(event);
            }
        }
        return true;
    }

    private void touchWifi(MotionEvent event)
    {
        Log.d("WifiTouchEvent", "ActionDown");
        if(!isMyTurn()) {
            Log.d("WifiTouchEvent", "Not my Turn");
            return;
        }
        //Log.d("OnTouchEvent", "ActionDown");
        int clickedIndex = checkClickPosition(event.getX(), event.getY());
        Log.d("OnTouchEvent", "ClickedIndex: " + clickedIndex);
        handleClick(clickedIndex, true);
    }

    private boolean isMyTurn() {
        return m_team == myTeam;
    }

    //FIXME: Make sure the message gets either send fast or the game is paused
    //till it can be send
    private void sendMessage(String message) {
        if(m_lokalGame) {
            Log.d("sendMessage", "ERROR: Called in local Game!");
            return;
        }
        if(sendTimer != null) {
            sendTimer.schedule(new SendTask(connection, this, message), 0);
        }
    }

    private void handleClick(int clickedIndex, boolean local) {
        boolean deSelect = false;
        boolean menSet = false;

        if(isValidIndex(clickedIndex))
        {
            if(m_clearMen)
            {
                clearMen(clickedIndex);
                if(local) {
                    sendMessage("clear "+clickedIndex+"\n");
                }
            }
            else
            {
                if(isSetPhase())
                {
                    if(!m_menPositions[clickedIndex].hasMen())
                    {
                        setMen(clickedIndex);
                        increaseCurrentTeamCount();
                        if(local) {
                            sendMessage("set "+clickedIndex+" \n");
                            //Log.d("handleClick", "Could not send");
                        }
                        menSet = true;
                    }
                }
                else
                {
                    if(isMenSelected())
                    {
                        if(m_lastMenIndex == clickedIndex)
                        {
                            m_lastMenIndex = -1;
                            deSelect = true;
                            if(local) {
                                sendMessage("deselect \n");
                            }
                        }
                        else
                        {
                            moveMen(clickedIndex);
                        }
                    }
                    else
                    {
                        Log.d("OnTouchEvent", "lastMenIndex: gesetzt");
                        if(m_menPositions[clickedIndex].hasMen() && m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()))
                        {
                            m_lastMenIndex = clickedIndex;
                            if(local) {
                                sendMessage("select " + clickedIndex + " \n");
                            }
                        }
                    }

                }

                if(!isMenSelected() && m_menPositions[clickedIndex].hasMen() && m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()) && !deSelect)
                {
                    if(checkForMill(clickedIndex))
                    {
                        Log.d("OnTouchEvent", "CheckForMill");
                        m_clearMen = true;
                        if(local) {
                            //sendMessage("checkForMil "+clickedIndex+" \n");
                            //Log.d("handleClick", "Could not send");
                        }
                    }
                    else
                    {
                        if(isSetPhase())
                        {
                            if(menSet)
                            {
                                changeTeam();
                            }
                        }
                        else
                        {
                            changeTeam();
                        }
                    }
                }
            }

            drawView();

            if(!isSetPhase() && !m_clearMen && !deSelect && !couldMove())
            {
                gameOver();
            }
        }
    }

    private void touchLokal(MotionEvent event)
    {
        Log.d("OnTouchEvent", "ActionDown");
        int clickedIndex = checkClickPosition(event.getX(), event.getY());
        Log.d("OnTouchEvent", "ClickedIndex: " + clickedIndex);
        boolean deSelect = false;
        boolean menSet = false;

        if(isValidIndex(clickedIndex))
        {
            if(m_clearMen)
            {
                clearMen(clickedIndex);
            }
            else
            {
                if(isSetPhase())
                {
                    if(!m_menPositions[clickedIndex].hasMen())
                    {
                        setMen(clickedIndex);
                        increaseCurrentTeamCount();
                        menSet = true;
                    }
                }
                else
                {
                    menSet = true;
                    if(isMenSelected())
                    {
                        if(m_lastMenIndex == clickedIndex)
                        {
                            m_lastMenIndex = -1;
                            deSelect = true;
                        }
                        else
                        {
                            moveMen(clickedIndex);
                        }
                    }
                    else
                    {
                        Log.d("OnTouchEvent", "lastMenIndex: gesetzt");
                        if(m_menPositions[clickedIndex].hasMen() && m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()))
                        {
                            m_lastMenIndex = clickedIndex;
                        }
                    }

                }

                if(!isMenSelected() && m_menPositions[clickedIndex].hasMen() && m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()) && !deSelect && menSet)
                {
                    if(checkForMill(clickedIndex))
                    {
                        Log.d("OnTouchEvent", "CheckForMill");
                        m_clearMen = true;
                    }
                    else
                    {
                        if(isSetPhase())
                        {
                            if(menSet)
                            {
                                changeTeam();
                            }
                        }
                        else
                        {
                            changeTeam();
                        }
                    }
                }
            }

            drawView();

            if(!isSetPhase() && !m_clearMen && !deSelect && !couldMove())
            {
                gameOver();
            }
        }
    }

    private void clearMen(int clickedIndex)
    {
        Log.d("clearmen", "Clickindex: "+clickedIndex);
        if(m_menPositions[clickedIndex].hasMen() && !m_menPositions[clickedIndex].getImage().equals(getCurrentTeamImage()))
        {
            boolean removeMen = false;

            if(!checkForMill(clickedIndex))
            {
                removeMen = true;
            }
            else
            {
                boolean onlyMills = true;
                for(int i = 0; i < m_menPositions.length - 1; i++)
                {
                    if(m_menPositions[i].hasMen() && !m_menPositions[i].getImage().equals(getCurrentTeamImage()))
                    {
                        if(!checkForMill(i))
                        {
                            onlyMills = false;
                            break;
                        }
                    }
                }
                removeMen = onlyMills;
            }
            if(removeMen)
            {
                removeMen(clickedIndex);
                gameActivity.vibrate();
                decreaseEnemyMenCount();
                changeTeam();
                isGameFinished();
                m_clearMen = false;
            }
        }

    }

    private void decreaseEnemyMenCount()
    {
        if(getEnemyTeamNumber() == 0)
        {
            m_menCountTeamOne--;
        }
        else
        {
            m_menCountTeamTwo--;
        }
    }

    private void increaseCurrentTeamCount()
    {
        if(getEnemyTeamNumber() == 1)
        {
            m_menCountTeamOne++;
        }
        else
        {
            m_menCountTeamTwo++;
        }
    }

    private int getEnemyTeamNumber()
    {
        return (m_team + 1) % 2;
    }

    private void changeTeam()
    {
        m_team = getEnemyTeamNumber();

    }

    private boolean couldMove()
    {
        boolean movable = false;

        if(getCurrentTeamMenCount() < 4)
        {
            movable = true;
        }
        else
        {
            for(int i = 0; i < (m_menPositions.length - 1) && !movable; i++)
            {
                if(m_menPositions[i].hasMen() && m_menPositions[i].getImage().equals(getCurrentTeamImage()))
                {
                    for(int y : m_movePositions[i])
                    {
                        if(y > -1 && !m_menPositions[y].hasMen())
                        {
                            movable = true;
                            break;
                        }
                    }
                }
            }
        }

        return movable;
    }

    private boolean isValidIndex(int clickedIndex)
    {
        boolean valid = false;
        if(clickedIndex > -1 && clickedIndex <= MAXCLICKINDEX)
        {
            valid = true;
        }
        return valid;
    }

    private boolean isSetPhase()
    {
        boolean setPhase = false;
        if(m_turnCount < 18)
        {
            setPhase = true;
        }
        return setPhase;
    }

    private void moveMen(int clickedIndex)
    {
        if(!m_menPositions[clickedIndex].hasMen())
        {
            // Nachbarfelder vom Stein abfragen
            if(isValidPositions(clickedIndex))
            {
                setMen(clickedIndex);
                removeMen(m_lastMenIndex);
                if(!m_lokalGame && isMyTurn()) {
                    sendMessage("move " + m_lastMenIndex + " " + clickedIndex + " \n");
                }
                m_lastMenIndex = -1;
            }
        }
    }

    private void moveMen(int lastIndex, int clickedIndex) {
        m_lastMenIndex = lastIndex;
        moveMen(clickedIndex);
    }

    private int getCurrentTeamMenCount()
    {
        int count;
        if(m_team == 0)
        {
            count = m_menCountTeamOne;
        }
        else
        {
            count = m_menCountTeamTwo;
        }
        return count;
    }

    private boolean isValidPositions(int clickedIndex)
    {
        boolean isValid = false;

        if(getCurrentTeamMenCount() < 4)
        {
            isValid = true;
        }
        else
        {
            for(int i = 0; i < 4; i++)
            {
                if(clickedIndex == m_movePositions[m_lastMenIndex][i])
                {
                    isValid = true;
                    break;
                }
            }
        }

        return isValid;
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

    private void isGameFinished()
    {
        if(!isSetPhase() && getCurrentTeamMenCount() < 3)
        {
            gameOver();
        }
    }

    private void gameOver()
    {
        changeTeam();
        m_endGame = true;
        drawView();
        gameActivity.setTeamImage(getCurrentTeamImage());
        gameActivity.gameOver();
    }

    private boolean isMenSelected()
    {
        boolean selected = false;
        if(m_lastMenIndex > -1)
        {
            selected = true;
        }
        return selected;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.argb(255,92,141,229));
        canvas.drawBitmap(m_pitch, 0, m_edge, null);

        if(isMenSelected())
        {
            drawMenMark(canvas);
        }

        drawMens(canvas);

        drawGameInformation(canvas);

        if(m_endGame)
        {
            drawRect(canvas);
        }
    }

    private void drawMenMark(Canvas canvas)
    {
        float cx = (m_menPositions[m_lastMenIndex].getXPosition() + m_menPositions[m_lastMenIndex].getX2Position()) / 2;
        float cy = ((m_menPositions[m_lastMenIndex].getYPosition() + m_menPositions[m_lastMenIndex].getY2Position()) / 2)-2;
        float radius = ((m_menPositions[m_lastMenIndex].getXPosition() + m_menPositions[m_lastMenIndex].getX2Position()) / 2)-m_menPositions[m_lastMenIndex].getXPosition();
        Paint color = new Paint();
        color.setColor(Color.GREEN);

        canvas.drawCircle(cx, cy, radius, color);
    }

    private void drawMens(Canvas canvas)
    {
        Log.d("MRO GameView", "Check Positions");
        for(MenPosition mp : m_menPositions)
        {
            if(mp.hasMen())
            {
                canvas.drawBitmap(mp.getImage(), mp.getXPosition(), mp.getYPosition(), null);
                //Debug Code. Zeigt X1 (rot), X2 (gruen) und Mitte ((X1+X2)/2) (blau) der Men an
                /*{
                    Paint color = new Paint();
                    color.setColor(Color.RED);
                    canvas.drawCircle(mp.getXPosition(), mp.getYPosition(), 1, color);
                    color.setColor(Color.GREEN);
                    canvas.drawCircle(mp.getX2Position(), mp.getY2Position(), 1, color);
                    color.setColor(Color.BLUE);
                    canvas.drawCircle((mp.getXPosition()+mp.getX2Position())/2, (mp.getYPosition()+mp.getY2Position())/2, 1, color);
                }*/
            }
        }
    }

    private void drawGameInformation(Canvas canvas)
    {
        Paint color = new Paint();
        color.setColor(Color.WHITE);
        color.setTextSize(100);

        canvas.drawBitmap(getCurrentTeamImage(), 80, 100, null);
        if(m_clearMen)
        {
            canvas.drawText("darf einen Stein", 260, 180, color);
            canvas.drawText("entfernen", 260, 290, color);
        }
        else
        {
            canvas.drawText("ist am Zug" , 260, 210, color);
        }

        canvas.drawBitmap(m_menImage, 5, m_size.y - m_menImage.getHeight()-80, null);
        canvas.drawBitmap(m_men2Image, m_size.x-m_men2Image.getWidth()-5, m_size.y - m_men2Image.getHeight() - 80, null);

        color.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(m_menCountTeamOne), m_menImage.getWidth()/3, m_size.y - 30, color);
        color.setColor(Color.BLACK);
        canvas.drawText(String.valueOf(m_menCountTeamTwo), m_size.x-m_men2Image.getWidth()/3*2, m_size.y - 30, color);

        if(isSetPhase())
        {
            color.setColor(Color.RED);
            canvas.drawText("Setzphase", m_size.x / 2 - 210, m_size.y - 150, color);
        }
        else
        {
            color.setColor(Color.GREEN);
            canvas.drawText("Zugphase", m_size.x / 2 - 205, m_size.y - 150, color);
        }
    }

    private void drawRect(Canvas canvas)
    {
        final Paint paintRect = new Paint();
        paintRect.setARGB(188, 55, 55, 55);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paintRect);
    }

    @SuppressLint("WrongCall")
    private void drawView()
    {
        Canvas canvas = null;

        try {
            canvas = getHolder().lockCanvas();
            if(canvas != null) {
                this.onDraw(canvas);
            }
        } finally {
            if(canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
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

    private int[][] setMovePosition()
    {
        int[][] positions = new int[24][4];

        positions[0][0] = 1;
        positions[0][1] = 9;
        positions[0][2] = -1;
        positions[0][3] = -1;

        positions[1][0] = 0;
        positions[1][1] = 2;
        positions[1][2] = 4;
        positions[1][3] = -1;

        positions[2][0] = 1;
        positions[2][1] = 14;
        positions[2][2] = -1;
        positions[2][3] = -1;

        positions[3][0] = 4;
        positions[3][1] = 10;
        positions[3][2] = -1;
        positions[3][3] = -1;

        positions[4][0] = 1;
        positions[4][1] = 3;
        positions[4][2] = 5;
        positions[4][3] = 7;

        positions[5][0] = 4;
        positions[5][1] = 13;
        positions[5][2] = -1;
        positions[5][3] = -1;

        positions[6][0] = 11;
        positions[6][1] = 7;
        positions[6][2] = -1;
        positions[6][3] = -1;

        positions[7][0] = 4;
        positions[7][1] = 6;
        positions[7][2] = 8;
        positions[7][3] = -1;

        positions[8][0] = 7;
        positions[8][1] = 12;
        positions[8][2] = -1;
        positions[8][3] = -1;

        positions[9][0] = 0;
        positions[9][1] = 10;
        positions[9][2] = 21;
        positions[9][3] = -1;

        positions[10][0] = 3;
        positions[10][1] = 9;
        positions[10][2] = 11;
        positions[10][3] = 18;

        positions[11][0] = 6;
        positions[11][1] = 10;
        positions[11][2] = 15;
        positions[11][3] = -1;

        positions[12][0] = 8;
        positions[12][1] = 13;
        positions[12][2] = 17;
        positions[12][3] = -1;

        positions[13][0] = 5;
        positions[13][1] = 12;
        positions[13][2] = 14;
        positions[13][3] = 20;

        positions[14][0] = 2;
        positions[14][1] = 13;
        positions[14][2] = 23;
        positions[14][3] = -1;

        positions[15][0] = 11;
        positions[15][1] = 16;
        positions[15][2] = -1;
        positions[15][3] = -1;

        positions[16][0] = 15;
        positions[16][1] = 17;
        positions[16][2] = 19;
        positions[16][3] = -1;

        positions[17][0] = 12;
        positions[17][1] = 16;
        positions[17][2] = -1;
        positions[17][3] = -1;

        positions[18][0] = 10;
        positions[18][1] = 19;
        positions[18][2] = -1;
        positions[18][3] = -1;

        positions[19][0] = 16;
        positions[19][1] = 18;
        positions[19][2] = 20;
        positions[19][3] = 22;

        positions[20][0] = 13;
        positions[20][1] = 19;
        positions[20][2] = -1;
        positions[20][3] = -1;

        positions[21][0] = 9;
        positions[21][1] = 22;
        positions[21][2] = -1;
        positions[21][3] = -1;

        positions[22][0] = 19;
        positions[22][1] = 21;
        positions[22][2] = 23;
        positions[22][3] = -1;

        positions[23][0] = 14;
        positions[23][1] = 22;
        positions[23][2] = -1;
        positions[23][3] = -1;


        return positions;
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

    private void setMen(int index)
    {
        m_menPositions[index].setImage(getCurrentTeamImage());
        m_turnCount++;
    }

    private void removeMen(int index)
    {
        if(!m_lokalGame && isMyTurn()) {
            sendMessage("unset "+index+" \n");
            //Log.d("handleClick", "Could not send");
        }
        m_menPositions[index].setImage(null);
    }

    @Override
    public void onDetachedFromWindow() {
        if(sendTimer != null) {
            sendTimer.purge();
            sendTimer = null;
        }
    }
}
