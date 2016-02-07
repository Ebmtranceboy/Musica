package joel.duet.musica;

//import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by joel on 20/01/15.13:29
 */
public final class Matrix {
    private static int ninstr;
    private static int nfx;
    private static boolean[] mLinks;
    public static String[] cells;
    private static Map<String,List<String>> edges = new HashMap<>();
    //private static final String TAG = "Matrix";
    private boolean isInitialised = false;
    private static Matrix self;

    public void initialize() {
        if (!isInitialised) {
            isInitialised = true;
        }
    }

    public static Matrix getInstance() {
        if (self == null) {
            self = new Matrix();
        }
        return self;
    }

     private Matrix() {
    }

    public void spy(){
        String instruments[] = CSD.mapInstr.keySet().toArray(new String[ninstr]);
        String effects[] =  CSD.mapFX.keySet().toArray(new String[nfx]);
        for(int i=0; i< ninstr+nfx; i++)
            for(int j=1; j<=nfx+1; j++)
                if(get(i,j))
                    if(i<ninstr){
                        if(!edges.containsKey(instruments[i]))
                            edges.put(instruments[i],new ArrayList<String>());

                        if(j<=nfx) edges.get(instruments[i]).add(effects[j-1]);
                        else edges.get(instruments[i]).add("Master");
                    } else{
                        if(!edges.containsKey(effects[i-ninstr]))
                            edges.put(effects[i-ninstr],new ArrayList<String>());

                        if(j<=nfx) edges.get(effects[i-ninstr]).add(effects[j-1]);
                        else edges.get(effects[i-ninstr]).add("Master");
                    }

       /* for(String source:edges.keySet())
            for(String sink:edges.get(source))
                Log.i(TAG,""+source+"->"+sink);*/
    }

    public void update(){
        reset();
        String instruments[] = CSD.mapInstr.keySet().toArray(new String[ninstr]);
        String effects[] =  CSD.mapFX.keySet().toArray(new String[nfx]);
        for(int i=0; i< ninstr; i++) {
            for (int j = 1; j <= nfx; j++)
                if (edges.containsKey(instruments[i]) && edges.get(instruments[i]).contains(effects[j-1]))
                    set(i, j);
            if(edges.containsKey(instruments[i]) && edges.get(instruments[i]).contains("Master")) set(i,nfx+1);
        }

        for(int i=ninstr; i< ninstr+nfx; i++) {
            for (int j = 1; j <= nfx; j++)
                if (edges.containsKey(effects[i-ninstr]) && edges.get(effects[i-ninstr]).contains(effects[j-1]))
                    set(i, j);
            if(edges.containsKey(effects[i-ninstr]) && edges.get(effects[i-ninstr]).contains("Master")) set(i,nfx+1);
        }
    }

    public void reset() {
        ninstr = CSD.getNbInstruments();
        nfx = CSD.getNbEffects();
        mLinks = new boolean[(ninstr + nfx + 1) * (nfx + 2)];
        cells = new String[(ninstr + nfx + 1) * (nfx + 2)];

        for (int i = 0; i < ninstr + nfx + 1; i++)
            for (int j = 0; j < nfx + 2; j++) unset(i,j);
    }

    public static boolean get(int i, int j){return mLinks[i*(nfx+2)+j];}

    public void set(int i, int j) {
        mLinks[i * (nfx + 2) + j] = true;
        show(i, j);
    }

    public void unset(int i, int j) {
        mLinks[i * (nfx + 2) + j] = false;
        show(i, j);
    }

    private void show(int i, int j) {
        String instruments[] = CSD.mapInstr.keySet().toArray(new String[ninstr]);
        String effects[] =  CSD.mapFX.keySet().toArray(new String[nfx]);
        if (i == ninstr + nfx && j == 0) {
            cells[i * (nfx + 2) + j] = "";
            return;
        }

        if (j == 0) {
            if (i < ninstr)
                cells[i * (nfx + 2)] = instruments[i];
            else cells[i * (nfx + 2)] = effects[i-ninstr];
        } else if (i == ninstr + nfx) {
            if (j < nfx + 1)
                cells[i * (nfx + 2) + j] = effects[j-1];
            else cells[i * (nfx + 2) + j] = "Master";
        } else if (get(i,j))
            cells[i * (nfx + 2) + j] = "\u21B4";
        else cells[i * (nfx + 2) + j] = ".";
    }

    public static int getNbActiveInput(int j){
        int val = 0;
        for(int i=0;i<ninstr+nfx;i++)
            if(get(i,j)) val++;
        return val;
    }

    public static String serialize(){
        String str = "";
        for(int i=0;i<ninstr+nfx+1;i++)
            for(int j=0;j<nfx+2;j++)
                if(get(i,j)) str += "T"; else str += "F";
        return str;
    }

    public void unserialize(String str){
        for(int i=0;i<ninstr+nfx+1;i++)
            for(int j=0;j<nfx+2;j++)
                if(str.charAt(i * (nfx + 2) + j) == 'T') set(i,j); else unset(i,j);
    }
}
