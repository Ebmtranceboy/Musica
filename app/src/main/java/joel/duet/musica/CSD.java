package joel.duet.musica;


//import android.util.Log;

import android.text.TextUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by joel on 13/01/16 at 08:23 at 08:23 at 12:53 at 09:55.
 */
final class CSD {
    //private static final String TAG = "CSD";
    public static double tempo_ratio = 1.0; // relative to 60 quater by second
    public static double master_gain_L = 1.0, master_gain_R = 1.0;
    public static String projectName = Default.new_project_name;
    public static String globals = Default.material;

    private static final int sr = 44100;
    private static final int ksmps = 100;
    private static final int nchnls = 2;
    private static final double zeroDbFs = 1;

    public static String extractName(String filename){
        int index = filename.indexOf(".");
        String name = filename;
        if(index>=0)
            name = filename.substring(0,index);
        return name;
    }

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

    static class Content{
        final String code;
        double gainL, gainR;
        Content(String c, double gl, double gr) {code = c; gainL = gl; gainR = gr;}
    }

    static class Corpus{
        private final Map<String, Content> map = new LinkedHashMap<>(); //new HashMap<>();
        private boolean dirty = true;
        private String[] array;

        public Set<String> getSet(){return map.keySet();}
        public String[] getArray() {
            if(dirty) {
                array = map.keySet().toArray(new String[map.size()]);
                dirty = false;
            }
            return array;
        }
        public void put(String name, Content content){
            map.put(name,content);
            dirty = true;
        }
        public Content get(String name){
            return map.get(name);
        }
        public void clear(){
            map.clear();
            dirty = true;
        }
        public void remove(String name){
            map.remove(name);
            dirty = true;
        }
        public int size(){
            return map.size();
        }
    }

    public static final Corpus effects = new Corpus();
    public static final Corpus instruments = new Corpus();

    /*
    static final Map<String, Content> mapInstr = new LinkedHashMap<>();//new HashMap<>();

    static int getNbInstruments() {
        return mapInstr.keySet().size();
    }
*/
    private static class Snippet {
        final String name;
        int narg;
        String accL, accR;
        final String last;

        private Snippet(int i, int j, boolean flattenControls) {
            if (j <= effects.size()) {
                name = effects.getArray()[j - 1];
                last = "a_" + name + "_L, a_" + name + "_R " + name
                        + " ain_" + name + "_L, ain_" + name + "_R";
            } else {
                name = "Master";
                last = "\nk_Master_L init " + master_gain_L
                    + "\nk_Master_R init " + master_gain_R
                    + (!flattenControls?"\nk_Master_L chnget \"ktrl_Master_L\""
                    +                  "\nk_Master_R chnget \"ktrl_Master_R\"":"")
                    + "\nouts k_Master_L * ain_Master_L, k_Master_R * ain_Master_R";

            }
            narg = Matrix.getNbActiveInput(j) - 1;
            accL = "ain_" + name + "_L sum ";
            accR = "ain_" + name + "_R sum ";
            String in;
            if (i < instruments.size()) {
                in = instruments.getArray()[i];
                accL += "k_" + in + "_L * ga_" + in + "_L";
                accR += "k_" + in + "_R * ga_" + in + "_R";
            } else {
                in = effects.getArray()[i - instruments.size()];
                accL += "k_" + in + "_L * a_" + in + "_L";
                accR += "k_" + in + "_R * a_" + in + "_R";
            }
        }
    }

    private static void update(int i, int j, boolean flattenControls) {
        if (snippets[j] != null) {
            String in;
            if (i < instruments.size()) {
                in = instruments.getArray()[i];
                snippets[j].accL += ", k_" + in + "_L * ga_" + in + "_L";
                snippets[j].accR += ", k_" + in + "_R * ga_" + in + "_R";
            } else {
                in = effects.getArray()[i - instruments.size()];
                snippets[j].accL += ", k_" + in + "_L * a_" + in + "_L";
                snippets[j].accR += ", k_" + in + "_R * a_" + in + "_R";
            }

            snippets[j].narg --;

        } else snippets[j] = new Snippet(i, j, flattenControls);
    }

    private static Snippet[] snippets;

    private static String Master(boolean flattenControls) {
        String master = "";
        String ktrl_L = "", ktrl_R = "";
        String in;
        snippets = new Snippet[effects.size() + 2];
        for(int i=0; i<instruments.size()+effects.size();i++) {
            if(i<instruments.size()) {
                in = instruments.getArray()[i];
                ktrl_L += "\nk_" + in + "_L init " + instruments.get(in).gainL;
                ktrl_R += "\nk_" + in + "_R init " + instruments.get(in).gainR;
            }
            else{
                in =effects.getArray()[i - instruments.size()];
                ktrl_L += "\nk_" + in + "_L init " + effects.get(in).gainL;
                ktrl_R += "\nk_" + in + "_R init " + effects.get(in).gainR;
            }

            if(!flattenControls) {
                ktrl_L += "\nk_" + in + "_L chnget \"ktrl_" + in + "_L\"";
                ktrl_R += "\nk_" + in + "_R chnget \"ktrl_" + in + "_R\"";
            }

            for (int j = 1; j <= effects.size() + 1; j++) {
                if (Matrix.get(i, j)) {
                    update(i, j, flattenControls);
                    if (snippets[j].narg == 0) {
                        master += "\n" + snippets[j].accL
                                + "\n" + snippets[j].accR
                                + "\n" + snippets[j].last;
                        snippets[j] = null;
                    }
                }
            }
        }
        return "\n\ninstr Master" + ktrl_L + ktrl_R + master + resets + "\nendin";
    }

    private static final String Voicer = "\n\ninstr Voicer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", inb + ifrac/200, 0, -1, p6, p7, p8"
            + "\nendin";

    private static final String Silencer = "\n\ninstr Silencer"
            + "\nSinstr = p4"
            + "\ninb nstrnum Sinstr"
            + "\nifrac = p5"
            + "\nevent_i \"i\", -(inb + ifrac/200), 0, 0"
            + "\nendin";

    private static String Metro() { return "\ngkmetro init 0"
            + "\n\ninstr Metro"
            + "\ngkmetro metro " + (Default.ticks_per_second * tempo_ratio)
            + "\nendin";}

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
            + "\nkpch init 0"
            + "\nkwait init istp[0]"
            + "\nif(gkmetro==1) then"
            + "\n   if(kwait==0) then"
            + "\n         kpreviouspch = kpch"
            + "\n         kpch = ipch[kstepnum]"
            + "\n         kdur = idur[kstepnum]"
            + "\n         kvel = ivel[kstepnum]"
            + "\n         event \"i\", \"" + instr + "\" ,0,kdur,kpch,kvel,kpreviouspch"
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
        for(String udo : effects.getSet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + effects.get(udo).code + "\nendop";
        }

        resets = "";
        String orchestra = "";
        for (String instr : instruments.getSet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            orchestra += "\n\ninstr " + instr + "\n" + instruments.get(instr).code + "\nendin";
        }
        //Log.i(TAG, "in :" + instruments);
        return header + globals + inits + udos + orchestra + Master(false) + Voicer + Silencer
                + initScore + endScore;
    }

    static String song(List<Pattern> score, int dur,boolean flattenControls) {
        String udos = "";
        String inits = "";
        for(String udo : effects.getSet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + effects.get(udo).code + "\nendop";
        }

        resets = "";
        String orchestra = "";
        for (String instr : instruments.getSet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            orchestra += "\n\ninstr " + instr + "\n" + instruments.get(instr).code + "\nendin";
        }
        return header + globals + inits + udos + orchestra + Master(flattenControls) + Voicer
                + Metro() + InstrLoops(score,dur) + Silencer
                + initScore + ScoreMetro + ScoreLoops(score) + endScore;
    }

    static String recordPart(String instrName) {
        String udos="";
        String inits = "";
        for(String udo : effects.getSet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + effects.get(udo).code + "\nendop";
        }

        resets = "";
        String orchestra = "";
        for (String instr : instruments.getSet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            orchestra += "\n\ninstr " + instr
                   + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, p5, p6" : "")
                   + "\n" + instruments.get(instr).code + "\nendin";
        }
        return header + globals + "\ngihand fiopen \"" + Default.score_events_absoluteFilePath + "\", 0"
                + inits + udos + orchestra + Master(false) + Voicer + Silencer + initScore + endScore;
    }

     static String recordSong(String instrName,List<Pattern> score, int dur) {
        String udos = "";
        String inits = "";
        for(String udo : effects.getSet()){
            udos += "\n\nopcode " + udo + ", aa, aa" + "\n" + effects.get(udo).code + "\nendop";
        }

        resets = "";
        String orchestra = "";
        for (String instr : instruments.getSet()) {
            inits += "\nga_" + instr + "_L init 0";
            inits += "\nga_" + instr + "_R init 0";
            resets += "\nga_" + instr + "_L = 0";
            resets += "\nga_" + instr + "_R = 0";
            orchestra += "\n\ninstr " + instr
                    + (instr.contentEquals(instrName) ? "\nfoutir gihand,0, 1, p4, p5, p6" : "")
                    +"\n" + instruments.get(instr).code + "\nendin";
        }
        return header + globals + "\ngihand fiopen \"" + Default.score_events_absoluteFilePath + "\", 0"
                + inits + udos + orchestra + Master(false) + Voicer
                + Metro() + InstrLoops(score,dur) + Silencer
                + initScore + ScoreMetro + ScoreLoops(score) + endScore;
    }
    public static int pressure2dB(float pressure){
        return Math.round(3*(pressure-32)/32-22);
    }
    public static int dB2Loudness(float dB){        return Math.round((dB+22)/3+1);}
    public static int defaultLoudness2dB() {        return (Default.default_loudness -1)*3-22; }
}
