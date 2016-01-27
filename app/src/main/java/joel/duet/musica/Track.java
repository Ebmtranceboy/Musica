package joel.duet.musica;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 22:17 at 22:21.
 */
public class Track {
    private static int mIdPatternSelected = 0;

    private final LinkedList<Pattern> mPatterns = new LinkedList<>();

    Track(){}

    public void createPattern(){
        mPatterns.addLast(new Pattern());
        mPatterns.getLast().setInstr("<undefined>");}

    public void deletePattern(Pattern p){
        mPatterns.remove(p);
    }
    public void deletePatterns(List<Pattern> list){
        for(int i=0;i<list.size();i++) mPatterns.remove(list.get(i));
    }

    public boolean isEmpty(){
        return mPatterns.isEmpty();
    }

    public int getNbOfPatterns(){return mPatterns.size();}

    public static int getIdPatternSelected(){return mIdPatternSelected;}
    public static void setPatternSelected(int id) {
        mIdPatternSelected = id;
    }

    public static Pattern getPatternSelected(){
        return Score.getTrackSelected().mPatterns.get(mIdPatternSelected-1);}

    public List<Pattern> allPatterns(){return mPatterns;}

}
