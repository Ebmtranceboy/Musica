package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.csounds.CsoundObj;


/**
 *
 * Created by joel on 17/02/16 at 10:37 at 11:23 at 09:31 at 10:12 at 18:53.
 */
public final class MasterFragment extends Fragment{
    private CsoundObj csoundObj;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        csoundObj = MainActivity.csoundObj;
    }

    private void registerLine(LayoutInflater inflater, ViewGroup container, final String componentName, LinearLayout verticalLayout, final int formatId, final boolean isInstr) {
        SeekBar seekBar;

        LinearLayout line = (LinearLayout) inflater.inflate(R.layout.master_line, container, false);
        TextView lineName = (TextView) line.findViewById(R.id.component_name);
        final String format = getResources().getString(formatId);
        lineName.setText(String.format(format, componentName));

        final TextView gaindb = (TextView) line.findViewById(R.id.gain_db);
        seekBar = (SeekBar) line.findViewById(R.id.gain);
        double val;

        int color;
        if (isInstr) {
            color = Default.instrument_color;
            if (formatId == R.string.master_line_L_format)
                val = CSD.mapInstr.get(componentName).gainL;
            else val = CSD.mapInstr.get(componentName).gainR;
        } else {
            if (componentName.equals("Master")) {
                color = Default.master_color;
                if (formatId == R.string.master_line_L_format)
                    val = CSD.master_gain_L;
                else val = CSD.master_gain_R;
            } else {
                color = Default.effect_color;
                if (formatId == R.string.master_line_L_format)
                    val = CSD.mapFX.get(componentName).gainL;
                else val = CSD.mapFX.get(componentName).gainR;
            }
        }
        seekBar.setProgress((int) Math.round(val* seekBar.getMax()));
        lineName.setBackgroundColor(color);
        gaindb.setText(String.format("%1s", val));

        csoundObj.addBinding(new SlidingCsoundBindingUI(seekBar, "ktrl_" + String.format(format, componentName), 0, 1, gaindb, isInstr, formatId, componentName));

        verticalLayout.addView(line);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.master_fragment, container, false);

        LinearLayout verticalLayout = (LinearLayout) view.findViewById(R.id.master);

        String[] instrNames = CSD.mapInstr.keySet().toArray(new String[CSD.getNbInstruments()]);
        for (int i = 0; i < CSD.getNbInstruments(); i++) {
            registerLine(inflater, container, instrNames[i], verticalLayout, R.string.master_line_L_format, true);
            registerLine(inflater, container, instrNames[i], verticalLayout, R.string.master_line_R_format, true);
        }

        String[] effectNames = CSD.mapFX.keySet().toArray(new String[CSD.getNbEffects()]);
        for (int i = 0; i < CSD.getNbEffects(); i++) {
            registerLine(inflater, container, effectNames[i], verticalLayout, R.string.master_line_L_format, false);
            registerLine(inflater, container, effectNames[i], verticalLayout, R.string.master_line_R_format, false);
        }

        registerLine(inflater, container, "Master", verticalLayout, R.string.master_line_L_format, false);
        registerLine(inflater, container, "Master", verticalLayout, R.string.master_line_R_format, false);

        return view;
    }

}
