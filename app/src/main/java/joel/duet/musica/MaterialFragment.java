package joel.duet.musica;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import joel.duet.musica.databinding.MaterialFragmentBinding;

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
        MaterialFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.material_fragment, container, false);
        globals = binding.material;
        globals.setText(CSD.globals);
        return binding.getRoot();
    }
}
