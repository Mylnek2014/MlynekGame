package mro.de.mlynek;

import android.graphics.Bitmap;

/**
 * Created by Sony on 15.09.2014.
 */
public class MenPosition
{
    private int m_XPosition;
    private int m_X2Position;
    private int m_YPosition;
    private int m_Y2Position;
    private Bitmap m_image;
    

    public MenPosition(int x, int y, int edgeSize)
    {
        edgeSize = edgeSize / 2;
        m_XPosition = x - edgeSize;
        m_YPosition = y - edgeSize;
        m_X2Position = x + edgeSize;
        m_Y2Position = y + edgeSize;
        m_image = null;
    }

    public int getXPosition()
    {
        return m_XPosition;
    }

//    public void setXPosition(int m_XPosition)
//    {
//        this.m_XPosition = m_XPosition;
//    }

    public int getX2Position()
    {
        return m_X2Position;
    }

//    public void setX2Position(int m_X2Position) {
//        this.m_X2Position = m_X2Position;
//    }

    public int getYPosition()
    {
        return m_YPosition;
    }

//    public void setM_YPosition(int m_YPosition) {
//        this.m_YPosition = m_YPosition;
//    }

    public int getY2Position() {
        return m_Y2Position;
    }

//    public void setM_Y2Position(int m_Y2Position) {
//        this.m_Y2Position = m_Y2Position;
//    }

    public boolean hasMen()
    {
        return m_image != null;
    }

    public boolean isTouched(float x, float y)
    {
        return x > m_XPosition && x < m_X2Position && y > m_YPosition && y < m_Y2Position;
    }

    public void setImage(Bitmap image)
    {
        m_image = image;
    }

    public Bitmap getImage()
    {
        return m_image;
    }


}
