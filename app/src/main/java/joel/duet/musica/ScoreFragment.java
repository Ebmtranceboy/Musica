package joel.duet.musica;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.csounds.CsoundObj;

import java.util.LinkedList;
import java.util.List;

import joel.duet.musica.databinding.ScoreFragmentBinding;

/**
 *
 * Created by joel on 22/01/16 at 21:39 at 21:40 at 09:53 at 10:29 at 10:31 at 12:27 at 15:25 at 10:05.
 */

public final class ScoreFragment extends Fragment {
    private final LinkedList<Integer> bars = new LinkedList<>();
    private static Spinner edition_spinner;
    //private static final String TAG = "ScoreFragment";

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
        ScoreFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.score_fragment, container, false);
        final ToggleButton mode_button = binding.mode;

        binding.setUser(ScoreView.user);
        mode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.user.edit_mode.set(!ScoreView.user.edit_mode.get());
                ScoreView.tool = ScoreView.Tool.NONE;
                edition_spinner.setSelection(0);
            }
        });

        edition_spinner = binding.edition;
        SimpleImageArrayAdapter edition_adapter = new SimpleImageArrayAdapter(getContext(), Default.edition_icons);
        edition_spinner.setAdapter(edition_adapter);

        edition_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ScoreView.tool = ScoreView.tools[i];
                scoreview.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        binding.newTrack.setOnClickListener(new View.OnClickListener() {
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

        binding.extendScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScoreView.number_patches++;
                bars.clear();
                for (int i = 1; i <= ScoreView.number_patches * 16; i++) bars.add(i);
                bars_adapter.notifyDataSetChanged();
                scoreview.invalidate();
            }
        });

        binding.trimScore.setOnClickListener(new View.OnClickListener() {
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

        final Spinner resolution_spinner = binding.resolution;
        SimpleImageArrayAdapter adapter =
                new SimpleImageArrayAdapter(getContext(), Default.resolution_icons);
        resolution_spinner.setAdapter(adapter);
        resolution_spinner.setSelection(Score.resolution_index);

        final Spinner bars_spinner = binding.barsSpinner;

        resolution_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(Score.resolution_index <i)
                    Score.bar_start *= Score.getResolution() / Default.resolutions[i];
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
        if(Score.bar_start<ScoreView.number_patches*16) bars_spinner.setSelection(Score.bar_start);
        else bars_spinner.setSelection(1);

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

        binding.play.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String csd = Score.sendPatterns(Score.allPatterns(), false,
                                                        bars_spinner.getSelectedItemPosition() * Score.getResolution());
                                                //Log.i(TAG, csd);
                                                csoundObj.stop();
                                                csoundObj.startCsound(activity.csoundUtil.createTempFile(csd));
                                            }
                                        }
        );

        binding.stop.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                csoundObj.stop();
                                                            }
                                                        }
        );

        scoreview = binding.scoreView;

        ScoreView.user.edit_mode.set(false);
        return binding.getRoot();
    }
}
