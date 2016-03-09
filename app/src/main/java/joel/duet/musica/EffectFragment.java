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

import joel.duet.musica.databinding.EffectFragmentBinding;

/**
 *
 * Created by joel on 21/01/16 at 14:07 at 14:44 at 09:26 at 09:29 at 10:41.
 */
public final class EffectFragment extends Fragment {
    private File effect_file;
    static private MainActivity activity;
    private EffectFragmentBinding binding;
    String effectName;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        effectName = getArguments().getString("effectName");

        binding = DataBindingUtil.inflate(inflater,
                R.layout.effect_fragment,
                container,
                false);

        binding.effectName.setText(String.format(getResources().getString(R.string.effect_title),
                effectName));
        binding.effectCode.setText(CSD.effects.get(effectName).code);

        binding.deleteEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager =
                        getActivity().getSupportFragmentManager();

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.effects.remove(effectName);
                        Matrix.getInstance().update();

                        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                new FXFragment(),
                                "Fx").commit();
                        MainActivity.toolbar.setTitle("FX");
                        MainActivity.currentFragment = MainActivity.State.Fx;
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(fragmentManager, "Delete instrument Fragment");
            }
        });

        binding.addToFxLib.setOnClickListener(new View.OnClickListener() {
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
                                effect_file = new File(chosenDir);
                                activity.csoundUtil.saveStringAsExternalFile(
                                        "opcode " + effectName + ", aa, aa\n"
                                                + CSD.effects.get(effectName).code
                                                + "endop", effect_file.getAbsolutePath());
                            }
                        }
                );
                if (effect_file != null) {
                    fileOpenDialog.default_file_name = effect_file.getParent();
                } else {
                    fileOpenDialog.default_file_name =
                            Environment.getExternalStorageDirectory().getAbsolutePath();
                }
                fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
            }
        });

        return binding.getRoot();
    }

    private void updateModel(){
        CSD.Content content = CSD.effects.get(effectName);
        if(content != null && binding != null)
            CSD.effects.put(effectName,
                    new CSD.Content(binding.effectCode.getText().toString(),
                            content.gainL,
                            content.gainR));
    }

    @Override
    public void onPause() {
        updateModel();
        super.onPause();
    }
}
