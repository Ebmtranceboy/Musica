package joel.duet.musica;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * Created by joel on 04/02/16 at 23:19 at 23:21.
 */
public class PreferenceManager {

    private static PreferenceManager self;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    private boolean isInitialised = false;
    private static final String ORCHESTRA_KEY = "Orchestra";
    private static final String FX_KEY = "FX";
    private static final String MATRIX_KEY = "Matrix";
    private static final String TRACKS_KEY = "Tracks";
    private Set<String> Orchestra = null;
    private Set<String> FX = null;
    private String Matrix = null;
    private String Tracks = null;

    @SuppressLint("CommitPrefEdits")
    public void initialize(Context context) {
        if (!isInitialised) {
            preferences = context.getSharedPreferences("MUSICA_PREFS", Context.MODE_PRIVATE);
            editor = preferences.edit();
            loadPreferences();
            isInitialised = true;
        }
    }

    public static PreferenceManager getInstance() {
        if (self == null) {
            self = new PreferenceManager();
        }
        return self;
    }

    private PreferenceManager() {
    }


    public void setOrchestra(Set<String> newPreferenceValue) {
        this.Orchestra = newPreferenceValue;
        savePreferences();
    }

    public void setFX(Set<String> newPreferenceValue) {
        this.FX = newPreferenceValue;
        savePreferences();
    }

    public void setMatrix(String newPreferenceValue) {
        this.Matrix = newPreferenceValue;
        savePreferences();
    }

    public void setTracks(String newPreferenceValue) {
        this.Tracks = newPreferenceValue;
        savePreferences();
    }

    private void savePreferences() {
        editor.putStringSet(ORCHESTRA_KEY, Orchestra);
        for(String instr:Orchestra)
            editor.putString(instr,CSD.mapInstr.get(instr));

        editor.putStringSet(FX_KEY, FX);
        for(String effect:FX)
            editor.putString(effect,CSD.mapFX.get(effect));

        editor.putString(MATRIX_KEY, Matrix);
        editor.putString(TRACKS_KEY, Tracks);
        editor.commit();
    }

    private void loadPreferences() {
        CSD.mapInstr.clear();
        Orchestra = preferences.getStringSet(ORCHESTRA_KEY, new LinkedHashSet<String>());
        for(String instr:Orchestra)
            CSD.mapInstr.put(instr,preferences.getString(instr,null));

        CSD.mapFX.clear();
        FX = preferences.getStringSet(FX_KEY, new LinkedHashSet<String>());
        for(String effect:FX)
            CSD.mapFX.put(effect, preferences.getString(effect, null));

        joel.duet.musica.Matrix.update();
        joel.duet.musica.Matrix.unserialize(preferences.getString(MATRIX_KEY, "FF"));

        Score.resetTracks();
        try {
            //Log.i(TAG,"in:"+pref.getString("Tracks",""));
            Score.loadJSONTracks(new JSONObject(preferences.getString(TRACKS_KEY, null)));
        } catch (JSONException e) {
            e.printStackTrace();
            e.getCause();
        }
    }

}
