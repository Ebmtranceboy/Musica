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
    // TODO : change presentation to listview with editable + checkable items
    // TODO : PMsine (at least for Ensemble)
    /*
    opcode PMsine,a,kk
        kcps, kwet xin
        setksmps 1

        kph phasor kcps
        krat = (1-kwet)/4

        if (kph&lt;0.5-krat) then
          kramp = kph*(0.25-krat)/(0.5-krat)
        elseif (kph&lt;0.5+krat) then
          kramp = (kph-0.5)*(2*krat-0.5)/(2*krat)
        else
          kramp = (kph-1)*(0.25-krat)/(0.5-krat)
        endif
        ksig tablei kph-kramp,-1,1,0,1
        asig = ksig
        xout asig
     endop
*/

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
