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
                radius = Math.min(getWidth(),getHeight()*2/3)/11;
                rs32 = radius * (float) Math.sqrt(3.0) / 2.0f;
            }
        });
    }

    private static float dist(float x0,float y0,float x1,float y1){
		return 3*(x1-x0)*(x1-x0)+(y1-y0)*(y1-y0);
	}

    private static int hexIndex(int m, int n){
        if(m==9 && n==3) return 0;
        int x=(m+3*n)/2-9, y=6-(m+n)/2,z=3-n; // m=9-3y-x, n=3-z
        int segment, corol,position;
        if(x>0){
            if(y>z){
                segment = 3;
                corol = -z;
                position = 2*corol - x;
            } else {
                segment = 2;
                corol = -y;
                position = x;
            }
        } else {
            if(y>z){
                segment = 4;
                corol = y;
                position = -x;
            } else {
                segment = 1;
                corol = z;
                position = 2*corol + x;
            }
        }

        //Log.i(TAG,"("+m+","+n+"):"+"["+x+","+y+","+z+"]");
        //Log.i(TAG,"("+m+","+n+"):"+"{"+segment+","+corol+","+position+"}");
        //Log.i(TAG,"("+m+","+n+")->"+((2*corol-1)*(2*corol-1)+2*(segment-1)*corol+position));
        return (2*corol-1)*(2*corol-1)+2*(segment-1)*corol+position;
    }

    private static Pair<Float,Float> indexToHex(int index){
        int corol = (int)Math.floor((Math.sqrt(index)+1)/2);
        if (corol==0) return new Pair<>((9)*rs32+rs32/1.5f,(3)*1.5f * radius+radius/1.5f);
        int segment = 1+(int)Math.floor((index-(2*corol-1)*(2*corol-1))/(2*corol));
        int position = index-(2*corol-1)*(2*corol-1)-2*corol*(segment-1);
        int x,y,z;
        switch (segment){
            case 1:z=corol;x=position-2*z;y=-x-z;break;
            case 2:x=position;y=-corol;z=-x-y;break;
            case 3:z=-corol;x=-position-2*z;y=-x-z;break;
            case 4:
                default:x=-position;y=corol;z=-x-y;break;
        }

        //Log.i(TAG,""+index+":{"+segment+","+corol+","+position+"}");
        //Log.i(TAG,""+index+":["+x+","+y+","+z+"]");
        //Log.i(TAG,""+index+":("+(z-y+6)+","+(6-x)+")");
        return new Pair<>((9-3*y-x)*rs32+rs32/1.5f,(3-z)*1.5f * radius+radius/1.5f);
    }

    public int whatis(float x, float y) {
        float m = (x - rs32/1.5f) / rs32;
        float n = (y - radius / 1.5f) / (1.5f * radius);
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
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 9; j++)
                fork((2 * i + j % 2) * rs32, radius * j * 1.5f, canvas);
        }

        Pair<Float,Float> p;
        boolean startMinorReached = false;
        int j=0;
        for(int i=0;i<Default.flavors.length;i++) {
            if(OptionsFragment.isMajor){
                if(Default.flavors[i].isMajor){
                    p = indexToHex(i);
                    if(i == indexColor)
                        mPainter.setColor(ContextCompat.getColor(getContext(),R.color.colorAccent));
                    else mPainter.setColor(Color.LTGRAY);
                    mPainter.setTextSize(scale);
                    canvas.drawText(Default.flavors[i].notation, p.first, p.second, mPainter);
                }
            } else {
                if (!Default.flavors[i].isMajor) {
                    if(!startMinorReached){
                        startMinorReached = true;
                        j=i;
                    }
                    p = indexToHex(i-j);
                    if (i-j == indexColor)
                        mPainter.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
                    else mPainter.setColor(Color.LTGRAY);
                    mPainter.setTextSize(scale);
                    canvas.drawText(Default.flavors[i].notation, p.first, p.second, mPainter);
                }
            }
        }
        canvas.restore();
    }
}
