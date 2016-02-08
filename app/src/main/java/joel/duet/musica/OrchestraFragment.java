package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by joel on 14/01/16 at 11:43 at 11:43 at 23:17 at 12:48.
 */
public final class OrchestraFragment extends FragmentPlus {
    //TODO implement import button
    //TODO implement add to lib
    static private MainActivity activity;
    //private static final String TAG = "Orchestra";
    static private ArrayAdapter<String> instr_adapter;
    static String instrName;
    static private List<String> listInstr;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.orchestra_fragment, container, false);

        final ListView listInstrView = (ListView) view.findViewById(R.id.listInstr);
        listInstr = new ArrayList<>();
        listInstr.addAll(CSD.mapInstr.keySet());
        instr_adapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_item, listInstr);
        listInstrView.setAdapter(instr_adapter);

        listInstrView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                instrName = listInstr.get(i);
                bundle.putString("instrName", instrName);
                InstrumentFragment fragment = new InstrumentFragment();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        fragment,
                        "INSTRUMENT").commit();
                MainActivity.toolbar.setTitle(instrName);
                MainActivity.currentFragment = MainActivity.State.INSTRUMENT;
                instrName = null;
            }
        });

        final Button new_instr = (Button) view.findViewById(R.id.new_instr);
        new_instr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("state", "ORCHESTRA");
                InputTextDialogFragment editNameDialog = new InputTextDialogFragment();
                editNameDialog.setArguments(bundle);
                editNameDialog.show(fragmentManager, "fragment_edit_name");
            }
        });

        return view;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        instrName = inputText;
        Matrix.getInstance().spy();
        CSD.mapInstr.put(instrName, "");
        Matrix.getInstance().update();
        listInstr.add(instrName);
        instr_adapter.notifyDataSetChanged();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString("instrName", instrName);
        InstrumentFragment fragment = new InstrumentFragment();
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                fragment,
                "INSTRUMENT").commit();
        MainActivity.toolbar.setTitle(instrName);
        MainActivity.currentFragment = MainActivity.State.INSTRUMENT;
        instrName = null;

    }
}




/*

        playButton = (Button) view.findViewById(R.id.live_play);
        recordButton = (Button) view.findViewById(R.id.live_record);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
                csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.part()));
                csoundObj.sendScore(activity.csoundUtil.getExternalFileAsString("unisonMelody.txt"));
            }
        });

        view.post(new Runnable() {
            @Override
            public void run() { // dimensions of view are ready
              // populate oct spinner
                final Spinner select_oct = (Spinner) view.findViewById(R.id.select_oct);
                oct_adapter = ArrayAdapter.createFromResource(activity, R.array.oct_array, android.R.layout.simple_spinner_item);
                oct_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                select_oct.setAdapter(oct_adapter);
                select_oct.setSelection(8);

                // populate instr spinner

                final Spinner select_instr = (Spinner) view.findViewById(R.id.select_instr);
                instr_adapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_item, CSD.mapInstr.keySet().toArray(new CharSequence[CSD.mapInstr.keySet().size()]));
                select_instr.setAdapter(instr_adapter);

                recordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        csoundObj.stop();
                        csoundObj.startCsound(activity.csoundUtil.createTempFile(CSD.recordPart((String) select_instr.getSelectedItem())));
                    }
                });

                keyboardArea = (LinearLayout) view.findViewById(R.id.KeyboardArea);

                KeyboardView.keyboard_width = keyboardArea.getWidth();// width must be declare as a field
                KeyboardView.keyboard_height = keyboardArea.getHeight();// height must be declare as a field

                keyboard = new KeyboardView(activity);
                keyboardArea.addView(keyboard);
                keyboard.draw(-1, -1);
                keyboard.show();

                keyboardArea.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int key;
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                float x = event.getX();
                                float y = event.getY();
                                key = keyboard.draw(x, y);
                                Log.i(TAG, "key=" + key);

                                csoundObj.sendScore("i\"" + select_instr.getSelectedItem() + "\" 0 0.5 " + select_oct.getSelectedItem() + "." + (key < 10 ? "0" : "") + key + " -12");
                                keyboard.show();
                                break;

                            case MotionEvent.ACTION_UP:
                                keyboard.draw(-1, -1);
                                keyboard.show();
                                break;
                        }
                        return true;
                    }
                });
            }
        });

        */

