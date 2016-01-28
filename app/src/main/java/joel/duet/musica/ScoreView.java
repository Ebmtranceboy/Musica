package joel.duet.musica;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Matrix;
import android.widget.Toast;

/**
 *
 * Created by joel on 22/01/16 at 23:18 at 08:57.
 */
public final class ScoreView extends View {
        // alternation between 16-bars (0), 8-bars(2), 4-bars(4), 2-bars(6) and 1-bars(8)
    private static final int[] order = {0, 8, 6, 8, 4, 8, 6, 8, 2, 8, 6, 8, 4, 8, 6, 8, 0};
    private static final Paint paint = new Paint();

    private static final float[] coords = new float[2];
    private static final Matrix mMatrix = new Matrix();
    private static final Matrix mInverse = new Matrix();

    public static boolean edit_mode;
    public static Tool tool;
    private static int bar_begin = -1;
    private static int bar_end;
    private static int insertion_track;

    public static int number_patches = 1;
    public static int tracks_displayed = Default.min_tracks_displayed;
    public static float track_height =
            (1.0f - (Default.top_margin + Default.bottom_margin)) / (float) tracks_displayed;

    // TODO Change positions and factors only while no bound has been reached

    public static float mScaleFactorX = 1f;
    public static float mScaleFactorY = 1f;

    private static float mLastTouchX, mLastTouchY;
    public static float mPosX, mPosY;
    private static int mActivePointerId;

    private static int width;
    private static int height;

    private final Context context;
    private final ScaleGestureDetector mScaleDetector;

    public ScoreView(Context ctx) {
        super(ctx);
        paint.setAntiAlias(true);

        mScaleDetector = new ScaleGestureDetector(ctx, new ScaleListener());
        context = ctx;
    }


    private static void drawBackground(Canvas canvas) {
        ScoreView.paint.setStyle(Paint.Style.FILL);
        ScoreView.paint.setColor(Color.parseColor("#7F7F7F"));
        canvas.drawPaint(ScoreView.paint);
    }

    private static void setStrokeBlack() {
        ScoreView.paint.setStyle(Paint.Style.STROKE);
        ScoreView.paint.setColor(Color.parseColor("#000000"));
    }

    private static void setStrokeWhite() {
        ScoreView.paint.setStyle(Paint.Style.STROKE);
        ScoreView.paint.setColor(Color.parseColor("#FFFFFF"));
    }

    private static void setBlueAlpha(int alpha) {
        ScoreView.paint.setStyle(Paint.Style.FILL);
        ScoreView.paint.setARGB(alpha, 0, 71, 136);
    }

    private static void setFillAlpha(int alpha) {
        ScoreView.paint.setStyle(Paint.Style.FILL);
        ScoreView.paint.setARGB(alpha, 255, 255, 255);
    }

    private static void drawReset(Canvas canvas) {
        drawBackground(canvas);
        setStrokeWhite();
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (Math.abs(detector.getCurrentSpanX()) < Math.abs(detector.getCurrentSpanY()))
                mScaleFactorY *= detector.getScaleFactor();
            else if (Math.abs(detector.getCurrentSpanY()) < Math.abs(detector.getCurrentSpanX()))
                mScaleFactorX *= detector.getScaleFactor();
            else {
                mScaleFactorY *= detector.getScaleFactor();
                mScaleFactorX *= detector.getScaleFactor();
            }

            // Don't let the object get too small or too large.
            mScaleFactorX = Math.max(0.1f, Math.min(mScaleFactorX, 5.0f));
            mScaleFactorY = Math.max(0.1f, Math.min(mScaleFactorY, 5.0f));

            invalidate();
            return true;
        }
    }

    public static class Focus {
        static Track track;
        static int track_id;
        static Pattern pattern;
        static int pattern_id;

        public static void reset() {
            track_id = 0;
            pattern_id = 0;
        }

        private static void save() {
            if (Score.getIdTrackSelected() > 0) {
                track = Score.getTrackSelected();
                track_id = Score.getIdTrackSelected();
            }
            if (Track.getIdPatternSelected() > 0) {
                pattern = Track.getPatternSelected();
                pattern_id = Track.getIdPatternSelected();
            }
        }

        private static void restore() {
            if (Score.getIdTrackSelected() > 0) Score.setTrackSelected(track_id);
            if (Track.getIdPatternSelected() > 0) Track.setPatternSelected(pattern_id);
        }
    }

    private void drawPatternPreview(float x0, float y0, float x1, float y1,
                                    Pattern pattern,Canvas canvas){
        int n;
        int om = Integer.MAX_VALUE, dM = 0;
        int pm = Integer.MAX_VALUE, pM = 0;
        for(n=1; n<=pattern.getNbOfNotes(); n++){
            Pattern.setNoteSelected(n);
            final Pattern.Note note = Pattern.getNoteSelected();
            if(note.onset < om) om = note.onset;
            if(dM<note.onset+note.duration) dM = note.onset+note.duration;
            if(note.pitch<pm) pm = note.pitch;
            if(pM<note.pitch) pM = note.pitch;
        }

        final float kx = (x1-x0)/(1.1f*(dM-om));
        final float ky = (y1-y0)/(1.1f*(pM-pm+2));

        for(n=1; n<=pattern.getNbOfNotes(); n++) {
            Pattern.setNoteSelected(n);
            final Pattern.Note note = Pattern.getNoteSelected();
            canvas.drawRect(x0 + (note.onset-om)*kx
                    , y1 - (note.pitch-pm+1.5f)*ky
                    , x0 + (note.onset + note.duration - om)*kx
                    , y1 - (note.pitch-pm+0.5f)*ky, paint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = getWidth();
        height = getHeight();
        drawReset(canvas);

        canvas.save();

        mMatrix.setTranslate(mPosX, mPosY);
        mMatrix.postScale(mScaleFactorX, mScaleFactorY
                , super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        mMatrix.invert(mInverse);
        coords[0] = 0;
        coords[1] = 10;
        mInverse.mapPoints(coords);

        canvas.concat(mMatrix);

        Focus.save();

        for (int t = 1; t <= Score.getNbOfTracks(); t++) {
            canvas.drawText("" + t, coords[0], (t - 0.5f) * track_height * height, paint);
            for (int i = 0; i < number_patches; i++) {
                drawPatch(i, canvas, t);
            }
            Score.setTrackSelected(t);
            final Track track = Score.getTrackSelected();
            for (int p = 1; p <= track.getNbOfPatterns(); p++) {
                Track.setPatternSelected(p);
                final Pattern pattern = Track.getPatternSelected();
                if (edit_mode && track == Focus.track && pattern == Focus.pattern) setBlueAlpha(112);
                else setStrokeBlack();
                canvas.drawRect(pattern.start * width / 16 / 32
                        , (t - 1) * track_height * height
                        , pattern.finish * width / 16 / 32
                        , t * track_height * height, paint);
                drawPatternPreview(pattern.start * width / 16 / 32
                        , (t - 1) * track_height * height
                        , pattern.finish * width / 16 / 32
                        , t * track_height * height, pattern,canvas);
                if (edit_mode && track == Focus.track && pattern == Focus.pattern) setBlueAlpha(12);
                else setFillAlpha(192);
                canvas.drawRect(pattern.start * width / 16 / 32
                        , (t - 1) * track_height * height
                        , pattern.finish * width / 16 / 32
                        , t * track_height * height, paint);

            }
        }

        if (bar_begin >= 0) {
            setFillAlpha(64);
            canvas.drawRect(bar_begin * width / 16 * Score.getResolution() / 32
                    , (insertion_track - 1) * track_height * height
                    , (bar_end + 1) * width / 16 * Score.getResolution() / 32
                    , insertion_track * track_height * height, paint);
        }
        Focus.restore();
        canvas.restore();
    }

    private static int closestX(float x0) {
        float d, dist = Float.MAX_VALUE;
        float x;
        int p, i, c = 0;
        for (p = 0; p < number_patches; p++) {
            for (i = 0; i < 16; i++) {
                x = (p * width + i * width / 16 + width / 32) * Score.getResolution() / 32;
                d = Math.abs(x - x0);
                if (d < dist) {
                    dist = d;
                    c = p * 16 + i;
                }
            }
        }
        return c;
    }

    private static int closestY(float y0) {
        float d, dist = Float.MAX_VALUE;
        float y;
        int t, c = 0;
        for (t = 1; t <= Score.getNbOfTracks(); t++) {
            y = (t - 0.5f) * track_height * height;
            d = Math.abs(y - y0);
            if (d < dist) {
                dist = d;
                c = t;
            }
        }
        return c;
    }

    private static boolean nonOverlapping(Track track, int start, int finish) {
        int p = 1;
        boolean non_overlap = true;
        while (p <= track.getNbOfPatterns() && non_overlap) {
            Track.setPatternSelected(p);
            Pattern pattern = Track.getPatternSelected();
            non_overlap = (pattern.start <= start && pattern.finish <= start)
                    || (pattern.start >= finish && pattern.finish >= finish);
            p++;
        }
        return non_overlap;
    }

    public enum Tool{
        NONE,
        COPY,
        MOVE
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                coords[0] = x;
                coords[1] = y;
                mInverse.mapPoints(coords);

                if (edit_mode && Score.getNbOfTracks() > 0) {
                    Focus.save();
                    insertion_track = closestY(coords[1]);
                    Score.setTrackSelected(insertion_track);
                    final Track track = Score.getTrackSelected();
                    boolean existing_pattern = false;
                    int p = 1;
                    while (p <= track.getNbOfPatterns() && !existing_pattern) {
                        Track.setPatternSelected(p);
                        final Pattern pattern = Track.getPatternSelected();
                        existing_pattern =
                                pattern.start * width / 16 / 32 <= coords[0]
                                        && coords[0] <= pattern.finish * width / 16 / 32;
                        p++;
                    }

                    if (existing_pattern) {
                        Focus.save();
                        FragmentManager fragmentManager = MasterFragment.activity.getSupportFragmentManager();

                        Bundle bundle = new Bundle();
                        bundle.putInt("resolution", Track.getPatternSelected().resolution);
                        bundle.putString("instr_name",Track.getPatternSelected().getInstr());
                        PatternFragment fragment = new PatternFragment();
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.mainFrame,
                                fragment,
                                "PATTERN").commit();
                        String format = getResources().getString(R.string.pattern_title);
                        MainActivity.toolbar.setTitle(String.format(format, Score.getIdTrackSelected(), Track.getIdPatternSelected()));
                        MainActivity.currentFragment = MainActivity.State.PATTERN;


                    } else {
                        if (tool != Tool.NONE && Track.getIdPatternSelected() > 0) {
                            final int start = closestX(coords[0]) * Score.getResolution();
                            final int finish = start + Focus.pattern.finish - Focus.pattern.start;
                            if (nonOverlapping(track, start, finish)) {
                                if(tool == Tool.MOVE && track == Focus.track){
                                    Focus.pattern.start = start;
                                    Focus.pattern.finish = finish;
                                }
                                else{
                                    track.createPattern();
                                    Track.setPatternSelected(track.getNbOfPatterns());
                                    final Pattern pattern = Track.getPatternSelected();
                                    pattern.start = start;
                                    pattern.finish = finish;
                                    pattern.setInstr(Focus.pattern.getInstr());
                                    pattern.mPosX = Focus.pattern.mPosX;
                                    pattern.mPosY = Focus.pattern.mPosY;
                                    pattern.mScaleFactorX = Focus.pattern.mScaleFactorX;
                                    pattern.mScaleFactorY = Focus.pattern.mScaleFactorY;
                                    pattern.resolution = Focus.pattern.resolution;
                                    for (int n = 1; n <= Focus.pattern.getNbOfNotes(); n++) {
                                        final Pattern.Note note = Focus.pattern.getNote(n - 1);
                                        pattern.createNote(note.onset, note.duration, note.pitch);
                                    }
                                    if(tool == Tool.MOVE){
                                        Focus.track.deletePattern(Focus.pattern);
                                        Focus.save();
                                    }
                                }
                            }
                        } else {
                            bar_begin = closestX(coords[0]);
                            bar_end = bar_begin;
                        }
                    }
                    Focus.restore();
                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    if (!edit_mode) {
                        mPosX += dx / mScaleFactorX;
                        mPosY += dy / mScaleFactorY;
                    } else {
                        coords[0] = x;
                        coords[1] = y;
                        mInverse.mapPoints(coords);
                        bar_end = closestX(coords[0]);
                    }
                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                if (0 <= bar_begin && bar_begin < bar_end + 1) {
                    Score.setTrackSelected(insertion_track);
                    final Track track = Score.getTrackSelected();
                    final int start = bar_begin * Score.getResolution();
                    final int finish = (bar_end + 1) * Score.getResolution();

                    if (nonOverlapping(track, start, finish)) {
                        if (CSD.getNbInstruments() > 0) {
                            track.createPattern();
                            Track.setPatternSelected(track.getNbOfPatterns());
                            final Pattern pattern = Track.getPatternSelected();
                            pattern.start = start;
                            pattern.finish = finish;
                        } else {
                            final Toast toast = Toast.makeText(context,
                                    "Please, create an instrument first", Toast.LENGTH_LONG);
                            toast.show();

                        }
                    }
                    bar_begin = -1;
                    invalidate();
                }
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex =
                        (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }
        return true;
    }

    private void drawPatch(int p, Canvas canvas, int t) {
        float x0, x1, y0, y1;
        Shader current;

        for (int i = 0; i < 16; i++) {
            x0 = (p * width + i * width / 16) * Score.getResolution() / 32;
            y0 = (t - 1) * track_height * height;
            x1 = x0;
            y1 = t * track_height * height;
            current = new LinearGradient(x0, y0, x1, y1
                    , new int[]{
                    Color.parseColor("#7F7F7F")
                    , Default.grays[10 * order[i]]
                    , Color.parseColor("#7F7F7F")}
                    , null
                    , Shader.TileMode.CLAMP);
            paint.setShader(current);
            paint.setAlpha(255);
            canvas.drawLine(x1, y1, x0, y0, paint);
            if (t == 1) {
                paint.setShader(null);
                setStrokeWhite();
                canvas.drawText("" + (16 * p + i + 1)
                        , (p * width + i * width / 16 + width / 32) * Score.getResolution() / 32, coords[1]
                        , paint);
            }
        }
        paint.setShader(null);
    }
}
