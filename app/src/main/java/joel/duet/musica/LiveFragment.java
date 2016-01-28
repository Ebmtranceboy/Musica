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
import android.widget.Button;
import android.widget.Spinner;

import com.csounds.CsoundObj;

/**
 *
 * Created by joel on 12/01/16 at 23:16 at 11:00.
 */
public final class LiveFragment extends Fragment {

    // TODO : implement ktrl sliders

    static private MainActivity activity;
    static private CsoundObj csoundObj;
    KeyboardView keyboard;
    int touchIds[] = new int[10];
    float touchX[] = new float[10];
    float touchY[] = new float[10];

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
        csoundObj.stop();
        csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.csd()));
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
        ArrayAdapter<CharSequence> instr_adapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_item, CSD.mapInstr.keySet().toArray(new CharSequence[CSD.mapInstr.keySet().size()]));
        select_instr.setAdapter(instr_adapter);

        Button playButton = (Button) view.findViewById(R.id.live_play);
        Button recordButton = (Button) view.findViewById(R.id.live_record);
        Button patternizeButton = (Button) view.findViewById(R.id.patternize);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.csd()));
                csoundObj.sendScore(activity.csoundUtil.getExternalFileAsString("unisonMelody.txt").replaceAll("i +\\w+ +", "i\"" + select_instr.getSelectedItem() + "\" "));
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.recordPart((String) select_instr.getSelectedItem())));
            }
        });

        patternizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.csoundUtil.patternize((String)select_instr.getSelectedItem());
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

                                    csoundObj.sendScore("i\"Voicer\" 0 0 \"" + select_instr.getSelectedItem() + "\" " + (id+1) + " " + select_oct.getSelectedItem() + "." + (key < 10 ? "0" : "") + key + " -12");

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

                            csoundObj.sendScore("i\"Silencer\" 0 0 \"" + select_instr.getSelectedItem() + "\" " + (id+1));

                            keyboard.draw(touchX[id], touchY[id], false);
                            keyboard.show();
                        }

                        break;
                }
                return true;
            }
        });


        return view;
    }

    protected int getTouchIdAssignment() {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == -1) {
                return i;
            }
        }
        return -1;
    }

    protected int getTouchId(int touchId) {
        for (int i = 0; i < touchIds.length; i++) {
            if (touchIds[i] == touchId) {
                return i;
            }
        }
        return -1;
    }
}

        /*
        startCsound = (Button) findViewById(R.id.StartCsound);
        stopCsound = (Button) findViewById(R.id.StopCsound);
        button1 = (Button) findViewById(R.id.Button1);
        seekbar1 = (SeekBar) findViewById(R.id.SeekBar1);

        startCsound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.addBinding(new CsoundSliderBinding(seekbar1, "seekbar1", 0, 1));
                csoundObj.addBinding(new CsoundButtonBinding(button1, "button1", 1));

                csoundObj.startCsound(createTempFile(getResourceFileAsString(R.raw.statevar)));


                stopCsound.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        csoundObj.stop();
                    }
                });
            }
        });


        }



*/