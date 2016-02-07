package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 *
 * Created by joel on 19/01/16 at 11:38 at 12:30 at 13:57 at 08:00.
 */
public final class PatchBayFragment extends Fragment {
    private static GridView grid;
    //private static final String TAG = "Patchbay";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.patchbay_fragment, container, false);
        grid = (GridView) view.findViewById(R.id.grid_view);

        grid.setNumColumns(CSD.getNbEffects() + 2);
        grid.setAdapter(new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, Matrix.cells) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);

                int n = CSD.getNbEffects() + 2;
                int j = position % n;
                int i = (position - j) / n;

                int color = 0x00FFFFFF; // Transparent
                if (j == 0) {
                    if (i < CSD.getNbInstruments()) color = 0x7F0000FF; // Opaque Blue
                    else if (i < CSD.getNbInstruments() + CSD.getNbEffects()) color = 0x7F00FF00;
                } else if (i == CSD.getNbInstruments() + CSD.getNbEffects() && j > 0) {
                    if (j <= CSD.getNbEffects()) color = 0x7F00FF00;
                    else color = 0x7FFF0000;
                }

                view.setBackgroundColor(color);
                ((TextView) view).setGravity(Gravity.CENTER | Gravity.BOTTOM);

                return view;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int n = CSD.getNbEffects() + 2;
                int j = position % n;
                int i = (position - j) / n;
                if (Matrix.get(i, j)) Matrix.getInstance().unset(i, j);
                else Matrix.getInstance().set(i, j);
                //Log.i(TAG,Matrix.serialize());
                grid.invalidateViews();
            }
        });

        return view;
    }
}
