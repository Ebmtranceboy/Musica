package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ContextThemeWrapper;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

/**
 *
 * Created by joel on 21/01/16 at 08:16 at 14:17 at 14:41.
 */
public final class FXFragment extends FragmentPlus {
    static private MainActivity activity;
    //private static final String TAG = "FX";
    static private ArrayAdapter<String> effect_adapter;
    private static String effectName;
    static private List<String> listEffect;
    private File effect_file;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.fx_fragment, container, false);

        final ListView listEffectView = (ListView) view.findViewById(R.id.listEffect);
        listEffect = new ArrayList<>();
        listEffect.addAll(CSD.mapFX.keySet());
        effect_adapter = new ArrayAdapter<>(activity.getBaseContext(), android.R.layout.simple_spinner_item, listEffect);
        listEffectView.setAdapter(effect_adapter);

        listEffectView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                effectName = listEffect.get(i);
                bundle.putString("effectName", effectName);
                EffectFragment fragment = new EffectFragment();
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        fragment,
                        "EFFECT").commit();
                MainActivity.toolbar.setTitle(effectName);
                MainActivity.currentFragment = MainActivity.State.EFFECT;
                effectName = null;
            }
        });

        final Button new_effect = (Button) view.findViewById(R.id.new_effect);
        new_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putString("state", "FX");
                InputTextDialogFragment editNameDialog = new InputTextDialogFragment();
                editNameDialog.setArguments(bundle);
                editNameDialog.show(fragmentManager, "fragment_edit_name");
            }
        });

        final Button import_effect = (Button) view.findViewById(R.id.import_effect);
        import_effect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(getContext(), R.style.csoundAlertDialogStyle),
                        "FileOpen..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                OnFileChosen(new File(chosenDir));
                            }
                        }
                );
                if (effect_file != null) fileOpenDialog.default_file_name = effect_file.getParent();
                else
                    fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });


        return view;
    }

    private class ParseEffect {
        String name, body;
        private final java.util.regex.Pattern header = java.util.regex.Pattern.compile(" *opcode +(\\w+)\\b");
        private final java.util.regex.Pattern footer = java.util.regex.Pattern.compile(" *endop\\b");

        ParseEffect(String text) {
            String[] lines = text.split("\n");
            int i = 0;
            while (i < lines.length) {
                Matcher matcher = header.matcher(lines[i]);
                if (matcher.find()) {
                    name = matcher.group(1);
                    break;
                }
                i++;
            }
            i++;

            body = "";
            while (i < lines.length) {
                Matcher matcher = footer.matcher(lines[i]);
                if(matcher.find()) break;
                else body += lines[i] + "\n";
                i++;
            }
        }
    }

    private void OnFileChosen(File file) {
        effect_file = file;
        String effect_text = activity.csoundUtil.getExternalFileAsString(file.getAbsolutePath());
        ParseEffect effect = new ParseEffect(effect_text);
        effectName = effect.name;
        Matrix.getInstance().spy();
        CSD.mapFX.put(effectName, new CSD.Content(effect.body,1.0,1.0));
        Matrix.getInstance().update();
        listEffect.add(effectName);
        effect_adapter.notifyDataSetChanged();

        effectName = null;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        effectName = inputText;
        Matrix.getInstance().spy();
        CSD.mapFX.put(effectName, new CSD.Content("xout 0, 0",1.0,1.0));
        Matrix.getInstance().update();
        listEffect.add(effectName);
        effect_adapter.notifyDataSetChanged();

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putString("effectName", effectName);
        EffectFragment fragment = new EffectFragment();
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                fragment,
                "EFFECT").commit();
        MainActivity.toolbar.setTitle(effectName);
        MainActivity.currentFragment = MainActivity.State.EFFECT;
        effectName = null;

    }
}
