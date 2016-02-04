package joel.duet.musica;

//import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 22:18 at 22:19.
 */
public final class Pattern {
    //private static final String TAG = "Pattern";
    private final LinkedList<Note> mNotes = new LinkedList<>();
    public int start;
    public int finish;
    private String instr;
    public void setInstr(String i){instr=i;}
    public String getInstr(){return instr;}

    public Pattern(){}

    public Pattern(List<Note> list, String str){
        mNotes.addAll(list);
        instr = str;
    }
    private static int mIdNoteSelected = 0;

    public float mScaleFactorX = 1f;
    public float mScaleFactorY = 1f;
    public float mPosX, mPosY;
    public int resolution;

    public static class Note {
        public final int onset;
        public final int duration;
        public final int pitch;
        public int loudness;
        Note(int o,int d, int p, int l){onset=o;duration=d;pitch=p;loudness=l;}
    }

    public void createNote(int o,int d, int p, int l){
        int i = 0;
        while(i < mNotes.size() && mNotes.get(i).onset < o) i++;
        mNotes.add(i,new Note(o,d,p,l));
    }

    public Note getNote(int n) {
        return mNotes.get(n);}

    public void deleteNote(Note note){
        mNotes.remove(note);
    }
    public void deleteNotes(List<Note> notes){
        for(Note note:notes) mNotes.remove(note);
    }

    public boolean isEmpty(){
        return mNotes.isEmpty();
    }

    public int getNbOfNotes(){return mNotes.size();}

    public List<String> getPitches(){
        List<String> list = new ArrayList<>();
        for(Note note:mNotes){
            //Log.i(TAG, "" + note.pitch);
            int key = note.pitch%12;
            int oct = 3 + (note.pitch-key)/12;
            list.add("" + oct + "." + (key<10?"0":"") + key);
        }
        return list;
    }

    public List<String> getWaits(){
        List<String> list = new ArrayList<>();
        for(Note note:mNotes)list.add("" + note.onset);
        return list;
    }

    public List<String> getDurationsInSeconds(){
        List<String> list = new ArrayList<>();
        for(Note note:mNotes){
            double dur = (double)note.duration/Default.ticks_per_second;
            list.add("" + dur);
        }
        return list;
    }

    public List<String> getPressureIndB(){
        List<String> list = new ArrayList<>();
        for(Note note:mNotes){
            double db = (double)(3*(note.loudness-1)-22);
            list.add("" + db);
        }
        return list;
    }

    public static void setNoteSelected(int id) {
        mIdNoteSelected = id;
    }

    public static Note getNoteSelected(){
        return Track.getPatternSelected().mNotes.get(mIdNoteSelected-1);}

    public List<Pattern> singleton(){
        final List<Pattern> list = new LinkedList<>();
        list.add(this);
        return list;
    }
}
