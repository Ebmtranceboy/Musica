package joel.duet.musica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Created by joel on 22/01/16 at 22:22 at 22:23 at 08:53.
 */
final class Default {

    public static final int ticks_per_second = 32;

    public static final int min_tracks_displayed = 5;
    public static final float top_margin = 0.05f;
    public static final float bottom_margin = 0.05f;

    public static final int max_midi_note = 136;
    public static final float initial_pattern_height = -1500; //somewhat around A440

    public static final int default_loudness = 5; // from 1(ppp) to 8(fff)

    // public static final int transparent_color = 0x00FFFFFF; // transparent
    public static final int instrument_color = 0x7F0000FF; // Semi-Opaque Blue
    public static final int effect_color = 0x7F00FF00;
    public static final int master_color = 0x7FFF0000;

    public static final String new_project_name = "New project";
    public static final String score_events_absoluteFilePath = "/storage/sdcard0/unisonMelody.txt";
    public static final String material =
            "\nopcode voxinterpol,k,iikkkkkkk"
                    + "\nifn, itess, kidx, ka, kb, kc, kl0, kl1, kl2 xin"
                    + "\nsetksmps 1"
                    + "\nkoffseta = 5 *(6* itess + ka) + kidx"
                    + "\nkoffsetb = 5 *(6* itess + kb) + kidx"
                    + "\nkoffsetc = 5 *(6* itess + kc) + kidx"
                    + "\nkvala table koffseta, ifn"
                    + "\nkvalb table koffsetb, ifn"
                    + "\nkvalc table koffsetc, ifn"
                    + "\nxout kl0*kvala+kl1*kvalb+kl2*kvalc"
                    + "\nendop"
                    + "\ngi_vox_formants ftgen 0,0.0 , 256 , -2 , 400.0 , 1620.0 , 2400.0 , 2800.0 , 3100.0 , 600.0 , 1040.0 , 2250.0 , 2450.0 , 2750.0 , 400.0 , 1620.0 , 2400.0 , 2800.0 , 3100.0 , 400.0 , 750.0 , 2400.0 , 2600.0 , 2900.0 , 250.0 , 1750.0 , 2600.0 , 3050.0 , 3340.0 , 350.0 , 600.0 , 2400.0 , 2675.0 , 2950.0 , 400.0 , 1700.0 , 2600.0 , 3200.0 , 3580.0 , 650.0 , 1080.0 , 2650.0 , 2900.0 , 3250.0 , 400.0 , 1700.0 , 2600.0 , 3200.0 , 3580.0 , 400.0 , 800.0 , 2600.0 , 2800.0 , 3000.0 , 290.0 , 1870.0 , 2800.0 , 3250.0 , 3540.0 , 350.0 , 600.0 , 2700.0 , 2900.0 , 3300.0 , 440.0 , 1800.0 , 2700.0 , 3000.0 , 3300.0 , 660.0 , 1120.0 , 2750.0 , 3000.0 , 3350.0 , 440.0 , 1800.0 , 2700.0 , 3000.0 , 3300.0 , 430.0 , 820.0 , 2700.0 , 3000.0 , 3300.0 , 270.0 , 1850.0 , 2900.0 , 3350.0 , 3590.0 , 370.0 , 630.0 , 2750.0 , 3000.0 , 3400.0 , 400.0 , 1600.0 , 2700.0 , 3300.0 , 4950.0 , 800.0 , 1150.0 , 2800.0 , 3500.0 , 4950.0 , 400.0 , 1600.0 , 2700.0 , 3300.0 , 4950.0 , 450.0 , 800.0 , 2830.0 , 3500.0 , 4950.0 , 350.0 , 1700.0 , 2700.0 , 3700.0 , 4950.0 , 325.0 , 700.0 , 2530.0 , 3500.0 , 4950.0 , 350.0 , 2000.0 , 2800.0 , 3600.0 , 4950.0 , 800.0 , 1150.0 , 2900.0 , 3900.0 , 4950.0 , 350.0 , 2000.0 , 2800.0 , 3600.0 , 4950.0 , 450.0 , 800.0 , 2830.0 , 3800.0 , 4950.0 , 270.0 , 2140.0 , 2950.0 , 3900.0 , 4950.0 , 325.0 , 700.0 , 2700.0 , 3800.0 , 4950.0"
                    + "\ngi_vox_amps ftgen 0,0.0 , 256 , -2 , 0.0 , -12.0 , -9.0 , -12.0 , -18.0 , 0.0 , -7.0 , -9.0 , -9.0 , -20.0 , 0.0 , -12.0 , -9.0 , -12.0 , -18.0 , 0.0 , -11.0 , -21.0 , -20.0 , -40.0 , 0.0 , -30.0 , -16.0 , -22.0 , -28.0 , 0.0 , -20.0 , -32.0 , -28.0 , -36.0 , 0.0 , -14.0 , -12.0 , -14.0 , -20.0 , 0.0 , -6.0 , -7.0 , -8.0 , -22.0 , 0.0 , -14.0 , -12.0 , -14.0 , -20.0 , 0.0 , -10.0 , -12.0 , -12.0 , -26.0 , 0.0 , -15.0 , -18.0 , -20.0 , -30.0 , 0.0 , -20.0 , -17.0 , -14.0 , -26.0 , 0.0 , -14.0 , -18.0 , -20.0 , -20.0 , 0.0 , -6.0 , -23.0 , -24.0 , -38.0 , 0.0 , -14.0 , -18.0 , -20.0 , -20.0 , 0.0 , -10.0 , -26.0 , -22.0 , -34.0 , 0.0 , -24.0 , -24.0 , -36.0 , -36.0 , 0.0 , -20.0 , -23.0 , -30.0 , -34.0 , 0.0 , -24.0 , -30.0 , -35.0 , -60.0 , 0.0 , -4.0 , -20.0 , -36.0 , -60.0 , 0.0 , -24.0 , -30.0 , -35.0 , -60.0 , 0.0 , -9.0 , -16.0 , -28.0 , -55.0 , 0.0 , -20.0 , -30.0 , -36.0 , -60.0 , 0.0 , -12.0 , -30.0 , -40.0 , -64.0 , 0.0 , -20.0 , -15.0 , -40.0 , -56.0 , 0.0 , -6.0 , -32.0 , -20.0 , -50.0 , 0.0 , -20.0 , -15.0 , -40.0 , -56.0 , 0.0 , -11.0 , -22.0 , -22.0 , -50.0 , 0.0 , -12.0 , -26.0 , -26.0 , -44.0 , 0.0 , -16.0 , -35.0 , -40.0 , -60.0"
                    + "\ngi_vox_bws ftgen 0,0.0 , 256 , -2 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 60.0 , 70.0 , 110.0 , 120.0 , 130.0 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 60.0 , 90.0 , 100.0 , 120.0 , 120.0 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 70.0 , 80.0 , 100.0 , 120.0 , 120.0 , 80.0 , 90.0 , 120.0 , 130.0 , 140.0 , 70.0 , 80.0 , 100.0 , 120.0 , 120.0 , 70.0 , 80.0 , 100.0 , 130.0 , 135.0 , 40.0 , 90.0 , 100.0 , 120.0 , 120.0 , 40.0 , 60.0 , 100.0 , 120.0 , 120.0 , 70.0 , 80.0 , 100.0 , 120.0 , 120.0 , 80.0 , 90.0 , 120.0 , 130.0 , 140.0 , 70.0 , 80.0 , 100.0 , 120.0 , 120.0 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 40.0 , 90.0 , 100.0 , 120.0 , 120.0 , 40.0 , 60.0 , 100.0 , 120.0 , 120.0 , 60.0 , 80.0 , 120.0 , 150.0 , 200.0 , 80.0 , 90.0 , 120.0 , 130.0 , 140.0 , 60.0 , 80.0 , 120.0 , 150.0 , 200.0 , 70.0 , 80.0 , 100.0 , 130.0 , 135.0 , 50.0 , 100.0 , 120.0 , 150.0 , 200.0 , 50.0 , 60.0 , 170.0 , 180.0 , 200.0 , 60.0 , 100.0 , 120.0 , 150.0 , 200.0 , 80.0 , 90.0 , 120.0 , 130.0 , 140.0 , 60.0 , 100.0 , 120.0 , 150.0 , 200.0 , 40.0 , 80.0 , 100.0 , 120.0 , 120.0 , 60.0 , 90.0 , 100.0 , 120.0 , 120.0 , 50.0 , 60.0 , 170.0 , 180.0 , 200.0"
                    + "\ngi_vox_idxs ftgen 0,0 , 64 , -2 , 9 , 6 , 4 , 4 , 6 , 5 , 4 , 5 , 2 , 9 , 4 , 2 , 9 , 2 , 8 , 2 , 5 , 3 , 2 , 1 , 8 , 2 , 3 , 1 , 6 , 7 , 5 , 5 , 7 , 3 , 3 , 7 , 1 , 1 , 7 , 8";
    public static final String empty_project = "{\"Globals\":\"" + material + "\",\"GainL\":1.0,\"GainR\":1.0,\"Tempo\":1.0,\"Orchestra\":[],\"FX\":[],\"Tracks\":{\"tracks\":[],\"Score_posX\":0,\"Score_posY\":0,\"Score_scaleFactorX\":1,\"Score_scaleFactorY\":1,\"Score_resolution\":2,\"Score_bar_start\":1,\"idTrackSelected\":0,\"idPatternSelected\":0},\"Matrix\":\"FF\"}";

    public static final int[] resolutions = {128, 64, 32, 16, 8, 4, 2, 1};
    public static final int[] grays = {-16777216, -16711423, -16645630, -16579837, -16514044, -16448251, -16382458, -16316665, -16250872, -16185079, -16119286, -16053493, -15987700, -15921907, -15856114, -15790321, -15724528, -15658735, -15592942, -15527149, -15461356, -15395563, -15329770, -15263977, -15198184, -15132391, -15066598, -15000805, -14935012, -14869219, -14803426, -14737633, -14671840, -14606047, -14540254, -14474461, -14408668, -14342875, -14277082, -14211289, -14145496, -14079703, -14013910, -13948117, -13882324, -13816531, -13750738, -13684945, -13619152, -13553359, -13487566, -13421773, -13355980, -13290187, -13224394, -13158601, -13092808, -13027015, -12961222, -12895429, -12829636, -12763843, -12698050, -12632257, -12566464, -12500671, -12434878, -12369085, -12303292, -12237499, -12171706, -12105913, -12040120, -11974327, -11908534, -11842741, -11776948, -11711155, -11645362, -11579569, -11513776, -11447983, -11382190, -11316397, -11250604, -11184811, -11119018, -11053225, -10987432, -10921639, -10855846, -10790053, -10724260, -10658467, -10592674, -10526881, -10461088, -10395295, -10329502, -10263709, -10197916, -10132123, -10066330, -10000537, -9934744, -9868951, -9803158, -9737365, -9671572, -9605779, -9539986, -9474193, -9408400, -9342607, -9276814, -9211021, -9145228, -9079435, -9013642, -8947849, -8882056, -8816263, -8750470, -8684677, -8618884, -8553091, -8487298, -8421505, -8355712, -8289919, -8224126, -8158333, -8092540, -8026747, -7960954, -7895161, -7829368, -7763575, -7697782, -7631989, -7566196, -7500403, -7434610, -7368817, -7303024, -7237231, -7171438, -7105645, -7039852, -6974059, -6908266, -6842473, -6776680, -6710887, -6645094, -6579301, -6513508, -6447715, -6381922, -6316129, -6250336, -6184543, -6118750, -6052957, -5987164, -5921371, -5855578, -5789785, -5723992, -5658199, -5592406, -5526613, -5460820, -5395027, -5329234, -5263441, -5197648, -5131855, -5066062, -5000269, -4934476, -4868683, -4802890, -4737097, -4671304, -4605511, -4539718, -4473925, -4408132, -4342339, -4276546, -4210753, -4144960, -4079167, -4013374, -3947581, -3881788, -3815995, -3750202, -3684409, -3618616, -3552823, -3487030, -3421237, -3355444, -3289651, -3223858, -3158065, -3092272, -3026479, -2960686, -2894893, -2829100, -2763307, -2697514, -2631721, -2565928, -2500135, -2434342, -2368549, -2302756, -2236963, -2171170, -2105377, -2039584, -1973791, -1907998, -1842205, -1776412, -1710619, -1644826, -1579033, -1513240, -1447447, -1381654, -1315861, -1250068, -1184275, -1118482, -1052689, -986896, -921103, -855310, -789517, -723724, -657931, -592138, -526345, -460552, -394759, -328966, -263173, -197380, -131587, -65794, -1};

    public static final Integer[] resolution_icons = new Integer[]{R.drawable.ic_1st_note, R.drawable.ic_2nd_note, R.drawable.ic_4th_note, R.drawable.ic_8th_note, R.drawable.ic_16th_note, R.drawable.ic_32th_note, R.drawable.ic_64th_note, R.drawable.ic_128th_note};
    public static final Integer[] edition_icons = new Integer[]{R.drawable.ic_compose, R.drawable.ic_copy, R.drawable.ic_move, R.drawable.ic_join, R.drawable.ic_split, R.drawable.ic_quantize};
    public static final Integer[] loudness_icons = new Integer[]{R.drawable.ic_fortississimo, R.drawable.ic_fortissimo, R.drawable.ic_forte, R.drawable.ic_mezzo_forte, R.drawable.ic_mezzo_piano, R.drawable.ic_piano, R.drawable.ic_pianissimo, R.drawable.ic_pianississimo};

    public static final class Flavor {
        static int nbMajor = 0;
        final boolean isMajor;
        final List<Integer> intervals = new ArrayList<>();
        final String notation;

        Flavor(boolean maj, String code, Integer... ns) {
            isMajor = maj;
            if(isMajor) nbMajor++;
            notation = code;
            Collections.addAll(intervals, ns);
        }
    }

    public static final Flavor[] flavors = {
            // MAJOR
            // diatonic triads
            new Flavor(true, "I", 0, 4, 7),
            new Flavor(true, "ii", 2, 5, 9),
            new Flavor(true, "iii", 4, 7, 11),
            new Flavor(true, "IV", 5, 9, 12),
            new Flavor(true, "V", 7, 11, 14),
            new Flavor(true, "vi", 9, 12, 16),
            new Flavor(true, "vii°", 11, 14, 17),
            // other chords
            new Flavor(true, "Gr6", 8, 12, 15, 18),
            new Flavor(true, "It6", 8, 12, 18),
            new Flavor(true, "N6", 1, 5, 8),
            new Flavor(true, "Fr6", 8, 12, 14, 18),
            new Flavor(true, "Sw6", 8, 12, 15, 18),
            // borrowred chords
            new Flavor(true, "bII", 2, 5, 8),
            new Flavor(true, "iih7", 2, 5, 8, 12),
            new Flavor(true, "sii°7", 3, 6, 9, 12),
            new Flavor(true, "III", 4, 8, 11),
            new Flavor(true, "bIII", 3, 7, 10),
            new Flavor(true, "biii", 3, 6, 10),
            new Flavor(true, "iv", 5, 8, 12),
            new Flavor(true, "ivb7", 5, 8, 12, 15),
            new Flavor(true, "IVb7", 5, 9, 12, 15),
            new Flavor(true, "Vb9", 7, 11, 14, 17, 20),
            new Flavor(true, "VI", 9, 13, 16),
            new Flavor(true, "bVI", 8, 12, 15),
            new Flavor(true, "bvi", 8, 11, 15),
            new Flavor(true, "svi°7", 10, 13, 16, 19),
            new Flavor(true, "vii°7", 11, 14, 17, 20),
            new Flavor(true, "bVII", 10, 14, 17),
            // secondary dominants
            new Flavor(true, "V7/ii", 9, 13, 16, 19),
            new Flavor(true, "V7/iii", 11, 15, 18, 21),
            new Flavor(true, "V7/IV", 0, 4, 7, 10),
            new Flavor(true, "V7/V", 2, 6, 9, 12),
            new Flavor(true, "V7/vi", 4, 8, 11, 14),
            // secondary leading tone diminished chords
            new Flavor(true, "vii°7/ii", 1, 4, 7, 10),
            new Flavor(true, "vii°7/iii", 3, 6, 9, 12),
            new Flavor(true, "vii°7/IV", 4, 7, 10, 13),
            new Flavor(true, "vii°7/V", 6, 9, 12, 15),
            new Flavor(true, "vii°7/vi", 8, 11, 14, 17),
            // secondary leading tone half-diminished chords
            new Flavor(true, "viih7/ii", 1, 4, 7, 11),
            new Flavor(true, "viih7/iii", 3, 6, 9, 13),
            new Flavor(true, "viih7/IV", 4, 7, 10, 14),
            new Flavor(true, "viih7/V", 6, 9, 12, 16),
            new Flavor(true, "viih7/vi", 8, 11, 14, 18),
            //
            // MINOR
            // diatonic triads
            new Flavor(false, "i", 0, 3, 7),
            new Flavor(false, "ii°", 2, 5, 8),
            new Flavor(false, "III", 3, 7, 10),
            new Flavor(false, "iv", 5, 8, 12),
            new Flavor(false, "v", 7, 10, 14),
            new Flavor(false, "V", 7, 11, 14),
            new Flavor(false, "VI", 8, 12, 15),
            new Flavor(false, "VII", 10, 14, 17),
            new Flavor(false, "vii°", 11, 14, 17),
            // other chords
            new Flavor(false, "Gr6", 8, 12, 15, 18),
            new Flavor(false, "It6", 8, 12, 18),
            new Flavor(false, "N6", 1, 5, 8),
            new Flavor(false, "Fr6", 8, 12, 14, 18),
            new Flavor(false, "Sw6", 8, 12, 15, 18),
            // borrowred chords
            new Flavor(false, "I", 0, 4, 7),
            new Flavor(false, "iii", 3, 6, 10),
            new Flavor(false, "siii", 4, 7, 11),
            new Flavor(false, "sIII", 4, 8, 11),
            new Flavor(false, "vi", 8, 11, 15),
            new Flavor(false, "svi", 9, 12, 16),
            new Flavor(false, "sVI", 9, 13, 16),
            // secondary dominants
            new Flavor(false, "V7/III", 10, 14, 17, 20),
            new Flavor(false, "V7/iv", 0, 4, 7, 10),
            new Flavor(false, "V7/V", 2, 6, 9, 12),
            new Flavor(false, "V7/VI", 3, 7, 10, 13),
            new Flavor(false, "V7/VII", 5, 9, 12, 15),
            // secondary leading tone diminished chords
            new Flavor(false, "vii°7/III", 2, 5, 8, 11),
            new Flavor(false, "vii°7/iv", 4, 7, 10, 13),
            new Flavor(false, "vii°7/V", 6, 9, 12, 15),
            new Flavor(false, "vii°7/VI", 7, 10, 13, 16),
            new Flavor(false, "vii°7/VII", 9, 12, 15, 18),
            // secondary leading tone half-diminished chords
            new Flavor(false, "viih7/III", 2, 5, 8, 12),
            new Flavor(false, "viih7/iv", 4, 7, 10, 14),
            new Flavor(false, "viih7/V", 6, 9, 12, 16),
            new Flavor(false, "viih7/VI", 7, 10, 13, 17),
            new Flavor(false, "viih7/VII", 9, 12, 15, 19)
    };
}
