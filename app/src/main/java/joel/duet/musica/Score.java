package joel.duet.musica;

//import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 22:16 at 22:22.
 */
public final class Score {
    //private static final String TAG = "Score";
    private static final LinkedList<Track> mTracks = new LinkedList<>();
    private static int mIdTrackSelected = 0;
    public static int resolution;
    public static int bar_start;

    public static boolean is_score_loop = false;

    public static int getResolution() {
        return Default.resolutions[Score.resolution];
    }

    public static void createTrack(){
        mTracks.addLast(new Track());}
    public static int getNbOfTracks(){return mTracks.size();}

    public static void deleteTracks(List<Track> list){
        for(int i=0;i<list.size();i++) mTracks.remove(list.get(i));
    }

    public static int getIdTrackSelected(){return mIdTrackSelected;}
    public static void setTrackSelected(int id) {
        mIdTrackSelected = id;
    }

    public static Track getTrackSelected(){
        return mTracks.get(mIdTrackSelected-1);}

    private static int getDuration(){
        int t,p,n;
        int dur,mx=0;
        final int t_id = getIdTrackSelected();
        final int p_id = Track.getIdPatternSelected();
        for(t=1;t<=getNbOfTracks();t++){
            setTrackSelected(t);
            for(p=1;p<=getTrackSelected().getNbOfPatterns();p++){
                Track.setPatternSelected(p);
                Pattern pattern = Track.getPatternSelected();
                for(n=1;n<=pattern.getNbOfNotes();n++){
                    Pattern.setNoteSelected(n);
                    Pattern.Note note = Pattern.getNoteSelected();
                    dur = pattern.start+note.onset+note.duration;
                    if(mx<dur) mx=dur;
                }
            }
        }
        setTrackSelected(t_id);
        Track.setPatternSelected(p_id);
        return mx;
    }

    public static float getSeconds(){
        return getDuration()/(float)Default.ticks_per_second;
    }

    public static JSONObject saveJSONTracks() throws JSONException {
        final int idTrackSelected = getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();

        int t,p,n;
        Track track;
        Pattern pattern;
        Pattern.Note note;

        JSONObject jsonObject = new JSONObject();
        final JSONArray tracks = new JSONArray();

        JSONArray patterns, notes;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;

        for(t=1; t<=Score.getNbOfTracks(); t++){
            Score.setTrackSelected(t);
            track = Score.getTrackSelected();
            track_obj = new JSONObject();
            patterns = new JSONArray();
            for(p=1; p<=track.getNbOfPatterns(); p++){
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = new JSONObject();
                pattern_obj.put("start", pattern.start);
                pattern_obj.put("finish",pattern.finish);
                pattern_obj.put("instr",pattern.getInstr());
                notes = new JSONArray();
                for(n=1 ; n<=pattern.getNbOfNotes(); n++){
                    Pattern.setNoteSelected(n);
                    note = Pattern.getNoteSelected();
                    note_obj = new JSONObject();
                    note_obj.put("onset",note.onset);
                    note_obj.put("duration",note.duration);
                    note_obj.put("pitch",note.pitch);
                    notes.put(note_obj);
                }
                pattern_obj.put("notes",notes);
                view_obj = new JSONObject();
                view_obj.put("posX", pattern.mPosX);
                view_obj.put("posY", pattern.mPosY);
                view_obj.put("scaleFactorX", pattern.mScaleFactorX);
                view_obj.put("scaleFactorY", pattern.mScaleFactorY);
                view_obj.put("resolution", pattern.resolution);
                pattern_obj.put("view",view_obj);
                patterns.put(pattern_obj);
            }
            track_obj.put("patterns",patterns);
            tracks.put(track_obj);
        }
        jsonObject.put("Score_posX", ScoreView.mPosX);
        jsonObject.put("Score_posY",ScoreView.mPosY);
        jsonObject.put("Score_scaleFactorX",ScoreView.mScaleFactorX);
        jsonObject.put("Score_scaleFactorY",ScoreView.mScaleFactorY);
        jsonObject.put("Score_resolution",Score.resolution);
        jsonObject.put("Score_bar_start",Score.bar_start);
        jsonObject.put("idTrackSelected",idTrackSelected);
        jsonObject.put("idPatternSelected",idPatternSelected);
        jsonObject.put("tracks", tracks);

        setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
        return jsonObject;
    }

    public static void loadJSONTracks(JSONObject feed) throws JSONException{
        final int idTrackSelected=feed.getInt("idTrackSelected");
        final int idPatternSelected=feed.getInt("idPatternSelected");
        final JSONArray tracks = feed.getJSONArray("tracks");
        JSONArray patterns, notes;
        int t,p,n;
        JSONObject track_obj, pattern_obj, note_obj, view_obj;
        Track track;
        Pattern pattern;

        for(t=1; t<=tracks.length();t++){
            createTrack();
            setTrackSelected(t);
            track = getTrackSelected();
            track_obj = tracks.getJSONObject(t-1);
            patterns = track_obj.getJSONArray("patterns");
            for(p=1; p<=patterns.length();p++){
                track.createPattern();
                Track.setPatternSelected(p);
                pattern = Track.getPatternSelected();
                pattern_obj = patterns.getJSONObject(p-1);
                pattern.start = pattern_obj.getInt("start");
                pattern.finish = pattern_obj.getInt("finish");
                pattern.setInstr(pattern_obj.getString("instr"));
                notes = pattern_obj.getJSONArray("notes");
                for(n=1;n<=notes.length();n++){
                    note_obj = notes.getJSONObject(n-1);
                    pattern.createNote(
                            note_obj.getInt("onset")
                            ,note_obj.getInt("duration")
                            ,note_obj.getInt("pitch"));
                }
                view_obj = pattern_obj.getJSONObject("view");
                pattern.mPosX = (float)view_obj.getDouble("posX");
                pattern.mPosY = (float)view_obj.getDouble("posY");
                pattern.mScaleFactorX = (float)view_obj.getDouble("scaleFactorX");
                pattern.mScaleFactorY = (float)view_obj.getDouble("scaleFactorY");
                pattern.resolution = view_obj.getInt("resolution");
            }
        }

        ScoreView.mPosX = (float)feed.getDouble("Score_posX");
        ScoreView.mPosY = (float)feed.getDouble("Score_posY");
        ScoreView.mScaleFactorX = (float)feed.getDouble("Score_scaleFactorX");
        ScoreView.mScaleFactorY = (float)feed.getDouble("Score_scaleFactorY");
        Score.resolution = feed.getInt("Score_resolution");
        Score.bar_start = feed.getInt("Score_bar_start");

        setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
    }

    public static void resetTracks(){
        mTracks.clear();
        Score.setTrackSelected(0);
        Track.setPatternSelected(0);
        ScoreView.Focus.reset();
    }

    public static List<Pattern> allPatterns(){
        final List<Pattern> patterns = new LinkedList<>();
        for(Track track : mTracks) patterns.addAll(track.allPatterns());
        return patterns;
    }

    public static String sendPatterns(List<Pattern> patterns, int... params){
        int tick_start, tick_finish;
        switch(params.length) {
            case 2:
                tick_start = params[0];
                tick_finish = params[1];
                break;
            case 1:
                tick_start = params[0];
                tick_finish = Integer.MAX_VALUE;
                break;
            default:
                tick_start = 0;
                tick_finish = Integer.MAX_VALUE;
                break;
        }
        int delay_start;
        int finish=0,start=Integer.MAX_VALUE;

        final List<Pattern> score = new LinkedList<>();
        List<Pattern.Note> list = new ArrayList<>();

        for(Pattern pattern : patterns) {
            delay_start = pattern.start-tick_start;
            if(pattern.start < start) start=pattern.start;
            if(pattern.finish > finish) finish=pattern.finish;

            list.clear();
            int lastOnset = 0;
            Pattern.Note note = null;
            for(int n = 1 ;n<=pattern.getNbOfNotes();n++) {
                if(n>1) lastOnset = delay_start + note.onset;
                note = pattern.getNote(n-1);
                if (note.onset + pattern.start >= tick_start
                        && note.onset + pattern.start <= tick_finish) {
                    list.add(new Pattern.Note(
                            delay_start + note.onset - lastOnset,
                            note.duration,
                            note.pitch));
                }
            }
            score.add(new Pattern(list,pattern.getInstr()));
        }
        //Log.i(TAG, CSD.song(score));
        return CSD.song(score,Math.min(finish - start, tick_finish - tick_start));
    }
}
