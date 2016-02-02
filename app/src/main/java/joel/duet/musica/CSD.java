package joel.duet.musica;


//import android.util.Log;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.List;
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

    static int getNbEffects() {
        return mapFX.keySet().size();
    }


    static Map<String, String> mapInstr = new HashMap<>();

    static int getNbInstruments() {
        return mapInstr.keySet().size();
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
        return "\n\ninstr Master" + master + resets + "\nendin";
    }

    static String Voicer = "\n\ninstr Voicer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", inb + ifrac/20, 0, -1, p6, p7"
            + "\nendin";

    static String Silencer = "\n\ninstr Silencer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", -(inb + ifrac/20), 0, 0"
            + "\nendin";

    static String Metro = "\ngkmetro init 0"
            + "\n\ninstr Metro"
            + "\ngkmetro metro " + Default.ticks_per_second
            + "\nendin";

    static String Looper(Pattern pat, int frac,int duration) {
        String instr = pat.getInstr();
        int n = pat.getNbOfNotes();
        return "\n\ninstr Looper_" + frac
            + "\nipch[] fillarray " + TextUtils.join(", ", pat.getPitches())
            + "\nistp[] fillarray " + TextUtils.join(", ", pat.getWaits())
            + "\nilen sumarray istp"
            + "\nidur[] fillarray " + TextUtils.join(", ", pat.getDurationsInSeconds())
            + "\nkstepnum init 0"
            + "\nkwait init istp[0]"
            + "\nif(gkmetro==1) then"
            + "\n   if(kwait==0) then"
            + "\n         kpch = ipch[kstepnum]"
            + "\n         kdur = idur[kstepnum]"
            + "\n         event \"i\", \"" + instr + "\" ,0,kdur,kpch,-12"
            + "\n         kstepnum += 1"
            + "\n         if(kstepnum>=" + n + ") then"
            + "\n            kstepnum = 0"
            + "\n            kwait = " + duration + " - ilen - 1 + istp[0]"
            + "\n         else"
            + "\n            kwait = istp[kstepnum]"
            + "\n            kwait -= 1"
            + "\n         endif"
            + "\n   else"
            + "\n      kwait -= 1"
            + "\n   endif"
            + "\nendif"
            + "\nendin";}

    static String InstrLoops(List<Pattern> score, int duration){
        String loops = "";
        int frac = 1;
        for(Pattern pattern:score){
            if(!pattern.isEmpty()) loops += Looper(pattern,frac,duration);
            frac ++;
        }
        return loops;
    }

    static String ScoreLoops(List<Pattern>score){
        String loops = "";
        int frac = 1;
        for(Pattern pattern :score){
            if(!pattern.isEmpty()) loops += "\ni\"Looper_" + frac + "\" 0 -1";
            frac ++;
        }
        return loops;
    }

    static String ScoreMetro = "\ni\"Metro\" 0 -1";

    static String initScore =
            "\n\n</CsInstruments>"
                    + "\n<CsScore>"
                    + "\nf0 z"
                    + "\ni\"Master\" 0 -1";

    static String endScore = "\n</CsScore>"
                    + "\n</CsoundSynthesizer>";

    static String resets;
    static String csd() {
        String inits = "";
        String udos = "";
        for(String udo : mapFX.keySet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + mapFX.get(udo) + "\nendop";
        }

        resets = "";
        String instruments = "";
        for (String instr : mapInstr.keySet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            instruments += "\n\ninstr " + instr + "\n" + mapInstr.get(instr) + "\nendin";
        }
        //Log.i(TAG, "in :" + instruments);
        return header + inits + udos + instruments + Master() + Voicer + Silencer + initScore + endScore;
    }

    static String song(List<Pattern> score, int dur) {
        String udos = "";
        String inits = "";
        for(String udo : mapFX.keySet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + mapFX.get(udo) + "\nendop";
        }

        resets = "";
        String instruments = "";
        for (String instr : mapInstr.keySet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            instruments += "\n\ninstr " + instr + "\n" + mapInstr.get(instr) + "\nendin";
        }
        return header + inits + udos + instruments + Master() + Voicer
                + Metro + InstrLoops(score,dur) + Silencer + initScore + ScoreMetro + ScoreLoops(score) + endScore;
    }

    static String recordPart(String instrName) {
        String udos="";
        String inits = "";
        for(String udo : mapFX.keySet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + mapFX.get(udo) + "\nendop";
        }

        resets = "";
        String instruments = "";
        for (String instr : mapInstr.keySet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
           instruments += "\n\ninstr " + instr + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, -12" : "") + "\n" + mapInstr.get(instr) + "\nendin";
        }
        return header + "\ngihand fiopen \"storage/sdcard0/unisonMelody.txt\", 0" + inits + udos + instruments + Master() + Voicer + Silencer + initScore + endScore;
    }
}
