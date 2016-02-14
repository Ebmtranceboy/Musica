package joel.duet.musica;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
//import android.util.Log;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.csounds.CsoundObj;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public final class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static CsoundObj csoundObj = new CsoundObj(false, true);
    final CsoundUtil csoundUtil = new CsoundUtil(this);
    private DrawerLayout mDrawer;
    static Toolbar toolbar;
    public static Runnable sensible_code;
    //private static final String TAG = "Musica";

    public enum State {
        WELCOME, LIVE, ORCHESTRA, INSTRUMENT, PATCHBAY, FX, EFFECT, SCORE, PATTERN //, MASTER, PREFERENCES, MATERIAL
    }

    static State currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getInstance().initialize(this);
        Matrix.getInstance().initialize();

        setContentView(R.layout.activity_main);
        csoundObj.setMessageLoggingEnabled(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                csoundObj.stop();
                csoundObj = new CsoundObj(false, true);
                csoundObj.setMessageLoggingEnabled(true);
                getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                        new WelcomeFragment(),
                        "WELCOME").commit();
                toolbar.setTitle("Musica");
                currentFragment = State.WELCOME;
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                new WelcomeFragment(),
                "WELCOME").commit();
        toolbar.setTitle("Musica");
        currentFragment = State.WELCOME;
    }

    @Override
    public void onBackPressed() {

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            if (currentFragment == State.INSTRUMENT) {
                InstrumentFragment fragment = (InstrumentFragment) fragmentManager.findFragmentByTag("INSTRUMENT");
                CSD.mapInstr.put(fragment.getInstrName(), fragment.getInstrCode());

                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        new OrchestraFragment(),
                        "ORCHESTRA").commit();
                toolbar.setTitle("Orchestra");
                currentFragment = State.ORCHESTRA;
            } else if (currentFragment == State.EFFECT) {
                EffectFragment fragment = (EffectFragment) fragmentManager.findFragmentByTag("EFFECT");
                CSD.mapFX.put(fragment.getEffectName(), fragment.getEffectCode());

                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        new FXFragment(),
                        "FX").commit();
                toolbar.setTitle("FX");
                currentFragment = State.FX;
            } else if (currentFragment == State.PATTERN) {
                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        new ScoreFragment(),
                        "SCORE").commit();
                toolbar.setTitle("Score");
                currentFragment = State.SCORE;
            } else {
                if (currentFragment != State.WELCOME) {
                    fragmentManager.beginTransaction().replace(R.id.mainFrame,
                            new WelcomeFragment(),
                            "WELCOME").commit();
                    toolbar.setTitle("Musica");
                    currentFragment = State.WELCOME;
                } else super.onBackPressed();
            }
        }

        PreferenceManager.getInstance().savePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File csd;

    private void OnFileChosen(File file) {
        csd = file;
        PreferenceManager.resetProject();
        try {
            JSONObject project = new JSONObject(csoundUtil.getExternalFileAsString(csd.getAbsolutePath()));
            PreferenceManager.loadProject(project);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                new WelcomeFragment(),
                "WELCOME").commit();
        toolbar.setTitle("Musica");
        currentFragment = State.WELCOME;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mDrawer.closeDrawer(GravityCompat.START);
        csoundObj.stop();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_orchestra) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new OrchestraFragment(),
                    "ORCHESTRA").commit();
            toolbar.setTitle("Orchestra");
            currentFragment = State.ORCHESTRA;

        } else if (id == R.id.nav_patchbay) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new PatchBayFragment(),
                    "PATCHBAY").commit();
            toolbar.setTitle("Patch Bay");
            currentFragment = State.PATCHBAY;

        } else if (id == R.id.nav_live) {
            if (CSD.getNbInstruments() > 0) {
                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                        new LiveFragment(),
                        "LIVE").commit();
                toolbar.setTitle("Live");
                currentFragment = State.LIVE;
            } else {
                final Toast toast = Toast.makeText(this,
                        "Please, create an instrument first", Toast.LENGTH_LONG);
                toast.show();
            }

        } else if (id == R.id.nav_fx) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new FXFragment(),
                    "FX").commit();
            toolbar.setTitle("FX");
            currentFragment = State.FX;

        } else if (id == R.id.nav_score) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new ScoreFragment(),
                    "SCORE").commit();
            toolbar.setTitle("Score");
            currentFragment = State.SCORE;

        } else if (id == R.id.nav_master) {
            // TODO : implement gains

        } else if (id == R.id.nav_material) {
            // TODO : implement synthpad generator

        } else if (id == R.id.new_project) {
            sensible_code = new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.resetProject();
                    fragmentManager.beginTransaction().replace(R.id.mainFrame,
                            new WelcomeFragment(),
                            "WELCOME").commit();
                    toolbar.setTitle("Musica");
                    currentFragment = State.WELCOME;
                }
            };

            final ConfirmationFragment confirmation = new ConfirmationFragment();
            confirmation.show(fragmentManager, "New project Fragment");

        } else if (id == R.id.open_project) {
            sensible_code = new Runnable() {
                @Override
                public void run() {
                    SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                            new ContextThemeWrapper(MainActivity.this, R.style.csoundAlertDialogStyle),
                            "FileOpen..",
                            new SimpleFileDialog.SimpleFileDialogListener() {
                                @Override
                                public void onChosenDir(String chosenDir) {
                                    MainActivity.this.OnFileChosen(new File(chosenDir));
                                }
                            }
                    );
                    if (csd != null) {
                        fileOpenDialog.default_file_name = csd.getAbsolutePath();
                    } else {
                        fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
                    }
                    fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);
                }
            };

            final ConfirmationFragment confirmation = new ConfirmationFragment();
            confirmation.show(fragmentManager, "Open project Fragment");

        } else if (id == R.id.save_project) {//GZIPOutputStream
            // TODO warns if existing file
            SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                    new ContextThemeWrapper(MainActivity.this, R.style.csoundAlertDialogStyle),
                    "FileSave..",
                    new SimpleFileDialog.SimpleFileDialogListener() {
                        @Override
                        public void onChosenDir(String chosenDir) {
                            int index = chosenDir.indexOf("//");
                            if (index >= 0) {
                                chosenDir = chosenDir.substring(index + 1);
                            }
                            File newFile = new File(chosenDir);
                            csoundUtil.saveStringAsExternalFile(PreferenceManager.project().toString(), newFile.getAbsolutePath());
                        }
                    }
            );
            if (csd != null) {
                fileOpenDialog.default_file_name = csd.getParent();
            } else {
                fileOpenDialog.default_file_name = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);

        } else if (id == R.id.nav_preferences)
// TODO : default sr, ksmps, nbchnls, 0dbfs
        {

        }

        return true;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final View mView = findViewById(R.id.mainFrame);
        final int oldHeight = mView.getHeight();
        final int oldWidth = mView.getWidth();

        mView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mView.getHeight() != oldHeight && mView.getWidth() != oldWidth) {
                    mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    //mView now has the correct dimensions, continue with your stuff
                    final FragmentManager fragmentManager = getSupportFragmentManager();

                    switch (currentFragment) {
                        case LIVE:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new LiveFragment(),
                                    "LIVE").commit();
                            toolbar.setTitle("Live");
                            break;
                        case ORCHESTRA:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new OrchestraFragment(),
                                    "ORCHESTRA").commit();
                            toolbar.setTitle("Orchestra");
                            break;
                        case INSTRUMENT:
                            InstrumentFragment fragment = (InstrumentFragment) fragmentManager.findFragmentByTag("INSTRUMENT");
                            Bundle bundle = new Bundle();
                            String instrName = fragment.getInstrName();
                            bundle.putString("instrName", instrName);
                            fragment = new InstrumentFragment();
                            fragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    fragment,
                                    "INSTRUMENT").commit();
                            toolbar.setTitle(instrName);
                            break;
                        case PATCHBAY:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new PatchBayFragment(),
                                    "PATCHBAY").commit();
                            toolbar.setTitle("Patch Bay");
                            break;
                        case FX:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new FXFragment(),
                                    "FX").commit();
                            toolbar.setTitle("FX");
                            break;
                        case EFFECT:
                            EffectFragment fr = (EffectFragment) fragmentManager.findFragmentByTag("EFFECT");
                            bundle = new Bundle();
                            String effectName = fr.getEffectName();
                            bundle.putString("effectName", effectName);
                            fr = new EffectFragment();
                            fr.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    fr,
                                    "EFFECT").commit();
                            toolbar.setTitle(effectName);
                            break;

                        case SCORE:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new ScoreFragment(),
                                    "SCORE").commit();
                            toolbar.setTitle("Score");
                            break;
                        case PATTERN:
                            bundle = new Bundle();
                            bundle.putInt("resolution_index", Track.getPatternSelected().resolution);
                            bundle.putString("instr_name", Track.getPatternSelected().getInstr());
                            PatternFragment patternFragment = new PatternFragment();
                            patternFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    patternFragment,
                                    "PATTERN").commit();
                            String format = getResources().getString(R.string.pattern_title);
                            toolbar.setTitle(String.format(format, Score.getIdTrackSelected(), Track.getIdPatternSelected()));

                            currentFragment = MainActivity.State.PATTERN;
                            break;

                        default:
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new WelcomeFragment(),
                                    "WELCOME").commit();
                            toolbar.setTitle("Musica");
                    }
                }
            }
        });
        super.onConfigurationChanged(newConfig);
    }
}
