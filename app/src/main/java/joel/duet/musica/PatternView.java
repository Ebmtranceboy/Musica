package joel.duet.musica;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Matrix;

/**
 *
 * Created by joel on 22/01/16 at 23:26 at 23:28.
 */
public final class PatternView extends View {
    private static final Paint paint = new Paint();
    private static final float scale_height = 10.0f;
    private static final float line_height = scale_height / (float) Default.max_midi_note;
    private static final float[] coords = new float[2];
    private static final Matrix mMatrix = new Matrix();
    private static final Matrix mInverse = new Matrix();

    private static Matrix mTemp;

    private static int bar_begin = -1;
    private static int bar_end;
    private static int insertion_line;
    private static float mLastTouchX, mLastTouchY;
    private static int mActivePointerId;
    private static int width;
    private static float width_per_tick;
    private static int height;
    private static int number_ticks;
    private ScaleGestureDetector mScaleDetector;

    public static boolean edit_mode;

    // Absolutely NON static
    public final Pattern pattern = Track.getPatternSelected();

    public PatternView(Context context) {
		super(context);
		init(context);
	}

	public PatternView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PatternView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

    public void init(Context ctx) {
        //paint.setAntiAlias(true);
        mScaleDetector = new ScaleGestureDetector(ctx, new ScaleListener());
    }

    private static void drawBackground(Canvas canvas) {
        PatternView.paint.setStyle(Paint.Style.FILL);
        PatternView.paint.setColor(Color.parseColor("#7F7F7F"));
        canvas.drawPaint(PatternView.paint);
    }

    private static void setStrokeWhite() {
        PatternView.paint.setStyle(Paint.Style.STROKE);
        PatternView.paint.setColor(Color.parseColor("#FFFFFF"));
    }

    private static void setStrokeBlack() {
        PatternView.paint.setStyle(Paint.Style.STROKE);
        PatternView.paint.setColor(Color.parseColor("#000000"));
    }

    private static void setFillAlpha(int alpha) {
        PatternView.paint.setStyle(Paint.Style.FILL);
        PatternView.paint.setARGB(alpha, 255, 255, 255);
    }

    private static void setFillGrey() {
        PatternView.paint.setStyle(Paint.Style.FILL);
        PatternView.paint.setColor(Color.parseColor("#DFDFDF"));
    }

    private static void drawReset(Canvas canvas) {
        drawBackground(canvas);
        setStrokeWhite();
    }

    private int getResolution() {
        return Default.resolutions[pattern.resolution];
    }

    private static boolean bounded(){
        return 0<=coords[0] && coords[0]<=width && 0<=coords[1] && coords[1]<=height*scale_height;
    }
    private boolean isBounded(){
        mTemp = new Matrix(mMatrix);
        mTemp.setTranslate(pattern.mPosX, pattern.mPosY);
        mTemp.postScale(pattern.mScaleFactorX, pattern.mScaleFactorY
                , super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        mTemp.invert(mInverse);

        coords[0] = 0;coords[1] = 0;
        mInverse.mapPoints(coords);
        if (!bounded()) return false;
        coords[0] = 0;coords[1] = height * scale_height;
        mInverse.mapPoints(coords);
        if (!bounded()) return false;
        coords[0] = width;coords[1] = height * scale_height;
        mInverse.mapPoints(coords);
        if (!bounded()) return false;
        coords[0] = width;coords[1] = 0;
        mInverse.mapPoints(coords);
        return bounded();
    }

    private class ScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (Math.abs(detector.getCurrentSpanX()) < Math.abs(detector.getCurrentSpanY()))
                pattern.mScaleFactorY *= detector.getScaleFactor();
            else if (Math.abs(detector.getCurrentSpanY()) < Math.abs(detector.getCurrentSpanX()))
                pattern.mScaleFactorX *= detector.getScaleFactor();
            else {
                pattern.mScaleFactorY *= detector.getScaleFactor();
                pattern.mScaleFactorX *= detector.getScaleFactor();
            }

            // Don't let the object get too small or too large.
            pattern.mScaleFactorX = Math.max(0.1f, Math.min(pattern.mScaleFactorX, 5.0f));
            pattern.mScaleFactorY = Math.max(0.1f, Math.min(pattern.mScaleFactorY, 5.0f));


/*
            float sx = pattern.mScaleFactorX;
            float sy = pattern.mScaleFactorY;

            if (Math.abs(detector.getCurrentSpanX()) < Math.abs(detector.getCurrentSpanY()))
                sy *= detector.getScaleFactor();
            else if (Math.abs(detector.getCurrentSpanY()) < Math.abs(detector.getCurrentSpanX()))
                sx *= detector.getScaleFactor();
            else {
                sy *= detector.getScaleFactor();
                sx *= detector.getScaleFactor();
            }

            // Don't let the object get too small or too large.
            sx = Math.max(0.1f, Math.min(sx, 5.0f));
            sy = Math.max(0.1f, Math.min(sy, 5.0f));


            if(!isBounded()){
                pattern.mScaleFactorX = sx;
                pattern.mScaleFactorY = sy;
            }
            */

            invalidate();
            return true;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        number_ticks = pattern.finish - pattern.start;

        width = getWidth();
        width_per_tick = width / (float)number_ticks;
        height = getHeight();
        drawReset(canvas);

        canvas.save();

        mMatrix.setTranslate(pattern.mPosX, pattern.mPosY);
        mMatrix.postScale(pattern.mScaleFactorX, pattern.mScaleFactorY
                , super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        mMatrix.invert(mInverse);
        coords[0] = 0;
        coords[1] = 13;
        mInverse.mapPoints(coords);

        canvas.concat(mMatrix);

        for (int m = 1; m <= Default.max_midi_note; m++) {
            setStrokeWhite();
            canvas.drawText("" + (Default.max_midi_note - m),
                    coords[0],
                    (m - 0.4f) * line_height * height,
                    paint);
            for (int i = 1; i <= Math.round((double)number_ticks / getResolution()); i++) {
                drawBar(i, canvas, m);
            }
        }

        for (int n = 1; n <= pattern.getNbOfNotes(); n++) {
            Pattern.setNoteSelected(n);
            final Pattern.Note note = Pattern.getNoteSelected();
            setStrokeBlack();
            canvas.drawRect(note.onset * width_per_tick
                    , (Default.max_midi_note - note.pitch - 1) * height * line_height
                    , (note.onset + note.duration) * width_per_tick
                    , (Default.max_midi_note - note.pitch) * height * line_height, paint);
            //setFillAlpha(192);
            setFillGrey();
            canvas.drawRect(note.onset * width_per_tick
                    , (Default.max_midi_note - note.pitch - 1) * height * line_height
                    , (note.onset + note.duration) * width_per_tick
                    , (Default.max_midi_note - note.pitch) * height * line_height, paint);
        }

        if (bar_begin >= 0) {
            setFillAlpha(64);
            canvas.drawRect(bar_begin * getResolution() * width_per_tick
                    , (insertion_line - 1) * height * line_height
                    , (bar_end + 1) * getResolution() *width_per_tick
                    , insertion_line * height * line_height, paint);
        }

        canvas.restore();
    }

    private int closestX(float x0) {
        float d, dist = Float.MAX_VALUE;
        float x;
        int p, c = 0;
        for (p = 1; p <= number_ticks / getResolution(); p++) {
            x = (p - 0.5f) * getResolution() * width_per_tick;
            d = Math.abs(x - x0);
            if (d < dist) {
                dist = d;
                c = p - 1;
            }
        }
        return c;
    }

    private static int closestY(float y0) {
        float d, dist = Float.MAX_VALUE;
        float y;
        int m, c = 0;
        for (m = 1; m <= Default.max_midi_note; m++) {
            y = (m - 0.5f) * line_height * height;
            d = Math.abs(y - y0);
            if (d < dist) {
                dist = d;
                c = m;
            }
        }
        return c;
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
                Pattern.Note note = null;

                coords[0] = x;
                coords[1] = y;
                mInverse.mapPoints(coords);

                if (edit_mode) {
                    insertion_line = closestY(coords[1]);
                    boolean existing_note = false;
                    int n = 1;
                    while (n <= pattern.getNbOfNotes() && !existing_note) {
                        Pattern.setNoteSelected(n);
                        note = Pattern.getNoteSelected();
                        existing_note =
                                note.pitch == Default.max_midi_note - insertion_line
                                        && note.onset * width / number_ticks <= coords[0]
                                        && coords[0] <= (note.onset + note.duration) * width_per_tick;
                        n++;
                    }

                    if (!existing_note) {
                        bar_begin = closestX(coords[0]);
                        bar_end = bar_begin;
                        invalidate();
                    } else {
                        pattern.deleteNote(note);
                        invalidate();
                    }
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
                        //final float tx = pattern.mPosX;
                        //final float ty = pattern.mPosY;
                        pattern.mPosX += dx;
                        pattern.mPosY += dy;
                        /*if(!isBounded()){
                            pattern.mPosX = tx;
                            pattern.mPosY = ty;
                        }*/
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
                    final int start = bar_begin * getResolution();
                    final int finish = (bar_end + 1) * getResolution();
                    final int pitch = Default.max_midi_note - insertion_line;
                    int n = 1;
                    boolean non_overlap = true;
                    while (n <= pattern.getNbOfNotes() && non_overlap) {
                        Pattern.setNoteSelected(n);
                        Pattern.Note note = Pattern.getNoteSelected();
                        non_overlap = note.pitch != pitch
                                || (note.onset <= start && note.onset + note.duration <= start)
                                || (note.onset >= finish && note.onset + note.duration >= finish);
                        n++;
                    }
                    if (non_overlap) {
                        pattern.createNote(start, finish - start, Default.max_midi_note - insertion_line);
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

    private void drawBar(int p, Canvas canvas, int m) {
        switch (m % 12) {
            case 1:
            case 3:
            case 6:
            case 8:
            case 10:
                paint.setARGB(255, 9, 9, 9);
                break;
            default:
                paint.setARGB(255, 255, 255, 255);
        }
        final float margin = 1;
        canvas.drawRect((p - 1) * getResolution() * width_per_tick + margin
                , (m - 1) * height * line_height + margin
                , p * getResolution() * width_per_tick - margin
                , m * height * line_height - margin, paint);
        if (m == 1) {
            setStrokeWhite();
            canvas.drawText("" + p
                    , (p - 0.7f) * getResolution() * width_per_tick, coords[1]
                    , paint);
        }
    }
}
