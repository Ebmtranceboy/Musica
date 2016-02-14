package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
//import android.util.Log;
import android.support.v7.view.ContextThemeWrapper;
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

/**
 *
 * Created by joel on 14/01/16 at 11:43 at 11:43 at 23:17 at 12:48 at 10:49 at 14:39 at 15:33.
 */
public final class OrchestraFragment extends FragmentPlus {
    static private MainActivity activity;
    //private static final String TAG = "Orchestra";
    static private ArrayAdapter<String> instr_adapter;
    private static String instrName;
    static private List<String> listInstr;
    private File instr_file;

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

        final Button import_instr = (Button) view.findViewById(R.id.import_instr);
        import_instr.setOnClickListener(new View.OnClickListener() {
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
                if (instr_file != null) fileOpenDialog.default_file_name = instr_file.getParent();
                else
                    fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });

        return view;
    }

    private class ParseInstr {
        String name, body;

        protected ParseInstr(String text) {
            String[] lines = text.split("\n");
            int i = 0;
            String[] words;
            while (i < lines.length) {
                words = lines[i].split(" +");
                int j = 0;
                while (words[j].equals("") && j < words.length - 1) j++;
                if (j < words.length && words[j].equals("instr")) {
                    name = words[j + 1];
                    break;
                }
                i++;
            }

            i++;

            body = "";
            while (i < lines.length) {
                words = lines[i].split(" ");
                if (!words[0].equals("endin")) body += lines[i] + "\n";
                i++;
            }
        }
    }

    private void OnFileChosen(File file) {
        instr_file = file;
        String instr_text = activity.csoundUtil.getExternalFileAsString(file.getAbsolutePath());
        ParseInstr instr = new ParseInstr(instr_text);
        instrName = instr.name;
        Matrix.getInstance().spy();
        CSD.mapInstr.put(instrName, instr.body);
        Matrix.getInstance().update();
        listInstr.add(instrName);
        instr_adapter.notifyDataSetChanged();

        instrName = null;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        instrName = inputText;
        Matrix.getInstance().spy();
        // TODO : improve default instr
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

