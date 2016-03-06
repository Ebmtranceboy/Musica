package joel.duet.musica;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import joel.duet.musica.databinding.InstrumentFragmentBinding;

/**
 *
 * Created by joel on 17/01/16 at 23:10 at 12:46 at 14:57 at 18:52.
 */
public final class InstrumentFragment extends Fragment {
    private File instr_file;
    static private MainActivity activity;
    private InstrumentFragmentBinding binding;
    String instrumentName;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        instrumentName = getArguments().getString("instrName");

        binding = DataBindingUtil.inflate(inflater,
                R.layout.instrument_fragment,
                container,
                false);

        binding.instrumentName.setText(String.format(getResources().getString(R.string.instr_title)
            , instrumentName));
        binding.instrumentCode.setText(CSD.mapInstr.get(instrumentName).code);

        binding.deleteInstr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager =
                        getActivity().getSupportFragmentManager();

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.mapInstr.remove(instrumentName);
                        Matrix.getInstance().update();

                        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                new OrchestraFragment(),
                                "Orchestra").commit();
                        MainActivity.toolbar.setTitle("Orchestra");
                        MainActivity.currentFragment = MainActivity.State.Orchestra;
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(fragmentManager, "Delete instrument Fragment");
            }
        });

        binding.addToInstrLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateModel();
                SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                        new ContextThemeWrapper(getContext(),
                                R.style.csoundAlertDialogStyle),
                        "FileSave..",
                        new SimpleFileDialog.SimpleFileDialogListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                int index = chosenDir.indexOf("//");
                                if (index >= 0) {
                                    chosenDir = chosenDir.substring(index + 1);
                                }
                                instr_file = new File(chosenDir);
                                activity.csoundUtil.saveStringAsExternalFile(
                                        "instr " + instrumentName + "\n"
                                                + CSD.mapInstr.get(instrumentName).code
                                                + "endin",
                                        instr_file.getAbsolutePath());
                            }
                        }
                );
                if (instr_file != null) {
                    fileOpenDialog.default_file_name = instr_file.getParent();
                } else {
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });

        return binding.getRoot();
    }

    private void updateModel() {
        CSD.Content content = CSD.mapInstr.get(instrumentName);
        if (content != null && binding != null)
            CSD.mapInstr.put(instrumentName,
                    new CSD.Content(binding.instrumentCode.getText().toString(),
                            content.gainL,
                            content.gainR));
    }

    @Override
    public void onPause() {
        updateModel();
        super.onPause();
    }
}
