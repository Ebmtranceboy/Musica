package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.csounds.CsoundObj;
import com.csounds.bindings.ui.CsoundSliderBinding;

/**
 *
 * Created by joel on 12/01/16 at 23:16 at 11:00 at 12:39.
 */
public final class LiveFragment extends Fragment {

    static private MainActivity activity;
    static private CsoundObj csoundObj;
    private KeyboardView keyboard;
    private final int[] touchIds = new int[10];
    private final float[] touchX = new float[10];
    private final float[] touchY = new float[10];
    static private boolean loudness_mode;
    static private boolean solo_mode;
    private static SeekBar ktrlx, ktrly;

    private void ktrlBindings(){
        csoundObj.addBinding(new CsoundSliderBinding(ktrlx, "ktrlx", 0, 1));
        csoundObj.addBinding(new CsoundSliderBinding(ktrly, "ktrly", 0, 1));
//        csoundObj.addBinding(new CsoundButtonBinding(button1, "button1", 1));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
        csoundObj.stop();
        csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.part()));
    }

    //Button startCsound, stopCsound, button1;
    //SeekBar seekbar1;
    //private static final String TAG = "Live";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {

        View view = inflater.inflate(R.layout.live_fragment, container, false);
        for (int i = 0; i < touchIds.length; i++) {
            touchIds[i] = -1;
            touchX[i] = -1;
            touchY[i] = -1;
        }
        // populate oct spinner
        final Spinner select_oct = (Spinner) view.findViewById(R.id.select_oct);
        ArrayAdapter<CharSequence> oct_adapter = ArrayAdapter.createFromResource(activity, R.array.oct_array, android.R.layout.simple_spinner_item);
        oct_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_oct.setAdapter(oct_adapter);
        select_oct.setSelection(7);

        // populate instr spinner
        final Spinner select_instr = (Spinner) view.findViewById(R.id.select_instr);
        ArrayAdapter<CharSequence> instr_adapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_item, CSD.mapInstr.keySet().toArray(new CharSequence[CSD.getNbInstruments()]));
        select_instr.setAdapter(instr_adapter);

        final ToggleButton loudnessButton = (ToggleButton) view.findViewById(R.id.loudness);
        loudnessButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loudness_mode = isChecked;
                if (isChecked)
                    loudnessButton.setBackgroundResource(R.drawable.ic_loudness_on);
                else loudnessButton.setBackgroundResource(R.drawable.ic_loudness_off);
            }
        });

        final ToggleButton soloModeButton = (ToggleButton) view.findViewById(R.id.solo);
        soloModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                solo_mode = isChecked;
                if (isChecked)
                    soloModeButton.setBackgroundResource(R.drawable.ic_read_partition);
                else soloModeButton.setBackgroundResource(R.drawable.ic_play_in_context);
            }
        });

        ktrlx = (SeekBar) view.findViewById(R.id.ktrlx);
        ktrly = (SeekBar) view.findViewById(R.id.ktrly);
        ktrlBindings();

        final ImageButton recordButton = (ImageButton) view.findViewById(R.id.live_record);
        ImageButton playButton = (ImageButton) view.findViewById(R.id.live_play);
        ImageButton patternizeButton = (ImageButton) view.findViewById(R.id.patternize);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_recording);
                csoundObj.stop();
                ktrlBindings();
                String csd = solo_mode ? CSD.recordPart((String) select_instr.getSelectedItem()) :
                        Score.sendPatternsForRecord((String) select_instr.getSelectedItem(), Score.allPatterns());
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);
                csoundObj.stop();
                ktrlBindings();
                String csd = solo_mode ? CSD.part() : Score.sendPatterns(Score.allPatterns(), 0);
                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                csoundObj.sendScore(activity.csoundUtil.getExternalFileAsString(Default.score_events_absoluteFilePath).replaceAll("i +\\w+ +", "i\"" + select_instr.getSelectedItem() + "\" "));
            }
        });

        patternizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);
                activity.csoundUtil.patternize((String) select_instr.getSelectedItem(), solo_mode);
            }
        });

        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordButton.setImageResource(R.drawable.ic_menu_live);
                csoundObj.stop();
                ktrlBindings();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.part()));
            }
        });
        // define keyboard

        keyboard = (KeyboardView) view.findViewById(R.id.Keyboard);
        keyboard.draw(-1, -1, false);
        keyboard.show();

        keyboard.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int key;
                final int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        for (int i = 0; i < event.getPointerCount(); i++) {
                            int pointerId = event.getPointerId(i);
                            int id = getTouchId(pointerId);

                            if (id == -1) {

                                id = getTouchIdAssignment();

                                if (id != -1) {
                                    touchIds[id] = pointerId;
                                    touchX[id] = event.getX(i);
                                    touchY[id] = event.getY(i);
                                    key = keyboard.draw(touchX[id], touchY[id], true);
                                    //Log.i(TAG, "key=" + key);

                                    csoundObj.sendScore("i\"Voicer\" 0 0 \""
                                            + select_instr.getSelectedItem() + "\" " + (id + 1) + " "
                                            + select_oct.getSelectedItem() + "." + (key < 10 ? "0" : "") + key + " "
                                            + (loudness_mode ?
                                            CSD.pressure2dB(event.getPressure()) :
                                            CSD.defaultLoudness2dB()));
                                }
                            }
                        }
                        keyboard.show();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:

                        int activePointerIndex = event.getActionIndex();
                        int pointerId = event.getPointerId(activePointerIndex);

                        int id = getTouchId(pointerId);
                        if (id != -1) {
                            touchIds[id] = -1;

                            csoundObj.sendScore("i\"Silencer\" 0 0 \"" + select_instr.getSelectedItem() + "\" " + (id + 1));

                            keyboard.draw(touchX[id], touchY[id], false);
                            keyboard.show();
                        }

                        break;
                }
                return true;
            }
        });

        loudness_mode = false;

        return view;
    }

    private int getTouchIdAssignment() {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == -1) {
                return i;
            }
        }
        return -1;
    }

    private int getTouchId(int touchId) {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == touchId) {
                return i;
            }
        }
        return -1;
    }
}
