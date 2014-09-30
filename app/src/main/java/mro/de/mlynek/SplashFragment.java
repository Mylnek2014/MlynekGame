package mro.de.mlynek;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Sony on 08.09.2014.
 */
public class SplashFragment extends Fragment
{
    private ImageView imageView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_splash, container, false);
        imageView = (ImageView) v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        imageView.setImageBitmap(Util.decodeSampledBitmapFromResource(getResources(), R.drawable.starton, imageView.getMaxWidth(), imageView.getMaxHeight()));
    }
}
