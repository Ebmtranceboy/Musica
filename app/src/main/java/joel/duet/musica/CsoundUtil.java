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

//import android.util.Log;

import android.content.Context;
import android.os.Environment;
import android.widget.SeekBar;

public final class CsoundUtil {
    /* Log.d("CsoundObj", "FRAMES:" + ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).
                getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));*/
    Context context;
    //private static final String TAG = "CsoundUtil";

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

}
