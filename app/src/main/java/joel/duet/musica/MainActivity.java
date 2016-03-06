package joel.duet.musica;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
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

import joel.duet.musica.databinding.ActivityMainBinding;
import joel.duet.musica.databinding.ContentMainBinding;

public final class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //private static final String TAG = "Musica";
    static CsoundObj csoundObj = new CsoundObj(false, true);
    final CsoundUtil csoundUtil = new CsoundUtil(this);

    private DrawerLayout mDrawer;
    static Toolbar toolbar;

    private ContentMainBinding cnt_binding;

    public static Runnable sensible_code;
    static State currentFragment;

    public enum State {
        Welcome,
        Instrument, Orchestra,
        Patchbay, Master,
        Live, Fx, Effect,
        Pattern, Score,
        Options, Material
    }

    private <F extends Fragment> void newFragment(State state, F newInstance){
        getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame,
                newInstance,
                state.toString()).commit();
        toolbar.setTitle(state.toString());
        currentFragment = state;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cnt_binding = DataBindingUtil.setContentView(this, R.layout.content_main);
        ActivityMainBinding act_binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        PreferenceManager.getInstance().initialize(this);
        Matrix.getInstance().initialize();

        csoundObj.setMessageLoggingEnabled(true);
        toolbar = act_binding.bar.toolbar;
        mDrawer = act_binding.drawerLayout;

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        mDrawer.addDrawerListener(drawerToggle);

        drawerToggle.syncState();

        /*bar_binding.fab*/
        act_binding.bar.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                csoundObj.stop();
                //csoundObj.forgetMessages();
                csoundObj.getCsound().Stop();
                csoundObj.getCsound().Reset();
                csoundObj.getCsound().Start();
                csoundObj = new CsoundObj(false, true);
                csoundObj.setMessageLoggingEnabled(true);
                newFragment(State.Welcome, new WelcomeFragment());
                toolbar.setTitle(CSD.projectName);
            }
        });

        act_binding.navView.setNavigationItemSelectedListener(this);
        newFragment(State.Welcome, new WelcomeFragment());
    }

    @Override
    public void onBackPressed() {

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            final FragmentManager fragmentManager = getSupportFragmentManager();

            if (currentFragment == State.Instrument) {
                newFragment(State.Orchestra, new OrchestraFragment());

            } else if (currentFragment == State.Effect) {
                newFragment(State.Fx, new FXFragment());

            } else if (currentFragment == State.Pattern) {
                newFragment(State.Score, new ScoreFragment());

            } else {
                if (currentFragment != State.Welcome) {
                    if(currentFragment == State.Material){
                        MaterialFragment fragment =
                                (MaterialFragment) fragmentManager.findFragmentByTag("Material");
                        CSD.globals = fragment.getGlobals();
                    }

                    newFragment(State.Welcome, new WelcomeFragment());
                    toolbar.setTitle(CSD.projectName);
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
            JSONObject project =
                    new JSONObject(csoundUtil.getExternalFileAsString(csd.getAbsolutePath()));
            PreferenceManager.loadProject(project);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        newFragment(State.Welcome, new WelcomeFragment());
        toolbar.setTitle(CSD.projectName);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mDrawer.closeDrawer(GravityCompat.START);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_orchestra) {
            csoundObj.stop();
            newFragment(State.Orchestra, new OrchestraFragment());

        } else if (id == R.id.nav_patchbay) {
            csoundObj.stop();
            newFragment(State.Patchbay, new PatchBayFragment());

        } else if (id == R.id.nav_master) {
            newFragment(State.Master, new MasterFragment());

        } else if (id == R.id.nav_live) {
            if (CSD.getNbInstruments() > 0) {
                csoundObj.stop();
                newFragment(State.Live, new LiveFragment());
            } else {
                final Toast toast = Toast.makeText(this,
                        "Please, create an instrument first", Toast.LENGTH_LONG);
                toast.show();
            }

        } else if (id == R.id.nav_fx) {
            csoundObj.stop();
            newFragment(State.Fx, new FXFragment());

        } else if (id == R.id.nav_score) {
            csoundObj.stop();
            newFragment(State.Score, new ScoreFragment());

        } else if (id == R.id.nav_material) {
            // TODO : implement synthpad generator, formant generators
            // csoundObj.stop();
            newFragment(State.Material, new MaterialFragment());
            toolbar.setTitle("Globals");


        } else if (id == R.id.new_project) {
            csoundObj.stop();
            sensible_code = new Runnable() {
                @Override
                public void run() {
                    PreferenceManager.resetProject();
                    newFragment(State.Welcome, new WelcomeFragment());
                    CSD.projectName = Default.new_project_name;
                    toolbar.setTitle(CSD.projectName);
                }
            };

            final ConfirmationFragment confirmation = new ConfirmationFragment();
            confirmation.show(fragmentManager, "New project Fragment");

        } else if (id == R.id.open_project) {
            csoundObj.stop();
            sensible_code = new Runnable() {
                @Override
                public void run() {
                    SimpleFileDialog fileOpenDialog = new SimpleFileDialog(
                            new ContextThemeWrapper(MainActivity.this, R.style.csoundAlertDialogStyle),
                            "FileOpen..",
                            new SimpleFileDialog.SimpleFileDialogListener() {
                                @Override
                                public void onChosenDir(String chosenDir) {
                                    File file = new File(chosenDir);
                                    CSD.projectName = CSD.extractName(file.getName());
                                    MainActivity.this.OnFileChosen(file);
                                }
                            }
                    );
                    if (csd != null) {
                        fileOpenDialog.default_file_name = csd.getAbsolutePath();
                    } else {
                        fileOpenDialog.default_file_name =
                                Environment.getExternalStorageDirectory().getAbsolutePath();
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
                            CSD.projectName = CSD.extractName(newFile.getName());
                            csoundUtil.saveStringAsExternalFile(PreferenceManager.project().toString(),
                                    newFile.getAbsolutePath());
                        }
                    }
            );
            if (csd != null) {
                fileOpenDialog.default_file_name = csd.getParent();
            } else {
                fileOpenDialog.default_file_name =
                        Environment.getExternalStorageDirectory().getAbsolutePath();
            }
            fileOpenDialog.chooseFile_or_Dir(fileOpenDialog.default_file_name);

        }  else if (id == R.id.render_project) {
            csoundUtil.saveStringAsExternalFile(Score.sendPatterns(Score.allPatterns(), true, 0),
                    "/storage/sdcard0/" + CSD.projectName + ".csd");

        } else if (id == R.id.nav_preferences) {
            // TODO : default sr, ksmps, nbchnls, 0dbfs
            csoundObj.stop();
            newFragment(State.Options, new OptionsFragment());
        }

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final View mView = cnt_binding.mainFrame;
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
                        case Orchestra:
                            newFragment(State.Orchestra, new OrchestraFragment());
                            break;

                        case Instrument:
                            InstrumentFragment fragment =
                                    (InstrumentFragment) fragmentManager.findFragmentByTag("Instrument");
                            Bundle bundle = new Bundle();
                            String instrName = fragment.instrumentName;
                            bundle.putString("instrName", instrName);
                            fragment = new InstrumentFragment();
                            fragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    fragment,
                                    "Instrument").commit();
                            toolbar.setTitle(instrName);
                            break;

                        case Patchbay:
                            newFragment(State.Patchbay, new PatchBayFragment());
                            break;

                        case Master:
                            newFragment(State.Master, new MasterFragment());
                            break;

                        case Live:
                            newFragment(State.Live, new LiveFragment());
                            break;

                        case Fx:
                            newFragment(State.Fx, new FXFragment());
                            break;

                        case Effect:
                            EffectFragment fr = (EffectFragment) fragmentManager.findFragmentByTag("Effect");
                            bundle = new Bundle();
                            String effectName = fr.effectName;
                            bundle.putString("effectName", effectName);
                            fr = new EffectFragment();
                            fr.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    fr,
                                    "Effect").commit();
                            toolbar.setTitle(effectName);
                            break;

                        case Score:
                            newFragment(State.Score, new ScoreFragment());

                            break;

                        case Pattern:
                            bundle = new Bundle();
                            bundle.putInt("resolution_index", Track.getPatternSelected().resolution);
                            bundle.putString("instr_name", Track.getPatternSelected().getInstr());
                            PatternFragment patternFragment = new PatternFragment();
                            patternFragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    patternFragment,
                                    "Pattern").commit();
                            String format = getResources().getString(R.string.pattern_title);
                            toolbar.setTitle(String.format(format, Score.getIdTrackSelected(), Track.getIdPatternSelected()));

                            break;

                        case Material:
                            newFragment(State.Material, new MaterialFragment());
                            toolbar.setTitle("Globals");
                            break;

                        case Options:
                            newFragment(State.Options, new OptionsFragment());
                            break;

                        default:
                            newFragment(State.Welcome, new WelcomeFragment());
                            toolbar.setTitle(CSD.projectName);
                    }
                }
            }
        });
        super.onConfigurationChanged(newConfig);
    }
}
