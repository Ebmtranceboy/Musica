package joel.duet.musica;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
//import android.util.Log;
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

import com.csounds.CsoundObj;

//import com.csounds.bindings.ui.CsoundButtonBinding;
//import com.csounds.bindings.ui.CsoundSliderBinding;

public final class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final CsoundObj csoundObj = new CsoundObj(false, true);
    final CsoundUtil csoundUtil = new CsoundUtil(this);
    static SharedPreferences pref;
    static SharedPreferences.Editor editor;
    private DrawerLayout mDrawer;
    ActionBarDrawerToggle drawerToggle;
    static Toolbar toolbar;
    public static Runnable sensible_code;
    //private static final String TAG = "Musica";

    //Button startCsound, stopCsound;

    public enum State {
        WELCOME, LIVE, ORCHESTRA, INSTRUMENT, PATCHBAY, FX, EFFECT //, SCORE, MASTER, PREFERENCES, MATERIAL
    }

    static State currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int MODE_PRIVATE = 0;
        pref = getApplicationContext().getSharedPreferences("MUSICA_PREFS",
                MODE_PRIVATE);
        editor = pref.edit();

        for(String instr:pref.getStringSet("Orchestra",CSD.mapInstr.keySet())) {
            CSD.mapInstr.put(instr, pref.getString(instr, ""));
        }

        for(String instr:pref.getStringSet("FX",CSD.mapFX.keySet())) {
            CSD.mapFX.put(instr, pref.getString(instr, ""));
        }

        Matrix.reset();
        Matrix.unserialize(pref.getString("Matrix",""));

        setContentView(R.layout.activity_main);
        csoundObj.setMessageLoggingEnabled(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
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
            if(currentFragment==State.INSTRUMENT){
                InstrumentFragment fragment = (InstrumentFragment)fragmentManager.findFragmentByTag("INSTRUMENT");
                CSD.mapInstr.put(fragment.getInstrName(),fragment.getInstrCode());

                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new OrchestraFragment(),
                                    "ORCHESTRA").commit();
                toolbar.setTitle("Orchestra");
                currentFragment = State.ORCHESTRA;
            } else if(currentFragment==State.EFFECT){
                EffectFragment fragment = (EffectFragment)fragmentManager.findFragmentByTag("EFFECT");
                CSD.mapFX.put(fragment.getEffectName(), fragment.getEffectCode());

                fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new FXFragment(),
                                    "FX").commit();
                toolbar.setTitle("FX");
                currentFragment = State.FX;
            }
              else {
                if(currentFragment != State.WELCOME){
                    fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    new WelcomeFragment(),
                                    "WELCOME").commit();
                toolbar.setTitle("Musica");
                currentFragment = State.WELCOME;
                } else super.onBackPressed();
            }
        }

        editor.putStringSet("Orchestra",CSD.mapInstr.keySet());
        for(String str:CSD.mapInstr.keySet()) {
            //Log.i(TAG,"out :" + str);
            editor.putString(str, CSD.mapInstr.get(str));
        }
        editor.putStringSet("FX",CSD.mapFX.keySet());
        for(String str:CSD.mapFX.keySet()) {
            //Log.i(TAG,"out :" + str);
            editor.putString(str, CSD.mapFX.get(str));
        }
        editor.putString("Matrix",Matrix.serialize());
        editor.apply();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        mDrawer.closeDrawer(GravityCompat.START);
        csoundObj.stop();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.nav_live) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new LiveFragment(),
                    "LIVE").commit();
            toolbar.setTitle("Live");
            currentFragment = State.LIVE;

        } else if (id == R.id.nav_orchestra) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new OrchestraFragment(),
                    "ORCHESTRA").commit();
            toolbar.setTitle("Orchestra");
            currentFragment = State.ORCHESTRA;

        } else if (id == R.id.nav_score) {

        } else if (id == R.id.nav_preferences) {

        } else if (id == R.id.nav_fx) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new FXFragment(),
                    "FX").commit();
            toolbar.setTitle("FX");
            currentFragment = State.FX;

        } else if (id == R.id.nav_patchbay) {
            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                    new PatchBayFragment(),
                    "PATCHBAY").commit();
            toolbar.setTitle("Patch Bay");
            currentFragment = State.PATCHBAY;

        } else if (id == R.id.nav_master) {

        } else if (id == R.id.nav_material) {

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
                            bundle.putString("instrName",instrName);
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
                            bundle.putString("effectName",effectName);
                            fr = new EffectFragment();
                            fr.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                    fr,
                                    "EFFECT").commit();
                            toolbar.setTitle(effectName);
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
