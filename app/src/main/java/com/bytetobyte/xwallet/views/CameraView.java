package com.bytetobyte.xwallet.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by bruno on 24.04.17.
 */
public class CameraView extends SurfaceView {
    private Paint mTransparentPaint;
    private Paint mSemiBlackPaint;
    private Path mPath = new Path();

    public CameraView(Context context) {
        super(context);
        initPaints();
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints();
    }

    private void initPaints() {
        mTransparentPaint = new Paint();
        mTransparentPaint.setColor(Color.TRANSPARENT);
        mTransparentPaint.setStrokeWidth(10);

        mSemiBlackPaint = new Paint();
        mSemiBlackPaint.setColor(Color.TRANSPARENT);
        mSemiBlackPaint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas == null) return;

        mPath.reset();

        mPath.addCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 550, Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 550, mTransparentPaint);

        canvas.drawPath(mPath, mSemiBlackPaint);
        canvas.clipPath(mPath);
        canvas.drawColor(Color.parseColor("#A6000000"));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (canvas == null) return;

        mPath.reset();

        mPath.addCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 550, Path.Direction.CW);
        mPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, 550, mTransparentPaint);

        canvas.drawPath(mPath, mSemiBlackPaint);
        canvas.clipPath(mPath);
        canvas.drawColor(Color.parseColor("#A6000000"));
    }
}
