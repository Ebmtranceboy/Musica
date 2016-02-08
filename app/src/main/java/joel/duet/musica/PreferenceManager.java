package joel.duet.musica;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by joel on 04/02/16 at 23:19 at 23:21 at 20:23.
 */
public final class PreferenceManager {
    private static final String TAG = "PreferenceManager";

    private static PreferenceManager self;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isInitialised = false;
    private static final String ORCHESTRA_KEY = "Orchestra";
    private static final String FX_KEY = "FX";
    private static final String MATRIX_KEY = "Matrix";
    private static final String TRACKS_KEY = "Tracks";
    private static final String PROJECT_KEY = "Project";

    @SuppressLint("CommitPrefEdits")
    public void initialize(Context context) {
        if (!isInitialised) {
            preferences = context.getSharedPreferences(PROJECT_KEY, Context.MODE_PRIVATE);
            editor = preferences.edit();
            loadPreferences();
            isInitialised = true;
        }
    }

    public static PreferenceManager getInstance() {
        if (self == null) {
            self = new PreferenceManager();
        }
        return self;
    }

    private PreferenceManager() {
    }

    public static JSONObject project() {
        JSONObject json = new JSONObject();

        try {
            json.put(ORCHESTRA_KEY, saveJSONOrchestra());
            json.put(FX_KEY, saveJSONFX());
            json.put(MATRIX_KEY, joel.duet.musica.Matrix.serialize());
            json.put(TRACKS_KEY, saveJSONTracks());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void savePreferences() {
        JSONObject json = project();
        editor.putString(PROJECT_KEY, json.toString());
        editor.commit();
    }

    public static void resetProject() {
        CSD.mapInstr.clear();
        CSD.mapFX.clear();
        Score.resetTracks();
        joel.duet.musica.Matrix.getInstance().update();
    }

    private void loadPreferences() {

        resetProject();
        JSONObject project;

        String feed = preferences.getString(PROJECT_KEY, null);
        if (feed != null) {
            try {
                project = new JSONObject(feed);
                loadProject(project);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    project = new JSONObject(Default.empty_project);
                    loadProject(project);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

        } else {
            try {
                project = new JSONObject(Default.empty_project);
                loadProject(project);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void loadProject(JSONObject project) throws JSONException {
        Log.i(TAG, project.toString());
        loadJSONOrchestra(project.getJSONArray(ORCHESTRA_KEY));
        loadJSONFX(project.getJSONArray(FX_KEY));
        loadJSONTracks(project.getJSONObject(TRACKS_KEY));
        joel.duet.musica.Matrix.getInstance().update();
        if(project.getString(MATRIX_KEY).length() == (CSD.getNbInstruments()+CSD.getNbEffects()+1)*(CSD.getNbEffects()+2))
            joel.duet.musica.Matrix.getInstance().unserialize(project.getString(MATRIX_KEY));
    }

    private static JSONArray saveJSONOrchestra() throws JSONException {
        final JSONArray instruments = new JSONArray();
        JSONObject instr_obj;

        for (String instr : CSD.mapInstr.keySet()) {
            instr_obj = new JSONObject();
            instr_obj.put("name", instr);
            instr_obj.put("body", CSD.mapInstr.get(instr));
            instruments.put(instr_obj);
        }
        return instruments;
    }

    private static void loadJSONOrchestra(JSONArray instruments) throws JSONException {
        JSONObject instr_obj;
        for (int i = 0; i < instruments.length(); i++) {
            instr_obj = instruments.getJSONObject(i);
            CSD.mapInstr.put(instr_obj.getString("name"), instr_obj.getString("body"));
        }
    }

    private static JSONArray saveJSONFX() throws JSONException {
        final JSONArray effects = new JSONArray();
        JSONObject effect_obj;

        for (String effect : CSD.mapFX.keySet()) {
            effect_obj = new JSONObject();
            effect_obj.put("name", effect);
            effect_obj.put("body", CSD.mapFX.get(effect));
            effects.put(effect_obj);
        }
        return effects;
    }

    private static void loadJSONFX(JSONArray effects) throws JSONException {
        JSONObject effect_obj;
        for (int i = 0; i < effects.length(); i++) {
            effect_obj = effects.getJSONObject(i);
            CSD.mapFX.put(effect_obj.getString("name"), effect_obj.getString("body"));
        }
    }

    private static JSONObject saveJSONTracks() throws JSONException {
        final int idTrackSelected = Score.getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();

        int t, p, n;
        Track track;
        Pattern pattern;
        Pattern.Note note;

        JSONObject jsonObject = new JSONObject();
        final JSONArray tracks = new JSONArray();

        JSONArray patterns, notes;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;

        for (t = 1; t <= Score.getNbOfTracks(); t++) {
            Score.setTrackSelected(t);
            track = Score.getTrackSelected();
            track_obj = new JSONObject();
            patterns = new JSONArray();
            for (p = 1; p <= track.getNbOfPatterns(); p++) {
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = new JSONObject();
                pattern_obj.put("start", pattern.start);
                pattern_obj.put("finish", pattern.finish);
                pattern_obj.put("instr", pattern.getInstr());
                notes = new JSONArray();
                for (n = 1; n <= pattern.getNbOfNotes(); n++) {
                    Pattern.setNoteSelected(n);
                    note = Pattern.getNoteSelected();
                    note_obj = new JSONObject();
                    note_obj.put("onset", note.onset);
                    note_obj.put("duration", note.duration);
                    note_obj.put("pitch", note.pitch);
                    note_obj.put("loudness", note.loudness);
                    notes.put(note_obj);
                }
                pattern_obj.put("notes", notes);
                view_obj = new JSONObject();
                view_obj.put("posX", pattern.mPosX);
                view_obj.put("posY", pattern.mPosY);
                view_obj.put("scaleFactorX", pattern.mScaleFactorX);
                view_obj.put("scaleFactorY", pattern.mScaleFactorY);
                view_obj.put("resolution_index", pattern.resolution);
                pattern_obj.put("view", view_obj);
                patterns.put(pattern_obj);
            }
            track_obj.put("patterns", patterns);
            tracks.put(track_obj);
        }
        jsonObject.put("Score_posX", ScoreView.mPosX);
        jsonObject.put("Score_posY", ScoreView.mPosY);
        jsonObject.put("Score_scaleFactorX", ScoreView.mScaleFactorX);
        jsonObject.put("Score_scaleFactorY", ScoreView.mScaleFactorY);
        jsonObject.put("Score_resolution", Score.resolution_index);
        jsonObject.put("Score_bar_start", Score.bar_start);
        jsonObject.put("idTrackSelected", idTrackSelected);
        jsonObject.put("idPatternSelected", idPatternSelected);
        jsonObject.put("tracks", tracks);

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
        return jsonObject;
    }

    private static void loadJSONTracks(JSONObject feed) throws JSONException {
        final int idTrackSelected = feed.getInt("idTrackSelected");
        final int idPatternSelected = feed.getInt("idPatternSelected");
        final JSONArray tracks = feed.getJSONArray("tracks");
        JSONArray patterns, notes;
        int t, p, n;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;
        Track track;
        Pattern pattern;

        for (t = 1; t <= tracks.length(); t++) {
            Score.createTrack();
            Score.setTrackSelected(t);
            track = Score.getTrackSelected();
            track_obj = tracks.getJSONObject(t - 1);
            patterns = track_obj.getJSONArray("patterns");
            for (p = 1; p <= patterns.length(); p++) {
                track.createPattern();
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = patterns.getJSONObject(p - 1);
                pattern.start = pattern_obj.getInt("start");
                pattern.finish = pattern_obj.getInt("finish");
                pattern.setInstr(pattern_obj.getString("instr"));
                notes = pattern_obj.getJSONArray("notes");
                for (n = 1; n <= notes.length(); n++) {
                    note_obj = notes.getJSONObject(n - 1);
                    pattern.createNote(
                            note_obj.getInt("onset")
                            , note_obj.getInt("duration")
                            , note_obj.getInt("pitch")
                            , note_obj.getInt("loudness"));
                }
                view_obj = pattern_obj.getJSONObject("view");
                pattern.mPosX = (float) view_obj.getDouble("posX");
                pattern.mPosY = (float) view_obj.getDouble("posY");
                pattern.mScaleFactorX = (float) view_obj.getDouble("scaleFactorX");
                pattern.mScaleFactorY = (float) view_obj.getDouble("scaleFactorY");
                pattern.resolution = view_obj.getInt("resolution_index");
            }
        }

        ScoreView.mPosX = (float) feed.getDouble("Score_posX");
        ScoreView.mPosY = (float) feed.getDouble("Score_posY");
        ScoreView.mScaleFactorX = (float) feed.getDouble("Score_scaleFactorX");
        ScoreView.mScaleFactorY = (float) feed.getDouble("Score_scaleFactorY");
        Score.resolution_index = feed.getInt("Score_resolution");
        Score.bar_start = feed.getInt("Score_bar_start");

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
    }
}
