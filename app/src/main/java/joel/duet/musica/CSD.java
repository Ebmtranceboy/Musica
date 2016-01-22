package joel.duet.musica;


//import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by joel on 13/01/16 at 08:23 at 08:23 at 12:53 at 09:55.
 */
public final class CSD {
    //private static final String TAG = "CSD";

    static int sr = 44100;
    static int ksmps = 100;
    static int nchnls = 2;
    static double zeroDbFs = 1;

    static String header =
            "\n<CsoundSynthesizer>"
                    + "\n<CsOptions>"
                    + "\n-d -odac -m0"
                    + "\n</CsOptions>"
                    + "\n<CsInstruments>"
                    + "\nsr = " + sr
                    + "\nksmps = " + ksmps
                    + "\nnchnls = " + nchnls
                    + "\n0dbfs = " + zeroDbFs;

    static Map<String, String> mapFX = new HashMap<>();

    static {
        mapFX.put("Delay",
                "ainL, ainR xin\n" +
                "adelL delay ainL, 0.2\n" +
                "adelR delay ainR, 0.2\n" +
                "xout adelL,adelR\n");
    }

    static int getNbEffects() {
        return mapFX.keySet().size();
    }


    static Map<String, String> mapInstr = new HashMap<>();

    static int getNbInstruments() {
        return mapInstr.keySet().size();
    }

    static String instrExample =
            "irelease = p3"
                    + "\nipch = cps2pch(p4,12)"
                    + "\niamp = ampdbfs(p5)"
                    + "\naenv linsegr 0, 0.01, 1, 0.01, .9, irelease, 0"
                    + "\naout vco2 iamp, ipch"
                    + "\naout =  aout * aenv"
                    + "\nahp,alp,abp,abr statevar aout, 3000, 4"
                    + "\namin = 0.0"
                    + "\nafiltmix = linsegr:a(0, p3 * .5, 0.75, p3 * .5, 0.0, irelease, 0.0)"
                    + "\nalpmix = min(amin, afiltmix - 0.5)"
                    + "\nabpmix = 0.5 - abs(afiltmix - 0.5)"
                    + "\nahpmix = min(amin, 0.5 - afiltmix)"
                    + "\nkVolume = 1"
                    + "\naout = kVolume * sum(alpmix * alp, abpmix * abp, ahpmix * ahp)"
                    + "\nga_Example_L += aout"
                    + "\nga_Example_R += aout";

    static {
        mapInstr.put("Example", instrExample);
        }

    private static class Snippet {
        String name;
        int narg;
        String accL, accR;
        String last;

        private Snippet(int i, int j) {
            if (j <= getNbEffects()) {
                name = mapFX.keySet().toArray(new String[getNbInstruments()])[j - 1];
                last = "a_" + name + "_L, a_" + name + "_R " + name + " ain_" + name + "_L, ain_" + name + "_R";
            } else {
                name = "Master";
                last = "outs ain_Master_L, ain_Master_R";

            }
            narg = Matrix.getNbActiveInput(j) - 1;
            accL = "ain_" + name + "_L sum ";
            accR = "ain_" + name + "_R sum ";
            String in;
            if (i < getNbInstruments()) {
                in = mapInstr.keySet().toArray(new String[getNbInstruments()])[i];
                accL += "ga_" + in + "_L";
                accR += "ga_" + in + "_R";
            } else {
                in = mapFX.keySet().toArray(new String[getNbEffects()])[i - getNbInstruments()];
                accL += "a_" + in + "_L";
                accR += "a_" + in + "_R";
            }
        }
    }

    public static void update(int i, int j) {
        if (snippets[j] != null) {
            String in;
            if (i < getNbInstruments()) {
                in = mapInstr.keySet().toArray(new String[getNbInstruments()])[i];
                snippets[j].accL += ", ga_" + in + "_L";
                snippets[j].accR += ", ga_" + in + "_R";
            } else {
                in = mapFX.keySet().toArray(new String[getNbEffects()])[i - getNbInstruments()];
                snippets[j].accL += ", a_" + in + "_L";
                snippets[j].accR += ", a_" + in + "_R";
            }
            snippets[j].narg --;

        } else snippets[j] = new Snippet(i, j);
    }

    static Snippet snippets[];

    static String Master() {
        String master = "";
        snippets = new Snippet[getNbEffects() + 2];
        for(int i=0; i<getNbInstruments()+getNbEffects();i++)
            for(int j=1; j<=getNbEffects()+1;j++){
                if(Matrix.get(i,j)){
                    update(i,j);
                    if(snippets[j].narg == 0) {
                        master += "\n" + snippets[j].accL
                                + "\n" + snippets[j].accR
                                + "\n" + snippets[j].last;
                        snippets[j] = null;
                    }
                }
            }
        return "\ninstr Master" + master + inits + "\nendin";
    }

    static String Voicer = "\ninstr Voicer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", inb + ifrac/20, 0, -1, p6, p7"
            + "\nendin";

    static String Silencer = "\ninstr Silencer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", -(inb + ifrac/20), 0, 0"
            + "\nendin";

    static String score =
            "\n</CsInstruments>"
                    + "\n<CsScore>"
                    + "\nf0 z"
                    + "\ni\"Master\" 0 -1"
                    + "\n</CsScore>"
                    + "\n</CsoundSynthesizer>";

    static String inits;
    static String csd() {
        String udos = "";
        for(String udo : mapFX.keySet()){
            udos += "\nopcode " + udo + ", aa, aa" + "\n" + mapFX.get(udo) + "\nendop";
        }

        inits = "";
        String instruments = "";
        for (String instr : mapInstr.keySet()) {
            inits += "\nga_" + instr + "_L = 0";
            inits += "\nga_" + instr + "_R = 0";
            instruments += "\ninstr " + instr + "\n" + mapInstr.get(instr) + "\nendin";
        }
        //Log.i(TAG, "in :" + instruments);
        return header + inits + udos + instruments + Master() + Voicer + Silencer + score;
    }

    static String recordPart(String instrName) {
        String udos="";
        for(String udo : mapFX.keySet()){
            udos += "\nopcode " + udo + ", aa, aa" + "\n" + mapFX.get(udo) + "\nendop";
        }

        String inits = "";
        String instruments = "";
        for (String instr : mapInstr.keySet()) {
            inits += "\nga_" + instr + "_L = 0";
            inits += "\nga_" + instr + "_R = 0";
            instruments += "\ninstr " + instr + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, -12" : "") + "\n" + mapInstr.get(instr) + "\nendin";
        }
        return header + "\ngihand fiopen \"storage/sdcard0/unisonMelody.txt\", 0" + inits + udos + instruments + Master() + Voicer + Silencer + score;
    }
}
