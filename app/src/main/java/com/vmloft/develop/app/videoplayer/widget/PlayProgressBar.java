package com.vmloft.develop.app.videoplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.utils.VMDimen;

/**
 * Created by chalilayang on 2016/12/13.
 */

public class PlayProgressBar extends View {

    private static final String TAG = "PlayProgressBar";
    public static final int MAX = 10000;

    private float barHeight;
    private Paint barPaint;
    private Paint barShadowPaint;
    private Paint barBufferPaint;

    private int measuredWidth;
    private int measuredHeight;

    private int progress = 0;
    private int secondProgress = 0;

    private Rect barRect;
    private Rect barShadowRect;
    private Rect barBufferRect;

    public PlayProgressBar(Context context) {
        this(context, null);
    }

    public PlayProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(false);
        setWillNotDraw(false);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        barHeight = VMDimen.dp2px(2);

        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(getResources().getColor(R.color.play_progress_bar));

        barShadowPaint = new Paint();
        barShadowPaint.setAntiAlias(true);
        barShadowPaint.setStyle(Paint.Style.FILL);
        barShadowPaint.setColor(getResources().getColor(R.color.vm_white_38));

        barBufferPaint = new Paint();
        barBufferPaint.setAntiAlias(true);
        barBufferPaint.setStyle(Paint.Style.FILL);
        barBufferPaint.setColor(getResources().getColor(R.color.vm_white_54));

        barRect = new Rect();
        barShadowRect = new Rect();
        barBufferRect = new Rect();
    }

    private void setProgressValue(int tmpValue, boolean fromUser) {
        if (tmpValue >= 0 && this.progress != tmpValue && tmpValue <= MAX) {
            this.progress = tmpValue;
            updateProgressBarRect();
            invalidate();
        } else if (tmpValue > MAX && this.progress != tmpValue) {
            this.progress = MAX;
            updateProgressBarRect();
            invalidate();
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setSecondaryProgress(int p) {
        Log.i(TAG, "setSecondaryProgress: " + p);
        secondProgress = p;
        updateBufferProgressBarRect();
        invalidate();
    }

    public void setProgress(int tmpValue) {
        setProgressValue(tmpValue, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBar(canvas);
    }

    private void drawBar(Canvas canvas) {
        canvas.drawRect(barShadowRect, barShadowPaint);
        canvas.drawRect(barBufferRect, barBufferPaint);
        canvas.drawRect(barRect, barPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        measuredWidth = width;
        measuredHeight = (int) barHeight;
        updateBarRect();
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void updateBarRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int top = 0;
            int left = 0;
            int right = measuredWidth;
            int bottom = measuredHeight - top;
            barShadowRect.set(left, top, right, bottom);
            updateProgressBarRect();
            updateBufferProgressBarRect();
        }
    }

    private void updateBufferProgressBarRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int right = (int) (barShadowRect.width() * secondProgress * 1.0 / MAX + barBufferRect.left);
            barBufferRect.set(barShadowRect.left, barShadowRect.top, right, barShadowRect.bottom);
        }
    }

    private void updateProgressBarRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int right = (int) (barShadowRect.width() * progress * 1.0 / MAX + barBufferRect.left);
            barRect.set(barShadowRect.left, barShadowRect.top, right, barShadowRect.bottom);
        }
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = specSize;
        }
        return result;
    }
}
