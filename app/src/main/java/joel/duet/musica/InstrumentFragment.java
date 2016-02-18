package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

/**
 *
 * Created by joel on 17/01/16 at 23:10 at 12:46 at 14:57.
 */
public final class InstrumentFragment extends Fragment {
    private String instrName;
    private EditText instrumentCode;
    private File instr_file;
    static private MainActivity activity;

    public String getInstrName() {
        return instrName;
    }

    public String getInstrCode() {
        return instrumentCode.getText().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.instrument_fragment, container, false);
        instrName = getArguments().getString("instrName");
        TextView instrTitleView = (TextView) view.findViewById(R.id.instr_title);
        String format = getResources().getString(R.string.instr_title);
        instrTitleView.setText(String.format(format, instrName));
        instrumentCode = (EditText) view.findViewById(R.id.instrumentCode);
        instrumentCode.setText(CSD.mapInstr.get(instrName).code);

        final Button del_button = (Button) view.findViewById(R.id.delete_instr);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.mapInstr.remove(instrName);
                        Matrix.getInstance().update();

                        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                new OrchestraFragment(),
                                "ORCHESTRA").commit();
                        MainActivity.toolbar.setTitle("Orchestra");
                        MainActivity.currentFragment = MainActivity.State.ORCHESTRA;
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(fragmentManager, "Delete instrument Fragment");
            }
        });

        final Button export_button = (Button) view.findViewById(R.id.add_to_instr_lib);
        export_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(getContext(), R.style.csoundAlertDialogStyle),
                        "FileSave..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                int index = chosenDir.indexOf("//");
                                if (index >= 0) {
                                    chosenDir = chosenDir.substring(index + 1);
                                }
                                instr_file = new File(chosenDir);
                                activity.csoundUtil.saveStringAsExternalFile("instr " + instrName + "\n" + getInstrCode() + "endin", instr_file.getAbsolutePath());
                            }
                        }
                );
                if (instr_file != null) {
                    fileOpenDialog.default_file_name = instr_file.getParent();
                } else {
                    fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });

        return view;
    }
}
