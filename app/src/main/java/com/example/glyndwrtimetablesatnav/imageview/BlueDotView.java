package com.example.glyndwrtimetablesatnav.imageview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.example.glyndwrtimetablesatnav.R;

/**
 * Extends great ImageView library by Dave Morrissey. See more:
 * https://github.com/davemorrissey/subsampling-scale-image-view.
 */
public class BlueDotView extends SubsamplingScaleImageView
{
    private float uncertaintyRadius = 1.0f;
    private float dotRadius = 1.0f;
    private PointF dotCenter = null;
    private double heading = -1.0;
    private SmoothEstimate smoothEstimate = new SmoothEstimate();
    Paint paint = new Paint();

    public void setUncertaintyRadius(float uncertaintyRadius)
    {
        this.uncertaintyRadius = uncertaintyRadius;
        //  Radius of uncertainty circle for location
    }   //  public void setUncertaintyRadius(float uncertaintyRadius)

    public void setDotRadius(float dotRadius)
    {
        this.dotRadius = dotRadius;
        //  Radius of user dot
    }   //  public void setDotRadius(float dotRadius)

    public void setDotCenter(PointF dotCenter)
    {
        this.dotCenter = dotCenter;
        //  Centre point of dot
    }   //  public void setDotCenter(PointF dotCenter)

    public void setHeading(double heading)
    {
        this.heading = heading;
        //  Heading set
    }   //  public void setHeading(double heading)

    public BlueDotView(Context context)
    {
        this(context, null);
    }   //  public BlueDotView(Context context)

    public BlueDotView(Context context, AttributeSet attr)
    {
        super(context, attr);
        initialise();
    }   //  public BlueDotView(Context context, AttributeSet attr)

    @SuppressLint("LogNotTimber")
    private void initialise()
    {
        Log.d("BluedotView", "Initialize");
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(getResources().getColor(R.color.ia_blue));
    }   //  private void initialise()

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (!isReady())
        {
            return;
        }   //  if (!isReady())
        if (dotCenter != null)
        {
            // Update smooth estimate - set degrees to radians
            smoothEstimate.update(dotCenter.x, dotCenter.y, (float)((heading)/180.0 * Math.PI), uncertaintyRadius, System.currentTimeMillis());
            PointF vPoint = sourceToViewCoord(smoothEstimate.getX(), smoothEstimate.getY());
            // Paint uncertainty circle
            float scaledUncertaintyRadius = getScale() * smoothEstimate.getRadius();
            paint.setAlpha(30);
            canvas.drawCircle(vPoint.x, vPoint.y, scaledUncertaintyRadius, paint);
            // Paint center point
            float scaledDotRadius = getScale() * dotRadius;
            paint.setAlpha(90);
            canvas.drawCircle(vPoint.x, vPoint.y, scaledDotRadius, paint);
            // Paint heading triangle if available
            if (heading != -1.0)
            {
                paint.setAlpha(255);
                Path triangle = headingTriangle(vPoint.x, vPoint.y,
                        // Note: Rotate up-pointing angle to right (for unit circle)
                        smoothEstimate.getHeading() - (float)Math.PI/2,
                        scaledDotRadius);
                canvas.drawPath(triangle, paint);
            }   //  if (heading != -1.0)
        }   //  if (dotCenter != null)
        postInvalidate();
    }   //  protected void onDraw(Canvas canvas)

    /**
     * Trigonometric (unit circle) computation of the heading arrow triangle
     * @param x X coordinate of the estimate (circle) center
     * @param y Y coordinate of the estimate (circle) center
     * @param a Heading angle in radians (zero pointing right)
     * @param r Radius of the estimate circle
     * @return Path representing the heading triangle
     */
    private static Path headingTriangle(float x, float y, float a, float r)
    {
        float x1 = (float)(x + 0.9*r*Math.cos(a));
        float y1 = (float)(y + 0.9*r*Math.sin(a));
        float x2 = (float)(x + 0.2*r*Math.cos(a + 0.5*Math.PI));
        float y2 = (float)(y + 0.2*r*Math.sin(a + 0.5*Math.PI));
        float x3 = (float)(x + 0.2*r*Math.cos(a - 0.5*Math.PI));
        float y3 = (float)(y + 0.2*r*Math.sin(a - 0.5*Math.PI));

        Path triangle = new Path();
        triangle.moveTo(x1, y1);
        triangle.lineTo(x2, y2);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x1, y1);

        return triangle;
    }   //  private static Path headingTriangle(float x, float y, float a, float r)
}   //  public class BlueDotView extends SubsamplingScaleImageView
