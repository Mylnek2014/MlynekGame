package mro.de.mlynek;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sony on 09.09.2014.
 */
public class MenuFragment extends Fragment implements View.OnClickListener
{
    private ImageView imageView;
    private ImageButton play;
    private View.OnClickListener clickListener;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_menu, container, false);
        imageView = (ImageView) v.findViewById(R.id.backgroudView);
        play = (ImageButton) v.findViewById(R.id.play);
        play.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        imageView.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.bgmenu, size.x, size.y));
        play.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.startbtn, size.x / 2, size.y / 2 / 2));
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            clickListener = (View.OnClickListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }

    }

    @Override
    public void onClick(View view)
    {
        clickListener.onClick(view);
    }
}
