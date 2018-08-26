package com.vmloft.develop.app.videoplayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.vmloft.develop.app.videoplayer.R;
import com.vmloft.develop.library.tools.utils.VMDimen;

/**
 * Created by chalilayang on 2016/12/13.
 */

public class PlaySeekBar extends View {

    private static final String TAG = "CustomSeekBar";
    public static final int MAX = 10000;

    private float mDownX;
    private float mDownY;
    private float mTouchSlop;

    private float circleRadius;
    private int circleColor;
    private Paint circlePaint;

    private float barHeight;
    private Paint barPaint;
    private LinearGradient gradient;
    private Paint barShadowPaint;
    private Paint barBufferPaint;
    private int startColor;
    private int endColor;

    private int measuredWidth;
    private int measuredHeight;

    private int minHeight;

    private int progress = 0;
    private int secondProgress = 0;

    private Point circleCenter;
    private Rect barRect;
    private Rect barShadowRect;
    private Rect barBufferRect;

    private OnSeekUpdateListener mCallback;

    private boolean isDragging = false;

    public PlaySeekBar(Context context) {
        this(context, null);
    }

    public PlaySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(false);
        setWillNotDraw(false);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        circleColor = getResources().getColor(R.color.vm_white);
        barHeight = VMDimen.dp2px(4);
        minHeight = VMDimen.dp2px(30);
        startColor = getResources().getColor(R.color.play_seek_bar_start);
        endColor = getResources().getColor(R.color.play_seek_bar_end);
        Log.i(TAG, "init: circleRadius " + circleRadius + " barHeight " + barHeight);
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(circleColor);

        barPaint = new Paint();
        barPaint.setAntiAlias(true);
        barPaint.setStyle(Paint.Style.FILL);

        barShadowPaint = new Paint();
        barShadowPaint.setAntiAlias(true);
        barShadowPaint.setStyle(Paint.Style.FILL);
        barShadowPaint.setColor(getResources().getColor(R.color.vm_white_38));

        barBufferPaint = new Paint();
        barBufferPaint.setAntiAlias(true);
        barBufferPaint.setStyle(Paint.Style.FILL);
        barBufferPaint.setColor(getResources().getColor(R.color.vm_white));

        circleRadius = 0;
        circleCenter = new Point();
        barRect = new Rect();
        barShadowRect = new Rect();
        barBufferRect = new Rect();
    }

    private void setProgressValue(int tmpValue, boolean fromUser) {
        if (tmpValue >= 0 && this.progress != tmpValue && tmpValue <= MAX) {
            this.progress = tmpValue;
            updateProgressBarRect();
            if (this.mCallback != null) {
                this.mCallback.onSeekUpdate(tmpValue, fromUser);
            }
            invalidate();
        } else if (tmpValue > MAX && this.progress != tmpValue) {
            this.progress = MAX;
            updateProgressBarRect();
            if (this.mCallback != null) {
                this.mCallback.onSeekUpdate(MAX, fromUser);
            }
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

    public int getMax() {
        return MAX;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = MotionEventCompat.getActionMasked(event);
        final float x = event.getX();
        final float y = event.getY();
        switch (actionMasked) {
        case MotionEvent.ACTION_DOWN:
            mDownX = x;
            mDownY = y;
            final float distance = (float) Math.sqrt((mDownX - circleCenter.x) * (mDownX - circleCenter.x) + (mDownY - circleCenter.y) * (mDownY - circleCenter.y));
            if (distance <= 2 * circleRadius) {
                Log.i(TAG, "onTouchEvent: isDragging " + isDragging);
                isDragging = true;
                invalidate();
            } else {
                int value = getProgressByPos(x, y);
                Log.i(TAG, "onTouchEvent: value " + value);
                setProgressValue(value, true);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (isDragging) {
                Log.i(TAG, "onTouchEvent: isDragging " + isDragging + "  x " + x);
                int value = getProgressByPos(x, y);
                Log.i(TAG, "onTouchEvent: value " + value);
                setProgressValue(value, true);
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            if (isDragging) {
                isDragging = false;
                if (this.mCallback != null) {
                    this.mCallback.onSeekRelease();
                }
            }
            invalidate();
            break;
        }
        return true;
    }

    private int getProgressByPos(float posX, float posY) {
        final float tmpX = posX - barShadowRect.left;
        float value = tmpX * MAX * 1.0f / barShadowRect.width();
        return (int) value;
    }

    public void setOnSeekUpdateListener(OnSeekUpdateListener lis) {
        if (lis != null) {
            this.mCallback = lis;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBar(canvas);
        drawCircle(canvas);
    }

    private void drawBar(Canvas canvas) {
        canvas.drawRect(barShadowRect, barShadowPaint);
        canvas.drawRect(barBufferRect, barBufferPaint);
        canvas.drawRect(barRect, barPaint);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(circleCenter.x, circleCenter.y, circleRadius, circlePaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = minHeight;
        if (height < barHeight) {
            measuredHeight = (int) (2 * barHeight);
        }
        measuredWidth = width;
        measuredHeight = height;
        circleRadius = measuredHeight / 2;
        gradient = new LinearGradient(0, 0, measuredWidth - 2 * circleRadius, 0, startColor, endColor, Shader.TileMode.CLAMP);
        barPaint.setShader(gradient);
        updateBarRect();
        Log.i(TAG, "onMeasure: " + measuredWidth + " " + measuredHeight);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private void updateBarRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int top = (int) ((measuredHeight - barHeight) * 0.5);
            int left = (int) circleRadius;
            int right = (int) (measuredWidth - circleRadius);
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
            updateCircleRect();
        }
    }

    private void updateProgressBarRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int right = (int) (barShadowRect.width() * progress * 1.0 / MAX + barBufferRect.left);
            barRect.set(barShadowRect.left, barShadowRect.top, right, barShadowRect.bottom);
            updateCircleRect();
        }
    }

    private void updateCircleRect() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            int centerX = barRect.right;
            int centerY = (int) (measuredHeight * 0.5);
            circleCenter.set(centerX, centerY);
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

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number).
            result = specSize;
        }
        return result;
    }

    /**
     * Paint.setTextSize(float textSize) default unit is px.
     *
     * @param spValue The real size of text
     * @return int - A transplanted sp
     */
    public int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    protected int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public interface OnSeekUpdateListener {

        void onSeekRelease();

        void onSeekUpdate(int value, boolean fromUser);
    }
}
