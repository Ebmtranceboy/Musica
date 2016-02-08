package joel.duet.musica;


//import android.util.Log;

import android.text.TextUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by joel on 13/01/16 at 08:23 at 08:23 at 12:53 at 09:55.
 */
final class CSD {
    //private static final String TAG = "CSD";

    private static final int sr = 44100;
    private static final int ksmps = 100;
    private static final int nchnls = 2;
    private static final double zeroDbFs = 1;

    private static final String header =
            "\n<CsoundSynthesizer>"
                    + "\n<CsOptions>"
                    + "\n-d -odac -m0"
                    + "\n</CsOptions>"
                    + "\n<CsInstruments>"
                    + "\nsr = " + sr
                    + "\nksmps = " + ksmps
                    + "\nnchnls = " + nchnls
                    + "\n0dbfs = " + zeroDbFs;

    static final Map<String, String> mapFX = new LinkedHashMap<>(); //new new HashMap<>();

    static int getNbEffects() {
        return mapFX.keySet().size();
    }


    static final Map<String, String> mapInstr = new LinkedHashMap<>();//new HashMap<>();

    static int getNbInstruments() {
        return mapInstr.keySet().size();
    }

    private static class Snippet {
        final String name;
        int narg;
        String accL, accR;
        final String last;

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

    private static void update(int i, int j) {
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

    private static Snippet[] snippets;

    private static String Master() {
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

    private static final String Voicer = "\n\ninstr Voicer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", inb + ifrac/20, 0, -1, p6, p7"
            + "\nendin";

    private static final String Silencer = "\n\ninstr Silencer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", -(inb + ifrac/20), 0, 0"
            + "\nendin";

    private static final String Metro = "\ngkmetro init 0"
            + "\n\ninstr Metro"
            + "\ngkmetro metro " + Default.ticks_per_second
            + "\nendin";

    private static String Looper(Pattern pat, int frac, int duration) {
        String instr = pat.getInstr();
        int n = pat.getNbOfNotes();
        return "\n\ninstr Looper_" + frac
            + "\nipch[] fillarray " + TextUtils.join(", ", pat.getPitches())
            + "\nistp[] fillarray " + TextUtils.join(", ", pat.getWaits())
            + "\nilen sumarray istp"
            + "\nidur[] fillarray " + TextUtils.join(", ", pat.getDurationsInSeconds())
            + "\nivel[] fillarray " + TextUtils.join(", ", pat.getPressureIndB())
            + "\nkstepnum init 0"
            + "\nkwait init istp[0]"
            + "\nif(gkmetro==1) then"
            + "\n   if(kwait==0) then"
            + "\n         kpch = ipch[kstepnum]"
            + "\n         kdur = idur[kstepnum]"
            + "\n         kvel = ivel[kstepnum]"
            + "\n         event \"i\", \"" + instr + "\" ,0,kdur,kpch,kvel"
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

    private static String InstrLoops(List<Pattern> score, int duration){
        String loops = "";
        int frac = 1;
        for(Pattern pattern:score){
            if(!pattern.isEmpty()) loops += Looper(pattern,frac,duration);
            frac ++;
        }
        return loops;
    }

    private static String ScoreLoops(List<Pattern> score){
        String loops = "";
        int frac = 1;
        for(Pattern pattern :score){
            if(!pattern.isEmpty()) loops += "\ni\"Looper_" + frac + "\" 0 -1";
            frac ++;
        }
        return loops;
    }

    private static final String ScoreMetro = "\ni\"Metro\" 0 -1";

    private static final String initScore =
            "\n\n</CsInstruments>"
                    + "\n<CsScore>"
                    + "\nf0 z"
                    + "\ni\"Master\" 0 -1";

    private static final String endScore = "\n</CsScore>"
                    + "\n</CsoundSynthesizer>";

    private static String resets;
    static String part() {
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
           instruments += "\n\ninstr " + instr + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, p5" : "") + "\n" + mapInstr.get(instr) + "\nendin";
        }
        return header + "\ngihand fiopen \"" + Default.score_events_absoluteFilePath + "\", 0" + inits + udos + instruments + Master() + Voicer + Silencer + initScore + endScore;
    }

     static String recordSong(String instrName,List<Pattern> score, int dur) {
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
            instruments += "\n\ninstr " + instr + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, p5" : "") +"\n" + mapInstr.get(instr) + "\nendin";
        }
        return header + "\ngihand fiopen \"" + Default.score_events_absoluteFilePath + "\", 0" + inits + udos + instruments + Master() + Voicer
                + Metro + InstrLoops(score,dur) + Silencer + initScore + ScoreMetro + ScoreLoops(score) + endScore;
    }
    public static int pressure2dB(float pressure){
        return Math.round(3*(pressure-32)/32-22);
    }
    public static int dB2Loudness(float dB){        return Math.round((dB+22)/3+1);}
    public static int defaultLoudness2dB() {        return (Default.default_loudness -1)*3-22; }
}
