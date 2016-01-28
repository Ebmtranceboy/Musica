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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.csounds.CsoundObj;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 21:39 at 21:40 at 09:53 at 10:29 at 10:31 at 12:27 at 15:25 at 10:05.
 */

public final class MasterFragment extends Fragment {
    private final LinkedList<Integer> bars = new LinkedList<>();
    private static ImageButton three;
    private static final String TAG = "Master";

    private static ScoreView scoreview;
    private static ArrayAdapter<Integer> bars_adapter;
    public static MainActivity activity;
    private static CsoundObj csoundObj;

    private void ensureThreeStateButtonCoherence() {

        if (ScoreView.edit_mode) {
            three.setVisibility(View.VISIBLE);
            switch (ScoreView.tool) {
                case NONE:
                    three.setImageResource(R.drawable.ic_crane);
                    break;
                case COPY:
                    three.setImageResource(R.drawable.ic_copy);
                    break;
                case MOVE:
                    three.setImageResource(R.drawable.ic_move);
                    break;
            }
        } else three.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
        csoundObj = MainActivity.csoundObj;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancesState) {
        final View view = inflater.inflate(R.layout.master_fragment, container, false);

        three = (ImageButton) view.findViewById(R.id.copy);

        view.findViewById(R.id.mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.edit_mode = !ScoreView.edit_mode;
                ScoreView.tool = ScoreView.Tool.NONE;
                ensureThreeStateButtonCoherence();
                scoreview.invalidate();
            }
        });

        view.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (ScoreView.tool) {
                    case NONE:
                        ScoreView.tool = ScoreView.Tool.COPY;
                        break;
                    case COPY:
                        ScoreView.tool = ScoreView.Tool.MOVE;
                        break;
                    case MOVE:
                        ScoreView.tool = ScoreView.Tool.NONE;
                        break;
                }
                ensureThreeStateButtonCoherence();
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
        resolution_spinner.setSelection(Score.resolution);

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Score.resolution = i;
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
        bars_adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1,
                bars);
        final Spinner bars_spinner = (Spinner) view.findViewById(R.id.bars_spinner);
        bars_spinner.setAdapter(bars_adapter);
        bars_spinner.setSelection(Score.bar_start);

        bars_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Score.bar_start = i;
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

        scoreview = new ScoreView(getContext());

        FrameLayout scoreArea =
                (FrameLayout) view.findViewById(R.id.score_view);
        scoreArea.addView(scoreview);

        // onResume:
        ScoreView.number_patches = 1 + (int) (Score.getSeconds() / (Score.getResolution() / 2));

        ScoreView.edit_mode = ((Switch) view.findViewById(R.id.mode)).isChecked();

        ScoreView.tool = ScoreView.Tool.NONE;

        ensureThreeStateButtonCoherence();

        return view;
    }

}
