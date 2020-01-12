package org.georunner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.IOrientationProvider;

public class myCompassOverlay extends CompassOverlay {

    private boolean mInCenter = false;

    private float mCompassCenterX = 35.0f;
    private float mCompassCenterY = 35.0f;

    private final Matrix mCompassMatrix = new Matrix();
    private Paint sSmoothPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    public myCompassOverlay(Context context, IOrientationProvider orientationProvider,
                            MapView mapView) {
        super(context, orientationProvider, mapView);

    };



    @Override
    protected void drawCompass(final Canvas canvas, final float bearing, final Rect screenRect) {
        final Projection proj = mMapView.getProjection();

        float centerX;
        float centerY;
        if (mInCenter) {
            final Rect rect = proj.getScreenRect();
            centerX = rect.exactCenterX();
            centerY = rect.exactCenterY();
        } else {
            centerX = mCompassCenterX * mScale;
            centerY = mCompassCenterY * mScale;
        }

        mCompassMatrix.setTranslate(-mCompassFrameCenterX, -mCompassFrameCenterY);
        mCompassMatrix.postTranslate(centerX, centerY);

        proj.save(canvas, false, true);
        canvas.concat(mCompassMatrix);
        canvas.drawBitmap(mCompassFrameBitmap, 0, 0, sSmoothPaint);
        proj.restore(canvas, true);

        mCompassMatrix.setRotate(-bearing, mCompassRoseCenterX, mCompassRoseCenterY);
        mCompassMatrix.postTranslate(-mCompassRoseCenterX, -mCompassRoseCenterY);
        mCompassMatrix.postTranslate(centerX, centerY);

        proj.save(canvas, false, true);
        canvas.concat(mCompassMatrix);
        canvas.drawBitmap(mCompassRoseBitmap, 0, 0, sSmoothPaint);
        proj.restore(canvas, true);
    }
}
