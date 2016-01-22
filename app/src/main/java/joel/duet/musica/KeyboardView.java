package joel.duet.musica;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 *
 * Created by joel on 10/01/16 at 16:13 at 16:16 at 00:29 at 22:36.
 */

public final class KeyboardView extends View {
    private Bitmap keyboard_bitmap;
    private int keyboard_width;
    private  int keyboard_height;

    private static final String TAG = "KeyboardView";

    private static final Paint mPainter = new Paint();
    private static final Integer whiteKeys[] = {0, 2, 4, 5, 7, 9, 11};
    private static final Integer blackKeys[] = {1, 3, 6, 8, 10};
    private static final List<Integer> listBlackKeys = Arrays.asList(blackKeys);
    private int piano[];
    //private static final int BLACK_TRANSPARENT = (0);
    //private static final int GRAY_OPAQUE = (-8421505);
    //private static final int DARKGRAY_OPAQUE = (-12632257);
    private static final int BLACK_OPAQUE = (-16777216);
    private static final int WHITE_OPAQUE = (-1);
    private static final int LIGHTGRAY_OPAQUE = (-4210753);
    private static final double nbkeys = 24.0;
    private static final double nbwhite = nbkeys / 12.0 * 7.0;
    private  int whitewidth;
    private int blackwidth2;
    private  int blackheight;

    private void drawWhiteStroke(int[] graph){
        int b;
        for (b = 0; b < nbwhite; b++)
            for (int i = 0; i < keyboard_height; i++)
                if(graph[i * keyboard_width + b * whitewidth]!=LIGHTGRAY_OPAQUE)
                    graph[i * keyboard_width + b * whitewidth] = BLACK_OPAQUE;
    }

    private void fillKeyboardDrawing(int[] graph) {
        //for (int i = 0; i < keyboard_width * keyboard_height; i++) graph[i] = WHITE;
        for (int j = 0; j < keyboard_width; j++)
            graph[(keyboard_height - 1) * keyboard_width + j] = BLACK_OPAQUE;

        drawWhiteStroke(graph);

        int b = 0;
        int w = 0;
        while (b < nbkeys) {
            if (!listBlackKeys.contains(b % 12)) {
                w++;
                if (listBlackKeys.contains((b + 1) % 12))
                    for (int i = 0; i < blackheight; i++)
                        for (int j = w * whitewidth - blackwidth2; j < w * whitewidth + blackwidth2; j++)
                            graph[i * keyboard_width + j] = BLACK_OPAQUE;
            }
            b++;
        }
    }

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
                keyboard_width = getWidth();
                keyboard_height = getHeight();

                whitewidth = (int) (keyboard_width / nbwhite);
                blackwidth2 = (int) (whitewidth * 0.6 * 0.5);
                blackheight = (int) (0.6 * keyboard_height);

                Log.i(TAG, "" + keyboard_width);
                Log.i(TAG, "" + keyboard_height);
                piano = new int[keyboard_width * keyboard_height];
                keyboard_bitmap =
                        Bitmap.createBitmap(keyboard_width, keyboard_height, Bitmap.Config.ARGB_8888);
                fillKeyboardDrawing(piano);
                draw(-1, -1, false);
                show();
            }
        });
    }

    private  int key(float x, float y) {
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

    public  int draw(float x, float y, boolean pressed) {
        int k = -1;
        if(keyboard_bitmap != null)
            if (x < 0 || y < 0) keyboard_bitmap.setPixels(piano, 0,
                keyboard_width, 0, 0, keyboard_width, keyboard_height);
            else {
                k = key(x, y);
                int[] mask = new int[piano.length];
                keyboard_bitmap.getPixels(mask, 0,
                        keyboard_width, 0, 0, keyboard_width, keyboard_height);
                int jmin = Math.max(0, (int) (x - whitewidth));
                int jmax = Math.min(keyboard_width, (int) (x + whitewidth));
                for (int j = jmin ; j < jmax; j++)
                    for (int i = 0; i < keyboard_height-1; i++)
                        if (key(j, i) == k)
                            if(pressed)
                                mask[i * keyboard_width + j] = LIGHTGRAY_OPAQUE;
                            else if(Arrays.asList(whiteKeys).contains(k%12))
                                mask[i * keyboard_width + j] = WHITE_OPAQUE;
                                    else mask[i * keyboard_width + j] = BLACK_OPAQUE;

                drawWhiteStroke(mask);
                keyboard_bitmap.setPixels(mask, 0,
                        keyboard_width, 0, 0, keyboard_width, keyboard_height);
            }
        return k;
    }

    public void show() {
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if(keyboard_bitmap != null) canvas.drawBitmap(keyboard_bitmap, 0, 0, mPainter);
        canvas.restore();
    }
}