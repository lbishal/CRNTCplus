package de.uni_passau.fim.esl.crn_toolbox_center.view;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

/**
 * An Android view element representing a two-dimensional graph chart.
 * Coordinates are added to a trace and displayed as points of lines in the
 * graph. There are multiple traces supported. Contains auto-scroll and removes
 * old coordinates.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 * 
 */
public class Chart2DView extends View {

    private static final int AXIS_COLOR = Color.GRAY;
    private static final float AXIS_WIDTH = 1;
    private static final int[] TRACE_COLORS = { Color.RED, Color.BLUE,
            Color.argb(255, 50, 155, 50), Color.CYAN, Color.MAGENTA, Color.YELLOW };
    private static final float TRACE_WIDTH = 2;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private boolean mIsInitialized;

    private LinkedList<Coordinate>[] mTraces;
    private double mInitialX = -1;
    private double mOffsetX;
    private double mCurrentMaxValue = 1;
    private boolean mInvalidated;

    /**
     * Creates a new Chart2DView.
     * 
     * @param context
     *            The context.
     */
    public Chart2DView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(TRACE_COLORS[0]);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(TRACE_WIDTH);
        mIsInitialized = false;
    }

    /**
     * Initializes the number of traces. Has to be called before coordinates can
     * be added.
     * 
     * @param traces
     *            The number of traces.
     */
    @SuppressWarnings("unchecked")
    public synchronized void initTraces(int traces) {
        mTraces = new LinkedList[traces];
        for (int i = 0; i < mTraces.length; i++) {
            mTraces[i] = new LinkedList<Coordinate>();
        }
    }

    /**
     * Adds a coordinate to the given trace.
     * 
     * @param trace
     *            The trace number
     * @param coordinate
     *            The coordinate
     */
    public synchronized void addCoordinate(int trace, Coordinate coordinate) {
        if (mTraces != null && coordinate != null && trace < mTraces.length) {
            if (mInitialX < 0) {
                mInitialX = coordinate.getX();
                mOffsetX = mInitialX;
            }
            LinkedList<Coordinate> coordinateList = mTraces[trace];

            Coordinate newCoordinate = new Coordinate(coordinate.getX(), coordinate.getY());
            double absValue = Math.abs(newCoordinate.getY());
            mCurrentMaxValue = Math.max(mCurrentMaxValue, absValue);
            coordinateList.addLast(newCoordinate);
            //if (coordinateList.getLast().getX() - mOffsetX > getWidth() - 1) {
            //	coordinateList.removeFirst();
            	//mOffsetX = coordinateList.getFirst().getX();
            //}
            mInvalidated = false;
            postInvalidate();
        }
    }

    /**
     * Called when the view is first painted.
     */
    private void initGraphics() {

        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);

        mCanvas = new Canvas();
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(Color.WHITE);

        mIsInitialized = true;

    }

    /**
     * Called when this view should be painted.
     */
    @Override
    public synchronized void onDraw(Canvas canvas) {
        if (!mIsInitialized) {
            initGraphics();
        }

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        // draw axis
        int color = mPaint.getColor();
        float strokeWidth = mPaint.getStrokeWidth();
        mPaint.setColor(AXIS_COLOR);
        mPaint.setStrokeWidth(AXIS_WIDTH);
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, mPaint);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(strokeWidth);
        mBitmap.eraseColor(Color.WHITE);

        // draw current traces
        int halfHeight = getHeight() / 2;

        if (mTraces != null)
            for (int i = 0; i < mTraces.length; i++) {

                LinkedList<Coordinate> coordinateList = mTraces[i];
                mPaint.setColor(TRACE_COLORS[i % TRACE_COLORS.length]);

                if (coordinateList != null && coordinateList.size() > 1) {
                    Coordinate prevCoordinate = coordinateList.get(0);
                    // set offset and remove old coordinates
                    
                    if (i == 0) {
                        while (coordinateList.getLast().getX() - mOffsetX > getWidth() - 1) {
                            coordinateList.removeFirst();
                            mOffsetX = coordinateList.getFirst().getX();// +
                                                                        // OFFSET_SPACING;
                        }
                    }
                    
                    for (Coordinate coordinate : coordinateList) {
                        // draw one trace
                        int x1 = (int) (prevCoordinate.getX() - mOffsetX);
                        int y1 = (int) (halfHeight - (prevCoordinate.getY() / mCurrentMaxValue * halfHeight));
                        int x2 = (int) (coordinate.getX() - mOffsetX);
                        int y2 = (int) (halfHeight - (coordinate.getY() / mCurrentMaxValue * halfHeight));
                        mCanvas.drawLine(x1, y1, x2, y2, mPaint);
                        prevCoordinate = coordinate;
                    }

                }
            }
        
        if (!mInvalidated) {
            postInvalidate();
            mInvalidated = true;
        }
        
    }
    public void reset() {
    	mIsInitialized = false;
    }

}
