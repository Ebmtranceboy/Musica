package joel.duet.musica;

//import android.util.Log;
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

    //public static boolean is_score_loop = false;

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

    public static List<Pattern> fix(List<Pattern> list){
        final List<Pattern> patterns = new ArrayList<>();
        Pattern fixed;
        int lastOnset;


        for(Pattern pattern:list){
            int n=0;
            fixed = new Pattern();

            while(n<pattern.getNbOfNotes()){
                Pattern.Note note = pattern.getNote(n);
                fixed.createNote(note.onset, note.duration, note.pitch, note.loudness);
                lastOnset = note.onset;
                n++;
                while(n<pattern.getNbOfNotes() && pattern.getNote(n).onset > lastOnset){
                    note = pattern.getNote(n);
                    fixed.createNote(note.onset,note.duration,note.pitch,note.loudness);
                    lastOnset = note.onset;
                    n++;
                }
                fixed.setInstr(pattern.getInstr());
                fixed.start = pattern.start;
                fixed.finish = pattern.finish;
                patterns.add(fixed);
                fixed = new Pattern();
            }
        }
        return patterns;
    }

    public static String sendPatterns(List<Pattern> unFixedPatterns, int... params){
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
        List<Pattern> patterns = fix(unFixedPatterns);

        for(Pattern pattern : patterns) {
            delay_start = pattern.start-tick_start;
            if(pattern.start < start) start = pattern.start;
            if(pattern.finish > finish) finish = pattern.finish;

            list.clear();
            int lastOnset = 0;
            boolean firstOnset = false;
            Pattern.Note note = null;
            for(int n = 1 ;n<=pattern.getNbOfNotes();n++) {
                if(firstOnset) lastOnset = delay_start + note.onset;
                note = pattern.getNote(n-1);
                if (note.onset + pattern.start >= tick_start
                        && note.onset + pattern.start <= tick_finish) {
                    if(!firstOnset){
                        lastOnset = 0;
                        firstOnset = true;
                    }
                    list.add(new Pattern.Note(
                            delay_start + note.onset - lastOnset,
                            note.duration,
                            note.pitch,
                            note.loudness));
                }
            }
            //for(Pattern.Note not:list)  Log.i(TAG,""+not.onset+" "+not.pitch);
            score.add(new Pattern(list,pattern.getInstr()));
        }
        return CSD.song(score,Math.min(finish, tick_finish) - tick_start);
    }
}
