package joel.duet.musica;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.csounds.CsoundObj;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 21:39 at 21:40 at 09:53 at 10:29 at 10:31 at 12:27 at 15:25 at 10:05.
 */

public final class ScoreFragment extends Fragment {
    private final LinkedList<Integer> bars = new LinkedList<>();
    private static Spinner edition_spinner;
    private static final String TAG = "ScoreFragment";

    private static ScoreView scoreview;
    private static ArrayAdapter<Integer> bars_adapter;
    public static MainActivity activity;
    private static CsoundObj csoundObj;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.score_fragment, container, false);


        view.findViewById(R.id.mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.edit_mode = !ScoreView.edit_mode;
                ScoreView.tool = ScoreView.Tool.NONE;
                edition_spinner.setSelection(0);
                if(ScoreView.edit_mode) edition_spinner.setVisibility(View.VISIBLE);
                else edition_spinner.setVisibility(View.INVISIBLE);
                scoreview.invalidate();
            }
        });

        edition_spinner = (Spinner) view.findViewById(R.id.edition);
        SimpleImageArrayAdapter edition_adapter = new SimpleImageArrayAdapter(getContext(), Default.edition_icons);
        edition_spinner.setAdapter(edition_adapter);

        edition_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        ScoreView.tool = ScoreView.Tool.NONE;
                        break;
                    case 1:
                        ScoreView.tool = ScoreView.Tool.COPY;
                        break;
                    case 2:
                        ScoreView.tool = ScoreView.Tool.MOVE;
                        break;
                    case 3:
                        ScoreView.tool = ScoreView.Tool.JOIN;
                        break;
                    case 4:
                        ScoreView.tool = ScoreView.Tool.SPLIT;
                        break;
                    case 5:
                        ScoreView.tool = ScoreView.Tool.QUANT;
                        break;
                }
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        view.findViewById(R.id.new_track).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int p_id = Track.getIdPatternSelected();
                final int t_id = Score.getIdTrackSelected();
                Score.createTrack();
                ScoreView.tracks_displayed =
                        Math.max(Score.getNbOfTracks(), Default.min_tracks_displayed);
                ScoreView.track_height =
                        (1.0f - (Default.top_margin + Default.bottom_margin))
                                / (float) ScoreView.tracks_displayed;
                Score.setTrackSelected(t_id);
                Track.setPatternSelected(p_id);
                scoreview.invalidate();
            }
        });

        view.findViewById(R.id.extend_score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.number_patches++;
                bars.clear();
                for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
                bars_adapter.notifyDataSetChanged();
                scoreview.invalidate();
            }
        });

        view.findViewById(R.id.trim_score).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<Track> uselessTracks = new LinkedList<>();
                int t, p;
                for (t = 1; t <= Score.getNbOfTracks(); t++) {
                    Score.setTrackSelected(t);
                    final Track track = Score.getTrackSelected();
                    final List<Pattern> uselessPatterns = new LinkedList<>();
                    for (p = 1; p <= track.getNbOfPatterns(); p++) {
                        Track.setPatternSelected(p);
                        final Pattern pattern = Track.getPatternSelected();
                        if (pattern.isEmpty()) uselessPatterns.add(pattern);
                    }
                    track.deletePatterns(uselessPatterns);
                    if (track.isEmpty()) uselessTracks.add(track);
                }
                Score.deleteTracks(uselessTracks);
                Score.setTrackSelected(Score.getNbOfTracks());
                Track.setPatternSelected(1);
                if (Score.getNbOfTracks() == 0) Track.setPatternSelected(0);

                ScoreView.number_patches = 1 + (int) (Score.getSeconds() / (Score.getResolution() / 2));
                bars.clear();
                for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
                bars_adapter.notifyDataSetChanged();

                ScoreView.Focus.reset();
                ScoreView.tracks_displayed =
                        Math.max(Score.getNbOfTracks(), Default.min_tracks_displayed);
                ScoreView.track_height =
                        (1.0f - (Default.top_margin + Default.bottom_margin))
                                / (float) ScoreView.tracks_displayed;
                scoreview.invalidate();
            }
        });

        final Spinner resolution_spinner = (Spinner) view.findViewById(R.id.resolution);
        SimpleImageArrayAdapter adapter = new SimpleImageArrayAdapter(getContext(), Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);
        resolution_spinner.setSelection(Score.resolution_index);

        final Spinner bars_spinner = (Spinner) view.findViewById(R.id.bars_spinner);

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(Score.resolution_index <i) Score.bar_start *= Score.getResolution() / Default.resolutions[i];
                else Score.bar_start /= Default.resolutions[i] / Score.getResolution();

                while(ScoreView.number_patches * 16 <  Score.bar_start) ScoreView.number_patches++;
                bars.clear();
                for (int p = 1; p <= ScoreView.number_patches * 16; p++) bars.add(p);
                bars_adapter.notifyDataSetChanged();

                bars_spinner.setSelection(Score.bar_start);
                Score.resolution_index = i;
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ScoreView.number_patches = 1 + (int) (Score.getSeconds() / (Score.getResolution() / 2));

        for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
        bars_adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                bars);
        bars_spinner.setAdapter(bars_adapter);
        bars_spinner.setSelection(Score.bar_start);

        bars_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Score.bar_start = i;
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        view.findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                String csd = Score.sendPatterns(Score.allPatterns(),
                                                                        bars_spinner.getSelectedItemPosition() * Score.getResolution());
                                                                Log.i(TAG, csd);
                                                                csoundObj.stop();
                                                                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                                                            }
                                                        }
        );

        view.findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                csoundObj.stop();
                                                            }
                                                        }
        );

        scoreview = (ScoreView) view.findViewById(R.id.score_view);

        ScoreView.edit_mode = false;
        edition_spinner.setVisibility(View.INVISIBLE);
        return view;
    }
}
