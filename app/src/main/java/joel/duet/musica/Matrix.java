package joel.duet.musica;

//import android.util.Log;

/**
 * Created by joel on 20/01/15.13:29
 */
public final class Matrix {
    private static int ninstr;
    private static int nfx;
    private static boolean[] mLinks;
    public static String[] cells;
    //private static final String TAG = "Matrix";

    // TODO : less aggressive reset
    public static void reset() {
        ninstr = CSD.getNbInstruments();
        nfx = CSD.getNbEffects();
        mLinks = new boolean[(ninstr + nfx + 1) * (nfx + 2)];
        cells = new String[(ninstr + nfx + 1) * (nfx + 2)];

        for (int i = 0; i < ninstr + nfx + 1; i++)
            for (int j = 0; j < nfx + 2; j++) unset(i,j);
    }

    public static boolean get(int i, int j){return mLinks[i*(nfx+2)+j];}

    public static void set(int i, int j) {
        mLinks[i * (nfx + 2) + j] = true;
        show(i, j);
    }

    public static void unset(int i, int j) {
        mLinks[i * (nfx + 2) + j] = false;
        show(i, j);
    }

    private static void show(int i, int j) {
        if (i == ninstr + nfx && j == 0) {
            cells[i * (nfx + 2) + j] = "";
            return;
        }

        if (j == 0) {
            if (i < ninstr)
                cells[i * (nfx + 2)] = CSD.mapInstr.keySet().toArray(new String[ninstr])[i];
            else cells[i * (nfx + 2)] = CSD.mapFX.keySet().toArray(new String[nfx])[i-ninstr];
        } else if (i == ninstr + nfx) {
            if (j < nfx + 1)
                cells[i * (nfx + 2) + j] = CSD.mapFX.keySet().toArray(new String[nfx])[j-1];
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

    public static void unserialize(String str){
        for(int i=0;i<ninstr+nfx+1;i++)
            for(int j=0;j<nfx+2;j++)
                if(str.charAt(i * (nfx + 2) + j) == 'T') set(i,j); else unset(i,j);
    }
}
