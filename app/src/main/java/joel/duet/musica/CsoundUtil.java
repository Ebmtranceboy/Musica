/* 
 
 BaseCsoundActivity.java:
 
 Copyright (C) 2011 Victor Lazzarini, Steven Yi
 
 This file is part of Csound Android Examples.
 
 The Csound Android Examples is free software; you can redistribute it
 and/or modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.   
 
 Csound is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with Csound; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA
 
 */

package joel.duet.musica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import android.content.Context;
import android.os.Environment;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.List;
import java.util.regex.*;

public final class CsoundUtil {
    /* Log.d("CsoundObj", "FRAMES:" + ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).
                getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));*/
    Context context;
    private static final String TAG = "CsoundUtil";

    public CsoundUtil(Context ctx) {
        context = ctx;
    }

    public void setSeekBarValue(SeekBar seekBar, double min, double max, double value) {
        double range = max - min;
        double percent = (value - min) / range;

        seekBar.setProgress((int) (percent * seekBar.getMax()));
    }

    protected String getResourceFileAsString(int resId) {
        StringBuilder str = new StringBuilder();

        InputStream is = context.getResources().openRawResource(resId);
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String line;

        try {
            while ((line = r.readLine()) != null) {
                str.append(line).append("\n");
            }
        } catch (IOException ios) {
            ios.printStackTrace();
        }

        return str.toString();
    }

    protected File createTempFile(String csd) {
        File f = null;

        try {
            f = File.createTempFile("temp", ".csd", context.getCacheDir());
            //Log.i(TAG,context.getCacheDir().getName());
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(csd.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    protected String getExternalFileAsString(String filename) {
        final File root = Environment.getExternalStorageDirectory();
        try {
            if (root.canRead()) {
                FileReader filereader =
                        new FileReader(
                                new File(root, filename));
                final BufferedReader in = new BufferedReader(filereader);
                final StringBuilder buffer = new StringBuilder("");
                String line;
                while ((line = in.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                in.close();
                return buffer.toString();
            }
        } catch (IOException e) {
            return "";
        }
        return "";
    }

    public void patternize(String instr_name) {
        final int idTrackSelected = Score.getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();
        Score.createTrack();
        Score.setTrackSelected(Score.getNbOfTracks());
        Track track = Score.getTrackSelected();
        track.createPattern();
        Track.setPatternSelected(1);
        Pattern pattern = Track.getPatternSelected();
        pattern.setInstr(instr_name);

        //Log.i(TAG, unisonConvert(60.0));

        String lines[] = getExternalFileAsString("unisonMelody.txt").split("\\n");
        final java.util.regex.Pattern istatement =
                java.util.regex.Pattern.compile("\\s*i\\s*\\d+\\s+(\\d+.?\\d*)\\s+(\\d+.?\\d*)\\s+(\\d+.?\\d*)\\s+(-?\\d+.?\\d*)");

        pattern.start = -1;
        pattern.finish = 0;
        for (String line : lines) {
            Matcher matcher = istatement.matcher(line);
            while (matcher.find()) {
                int onset = (int) Math.round(Double.parseDouble(matcher.group(1)) * Default.ticks_per_second);
                int duration = (int) Math.round(Double.parseDouble(matcher.group(2)) * Default.ticks_per_second);

                if (pattern.start < 0) pattern.start = onset;
                if (onset + duration > pattern.finish) pattern.finish = onset + duration;

                int pitch = (int) Math.round(Double.parseDouble(matcher.group(3)) * 100);
                int key = pitch % 100;
                int oct = (pitch - key) / 100 - 3;
                pattern.createNote(onset - pattern.start, duration, oct * 12 + key);
            }
        }
        pattern.finish += 1;//28; // a full note

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);
    }

    private Integer quantize(double r) {
        if (r < 6) return 6;
        int pow2 = 1;
        while (!(6 * pow2 <= r && r < 8 * pow2
                || 8 * pow2 <= r && r < 9 * pow2
                || 9 * pow2 <= r && r < 6 * pow2 * 2)) pow2 *= 2;
        if (6 * pow2 <= r && r < 8 * pow2)
            if (r < 7 * pow2) return 6 * pow2;
            else return 8 * pow2;
        else if (8 * pow2 <= r && r < 9 * pow2)
            if (r < 8.5 * pow2) return 8 * pow2;
            else return 9 * pow2;
        else if (r < 10.5 * pow2) return 9 * pow2;
        else return 6 * pow2 * 2;
    }

    public void quantPatternize(String instr_name, double tempo) {

        final int idTrackSelected = Score.getIdTrackSelected();
        final int idPatternSelected = Track.getIdPatternSelected();
        Score.createTrack();
        Score.setTrackSelected(Score.getNbOfTracks());
        Track track = Score.getTrackSelected();
        track.createPattern();
        Track.setPatternSelected(1);
        Pattern pattern = Track.getPatternSelected();
        pattern.setInstr(instr_name);

        List<String> score_lines = Arrays.asList(getExternalFileAsString("unisonMelody.txt").split("\\n"));

        int score_size = score_lines.size();
        double[] differences = new double[score_size];
        double[] lengths = new double[score_size];
        int[] durations = new int[score_size];
        int[] onsets = new int[score_size];
        int[] pitches = new int[score_size];

        for (int i = 0; i < score_size; i++) {
            String[] items = score_lines.get(i).split(" +");
            differences[i] = Double.parseDouble(items[2]);
            lengths[i] = Double.parseDouble(items[3]);
            int pitch = (int) Math.round( Double.parseDouble(items[4]) * 100);
            int key = pitch % 100;
            int oct = (pitch - key) / 100 - 3;
            pitches[i] = oct * 12 + key;
        }

        for (int i = 0; i < score_size - 1; i++)
            differences[i] = differences[i + 1] - differences[i];
        differences[score_size - 1] = lengths[score_size - 1];

        pattern.start = 0;
        pattern.finish = 0;
        for (int i = 0; i < score_size; i++) {
            pattern.createNote(pattern.finish, (int)Math.round(lengths[i]*Default.ticks_per_second), pitches[i]);
            int quant = Math.round(quantize(differences[i] * 96. * tempo / 60.) / 3);
            pattern.finish += quant;
        }

        pattern.finish += 1;//28; // a full note

        Score.setTrackSelected(idTrackSelected);
        Track.setPatternSelected(idPatternSelected);

    }
}
