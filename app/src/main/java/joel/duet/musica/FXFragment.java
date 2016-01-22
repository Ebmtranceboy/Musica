package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
 * Created by joel on 21/01/16 at 08:16 at 14:17.
 */
public final class FXFragment extends FragmentPlus{
    //TODO reset matrix after adding and removing effect
    //TODO implement import button
    //TODO implement add to lib
    static private MainActivity activity;
    //private static final String TAG = "FX";
    static private ArrayAdapter<String> effect_adapter;
    static String effectName;
    static private List<String> listEffect;

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




        return view;
    }

    @Override
    public void onFinishEditDialog(String inputText) {
        effectName = inputText;
        CSD.mapFX.put(effectName, "");
        Matrix.reset();
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
