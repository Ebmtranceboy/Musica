package joel.duet.musica;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
//import android.util.Log;
import android.util.Pair;
import android.view.View;

/**
 *
 * Created by joel on 11/03/16 at 01:50 at 01:54 at 02:16 at 09:36.
 */
public class ChordsView extends View {
    private final Paint mPainter = new Paint();
    private int indexColor;
    //private static final String TAG = "ChordsView";

    private static float radius; // hexagon radius in pixels
    private static float rs32;
    private static int width_count,height_count;
    private static int m0,n0,x0,y0,z0;
    private final static float scale = 16.0f; // font size

    public ChordsView(Context context) {
        super(context);
        init();
    }

    public ChordsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChordsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {

                int nchords = OptionsFragment.isMajor ? Default.Flavor.nbMajor :
                        Default.flavors.length - Default.Flavor.nbMajor;
                float width = getWidth() * 0.9f;
                width_count = (int) Math.ceil(Math.sqrt(1+nchords*width/getHeight()));
                height_count = (int) Math.ceil(Math.sqrt(1+nchords*getHeight()/width));
                //Log.i(TAG,""+width+" "+height);
                radius = Math.min(width/width_count/(float)Math.sqrt(3.0)*2,getHeight()/(1+3*height_count/2));
                rs32 = radius * (float) Math.sqrt(3.0) / 2.0f;
                m0=0;
                n0=height_count;            // center in cartesian coordinates
                if(n0%2==1) n0--;
                x0=-(m0+3*n0)/2; // upper left corner in center coordinates
                y0=(m0+n0)/2;
                z0=n0;
            }
        });
    }

    private static float dist(float x0,float y0,float x1,float y1){
		return 3*(x1-x0)*(x1-x0)+(y1-y0)*(y1-y0);
	}

    private static int hexIndex(int m, int n){

        if(m==m0 && n==n0) return 0;
        int x = (m+3*n)/2+x0, z = z0-n; // y = y0-(m+n)/2, so that m=x0-x+3(y0-y), n=z0-z
        int rest = x + (z%2==0?3*z/2:3*(z-1)/2+1);

        //Log.i(TAG,"("+m+","+n+"):"+"["+x+","+y+","+z+"]");
        //Log.i(TAG,"("+m+","+n+"):"+(z*width+rest));
        return z*width_count + rest;
    }

    private static Pair<Float,Float> indexToHex(int index){
        int rest = index % width_count;
        int z = (index-rest) / width_count;
        int x = rest - (z%2==0?3*z/2:3*(z-1)/2+1);
        int y = -z - x;

        //Log.i(TAG,""+index+":["+x+","+y+","+z+"]");
        int m=x0-x+3*(y0-y), n=(z0-z);

        return new Pair<>(m*rs32+rs32/3.5f,n*1.5f * radius+radius/3.5f);
    }

    public int whatis(float x, float y) {
        float m = (x - rs32/1.5f) / rs32;
        if (m < 0) m = 0;
        float n = (y - radius / 1.5f) / (1.5f * radius);
        if (n < 0) n = 0;
        int m0 = (int) m;
        int m1 = m0 + 1;
        int n0 = (int) n;
        int n1 = n0 + 1;
        if ((m0 + n0) % 2 == 0)
            if (dist(m, n, (float) m0, (float) n0) < dist(m, n, (float) m1, (float) n1))
                return hexIndex(m0, n0);
            else return hexIndex(m1, n1);
        else if (dist(m, n, (float) m0, (float) n1) < dist(m, n, (float) m1, (float) n0))
            return hexIndex(m0, n1);
        else return hexIndex(m1, n0);
    }

    private int abscisa(double y, double x0, double y0, double x1, double y1) {
        return (int) ((y - y0) * (x1 - x0) / (y1 - y0) + x0);
    }

    private void fork(float x0, float y0, Canvas canvas) {
        float x = x0 + rs32 ;
        float y = y0 - radius / 2;
        canvas.drawLine(abscisa(y, x, y, x0, y0), y, abscisa(y0, x, y, x0, y0), y0, mPainter);
        canvas.drawLine(x0, y0, x0, y0 + radius, mPainter);
        x = x0 - rs32;
        canvas.drawLine(abscisa(y, x, y, x0, y0), y, abscisa(y0, x, y, x0, y0), y0, mPainter);
    }

    public void show(int color) {
        indexColor = color;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        mPainter.setColor(Color.BLACK);
        for (int i = 0; i < width_count + 1; i++) {
            for (int j = 0; j < height_count + 2; j++)
                fork((2 * i + j % 2) * rs32 - rs32/3.5f, radius * j * 1.5f - radius/3.5f, canvas);
        }

        Pair<Float,Float> p;
        mPainter.setTextSize(scale);
        for(int i=0; i<Default.flavors.length; i++) {
            if(OptionsFragment.isMajor){
                if(Default.flavors[i].isMajor){
                    p = indexToHex(i);
                    if(i == indexColor)
                        mPainter.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
                    else mPainter.setColor(Color.LTGRAY);
                    canvas.drawText(Default.flavors[i].notation, p.first, p.second, mPainter);
                }
            } else {
                if (!Default.flavors[i].isMajor) {
                    p = indexToHex(i-Default.Flavor.nbMajor);
                    if (i-Default.Flavor.nbMajor == indexColor)
                        mPainter.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    else mPainter.setColor(Color.LTGRAY);
                    canvas.drawText(Default.flavors[i].notation, p.first, p.second, mPainter);
                }
            }
        }
        canvas.restore();
    }
}
