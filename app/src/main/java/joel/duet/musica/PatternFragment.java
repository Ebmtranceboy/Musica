package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.csounds.CsoundObj;

import java.util.LinkedList;

/**
 *
 * Created by joel on 22/01/16 at 23:25 at 14:27.
 */
public final class PatternFragment extends Fragment {
    private PatternView patternview;
    private final LinkedList<String> instrumentIds = new LinkedList<>();
    public int instr_selected = 0 ;

    static public MainActivity activity;
    static public CsoundObj csoundObj;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.pattern_fragment, container, false);


        view.findViewById(R.id.mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PatternView.edit_mode = !PatternView.edit_mode;
            }
        });

        for(String instr:CSD.mapInstr.keySet()) instrumentIds.add(instr);
        //for(int i=1;i<= CSD.getNbInstruments();i++) instrumentIds.add(i);
        final ArrayAdapter<String> instruments_adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item,
                        instrumentIds);
        final Spinner instrument_spinner = (Spinner)view.findViewById(R.id.instrument);
        instrument_spinner.setAdapter(instruments_adapter);
        instrument_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        patternview.pattern.setInstr(CSD.mapInstr.keySet().toArray(new String[CSD.getNbInstruments()])[i]);
                        instr_selected = i;
                        /*Instrument.updateNative(i + 1);
                        if (Score.is_score_loop)
                            Score.sendPatterns(Score.allPatterns(),
                                    patternview.pattern.start,
                                    patternview.pattern.finish);
                        else
                            Score.sendPatterns(patternview.pattern.singleton(),
                                    patternview.pattern.start,
                                    patternview.pattern.finish);
                                    */
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

        view.findViewById(R.id.preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Engine.looped(false);
                Engine.stopped(false);*/
                String csd = Score.sendPatterns(patternview.pattern.singleton(),
                        patternview.pattern.start,
                        patternview.pattern.finish);
                Score.is_score_loop = false;
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        view.findViewById(R.id.loop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Engine.looped(true);
                Engine.stopped(false);*/
                String csd = Score.sendPatterns(patternview.pattern.singleton(),
                        patternview.pattern.start,
                        patternview.pattern.finish);
                Score.is_score_loop = false;
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        view.findViewById(R.id.score_loop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Engine.looped(true);
                Engine.stopped(false);*/
                String csd = Score.sendPatterns(Score.allPatterns(),
                        patternview.pattern.start,
                        patternview.pattern.finish);
                Score.is_score_loop = true;
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Engine.looped(false);
                Engine.stopped(true);*/
                csoundObj.stop();
            }
        });

        // on Resume:

        patternview = new PatternView(getContext());

        final FrameLayout patternArea=
                (FrameLayout)view.findViewById(R.id.pattern_view);
        patternArea.addView(patternview);

        PatternView.edit_mode = ((Switch)view.findViewById(R.id.mode)).isChecked();

        ((Spinner)view.findViewById(R.id.instrument)).setSelection(instr_selected);

        final Spinner resolution_spinner = (Spinner) view.findViewById(R.id.resolution);
        resolution_spinner.setSelection(ScoreView.resolution);
        SimpleImageArrayAdapter adapter = new SimpleImageArrayAdapter(getContext(),Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                patternview.pattern.resolution = i;
                patternview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return view;
    }

}
