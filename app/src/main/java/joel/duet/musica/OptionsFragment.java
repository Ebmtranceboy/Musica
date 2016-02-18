package joel.duet.musica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 *
 * Created by joel on 16/02/16 at 15:46 at 16:06 at 23:28.
 */
public final class OptionsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {

        View view = inflater.inflate(R.layout.options_fragment, container, false);

        EditText options_tempo = (EditText) view.findViewById(R.id.options_tempo);
        options_tempo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try{

                    Float tempo = Float.parseFloat(editable.toString());
                            CSD.tempo_ratio = tempo / 60.0;
                }catch (NumberFormatException ex){
                    ex.printStackTrace();
                }
            }
        });

        String format = getResources().getString(R.string.floating_point_format);
        options_tempo.setText(String.format(format,CSD.tempo_ratio * 60));

        return view;
    }
}
