package joel.duet.musica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 * Created by joel on 16/01/16 at 00:17 at 06:52.
 */
public final class WelcomeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        return inflater.inflate(R.layout.welcome_fragment, container, false);

    }

}
