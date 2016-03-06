package joel.duet.musica;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.csounds.CsoundObj;

import java.util.LinkedList;

import joel.duet.musica.databinding.PatternFragmentBinding;

/**
 *
 * Created by joel on 22/01/16 at 23:25 at 14:27.
 */
public final class PatternFragment extends FragmentPlus {
    private PatternView patternview;
    private final LinkedList<String> instrumentIds = new LinkedList<>();
    private static final String TAG = "PatternFragment";

    private static MainActivity activity;
    private static CsoundObj csoundObj;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        PatternFragmentBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.pattern_fragment, container, false);
        final ImageButton arpeggio_button = binding.arpeggio;
        final ToggleButton mode_button = binding.mode;

        binding.setUser(PatternView.user);
        mode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PatternView.user.edit_mode.set(!PatternView.user.edit_mode.get());
            }
        });

        final ToggleButton loudnessButton = binding.loudness;
                loudnessButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        PatternView.user.loudness_mode.set(isChecked);
                    }
                });

        for(String instr:CSD.mapInstr.keySet()) instrumentIds.add(instr);

        final ArrayAdapter<String> instruments_adapter =
                new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item,
                        instrumentIds);
        final Spinner instrument_spinner = binding.instrument;
        instrument_spinner.setAdapter(instruments_adapter);
        instrument_spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        patternview.pattern.setInstr(CSD.mapInstr.keySet().toArray(
                                new String[CSD.getNbInstruments()])[i]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

        arpeggio_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                InputTextDialogFragment commandLabDialog = new InputTextDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("state", "PATTERN");
                commandLabDialog.setArguments(bundle);
                commandLabDialog.show(fragmentManager, "fragment_command_lab");
            }
        });

        binding.recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                patternview.pattern.mPosX = 0;
                patternview.pattern.mPosY = Default.initial_pattern_height;
                patternview.invalidate();
            }
        });

        final Spinner resolution_spinner = binding.resolution;
        SimpleImageArrayAdapter adapter =
                new SimpleImageArrayAdapter(getContext(),Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);

        resolution_spinner.setSelection(getArguments().getInt("resolution_index"));

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

        binding.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String csd = Score.sendPatterns(patternview.pattern.singleton(), false,
                        patternview.pattern.start,
                        patternview.pattern.finish);
                Log.i(TAG, csd);
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        binding.inContext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String csd = Score.sendPatterns(Score.allPatterns(), false,
                        patternview.pattern.start,
                        patternview.pattern.finish);
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        binding.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
            }
        });

        patternview = binding.patternView;
        PatternView.note_loudness = binding.noteLoudness;

        PatternView.user.edit_mode.set(mode_button.isChecked());

        int instr_selected = 0;
        String names[] = CSD.mapInstr.keySet().toArray(new String[CSD.getNbInstruments()]);
        String name = getArguments().getString("instr_name");
        while(instr_selected<CSD.getNbInstruments() && !names[instr_selected].equals(name))
            instr_selected ++;
        instrument_spinner.setSelection(instr_selected);

        return binding.getRoot();
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        String[] command = inputText.split(" +");
        int n = patternview.pattern.getNbOfNotes();
        int period = patternview.pattern.getNote(n-1).onset+patternview.pattern.getNote(n-1).duration;

        if(command[0].equals("repeat")){
            boolean roomLeft = true;
            int mark = period;

            while(roomLeft){
                for(int i=0; i<n; i++) {
                    Pattern.Note note = patternview.pattern.getNote(i);
                    if (mark + note.onset + note.duration <
                            patternview.pattern.finish - patternview.pattern.start)
                        patternview.pattern.createNote(
                                mark + note.onset,
                                note.duration,
                                note.pitch,
                                note.loudness);
                    else roomLeft = false;
                }
                if(roomLeft) mark += period;
            }
        } else if(command[0].equals("transpose")){
            try {
                int delta = Integer.parseInt(command[1]);
                for (int i = 0; i < n; i++) {
                    Pattern.Note note = patternview.pattern.getNote(i);
                    note.pitch += delta;
                }
            } catch (NumberFormatException exp){exp.printStackTrace();}
        }

        patternview.invalidate();
    }
}
