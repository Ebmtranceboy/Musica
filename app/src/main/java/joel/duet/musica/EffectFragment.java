package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 *
 * Created by joel on 21/01/16 at 14:07 at 14:44.
 */
public final class EffectFragment extends Fragment {
    private String effectName;
    private EditText effectCode;

    public String getEffectName(){
        return effectName;
    }

    public String getEffectCode(){
        return effectCode.getText().toString();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.effect_fragment, container, false);
        effectName = getArguments().getString("effectName");
        TextView effectTitleView = (TextView) view.findViewById(R.id.effect_title);
        String format = getResources().getString(R.string.effect_title);
        effectTitleView.setText(String.format(format, effectName));
        effectCode = (EditText)view.findViewById(R.id.effectCode);
        effectCode.setText(CSD.mapFX.get(effectName));

        final Button del_button = (Button) view.findViewById(R.id.delete_effect);
        del_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                MainActivity.sensible_code = new Runnable() {
                    @Override
                    public void run() {
                        Matrix.getInstance().spy();
                        CSD.mapFX.remove(effectName);
                        Matrix.getInstance().update();

                        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                            new FXFragment(),
                            "FX").commit();
                        MainActivity.toolbar.setTitle("FX");
                        MainActivity.currentFragment = MainActivity.State.FX;
                    }
                };

                final ConfirmationFragment confirmation = new ConfirmationFragment();
                confirmation.show(fragmentManager, "Delete instrument Fragment");
            }
        });

        return view;
    }
}
