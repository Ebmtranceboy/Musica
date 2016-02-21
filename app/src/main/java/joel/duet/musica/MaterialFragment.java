package joel.duet.musica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 *
 * Created by joel on 21/02/16 at 18:00 at 18:01.
 */
public final class MaterialFragment extends Fragment{
    private EditText globals;

    public String getGlobals() {
        return globals.getText().toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.material_fragment, container, false);
        globals = (EditText) view.findViewById(R.id.material);
        globals.setText(CSD.globals);
        return view;
    }
}
