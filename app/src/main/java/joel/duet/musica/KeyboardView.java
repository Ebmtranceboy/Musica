package joel.duet.musica;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
//import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by joel on 10/01/16 at 16:13 at 16:16 at 00:29 at 22:36 at 10:46.
 */

public final class KeyboardView extends View {
    private int keyboard_width;
    private int keyboard_height;

    //private static final String TAG = "KeyboardView";

    private static final Paint mPainterStroke = new Paint();
    private static final Paint mPainterFill = new Paint();
    private static final Integer whiteKeys[] = {0, 2, 4, 5, 7, 9, 11};
    private static final Integer blackKeys[] = {1, 3, 6, 8, 10};
    private static final List<Integer> listBlackKeys = Arrays.asList(blackKeys);
    private static final int nbkeys = 24;
    private static final double nbwhite = nbkeys / 12.0 * 7.0;
    private int whitewidth;
    private int blackwidth2;
    private int blackheight;
    private static final boolean[] pressed = new boolean[nbkeys];

    public KeyboardView(Context context) {
        super(context);
        init();
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                mPainterStroke.setStrokeWidth(1);
                mPainterStroke.setStyle(Paint.Style.STROKE);

                mPainterFill.setStyle(Paint.Style.FILL);

                keyboard_width = getWidth();
                keyboard_height = getHeight();

                whitewidth = (int) ((keyboard_width-1) / nbwhite);
                blackwidth2 = (int) (whitewidth * 0.6 * 0.5);
                blackheight = (int) (0.6 * keyboard_height);

                draw(-1, -1, false);
                show();
            }
        });
    }

    private int key(float x, float y) {
        int pos = (int) (x % whitewidth);
        boolean top = false;
        if (pos > whitewidth / 2) {
            pos -= whitewidth;
            top = true;
        }
        int nth = (int) (x - pos) / whitewidth;
        int k;
        if (pos > -blackwidth2 && pos < blackwidth2 && y < blackheight && nth % 7 != 3 && nth % 7 != 0)
            k = (nth % 7 < 4 ? 2 * nth % 7 - 1 : 2 * (nth % 7 - 1));
        else if (top) k = whiteKeys[(nth + 6) % 7];
        else k = whiteKeys[nth % 7];

        return k + 12 * (nth / 7 - (nth % 7 == 0 && top ? 1 : 0)); // KEEP PARENS!!
    }

    public int draw(float x, float y, boolean isPressed) {
        int k = -1;
        if (x >= 0 && y >= 0) {
            k = key(x, y);
            if(k>=0 && k < nbkeys) pressed[k] = isPressed;
        }

        return k;
    }

    public void show() {
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int b = 0;
        int w = 0;
        while (b < nbkeys) {
            if (!listBlackKeys.contains(b % 12)) {
                if (pressed[b]) {
                    mPainterFill.setColor(Color.LTGRAY);
                    canvas.drawRect(w * whitewidth, 0, (w + 1) * whitewidth, keyboard_height - 1, mPainterFill);
                    mPainterStroke.setColor(Color.LTGRAY);
                    canvas.drawRect(w * whitewidth, 0, (w + 1) * whitewidth, keyboard_height - 1, mPainterStroke);
                } else {
                    mPainterFill.setColor(Color.WHITE);
                    canvas.drawRect(w * whitewidth, 0, (w + 1) * whitewidth, keyboard_height - 1, mPainterFill);
                    mPainterStroke.setColor(Color.BLACK);
                    canvas.drawRect(w * whitewidth, 0, (w + 1) * whitewidth, keyboard_height - 1, mPainterStroke);
                }
                w++;
            }
            b++;
        }

        b = 0;
        w = 0;
        while (b < nbkeys) {
            if (!listBlackKeys.contains(b % 12)) {
                w++;
                if (listBlackKeys.contains((b + 1) % 12))
                    if (pressed[b + 1]) {
                        mPainterFill.setColor(Color.LTGRAY);
                        canvas.drawRect(w * whitewidth - blackwidth2, 0, w * whitewidth + blackwidth2, blackheight, mPainterFill);
                        mPainterStroke.setColor(Color.LTGRAY);
                        canvas.drawRect(w * whitewidth - blackwidth2, 0, w * whitewidth + blackwidth2, blackheight, mPainterStroke);
                    } else {
                        mPainterFill.setColor(Color.BLACK);
                        canvas.drawRect(w * whitewidth - blackwidth2, 0, w * whitewidth + blackwidth2, blackheight, mPainterFill);
                        mPainterStroke.setColor(Color.BLACK);
                        canvas.drawRect(w * whitewidth - blackwidth2, 0, w * whitewidth + blackwidth2, blackheight, mPainterStroke);
                    }
            }
            b++;
        }
        canvas.restore();
    }
}