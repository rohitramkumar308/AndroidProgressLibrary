package com.progress.library;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class WaveProgressView extends View {

    public int WAVE_MAX_HEIGHT = 160;
    private int radius = 30;
    private float[] circlePos;
    private ValueAnimator[] animators;
    private boolean isStarted, isRunning;
    private float centerY;
    private Paint[] fillPaint;
    private int circleCount;

    public WaveProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveLoadingView, 0, 0);

        radius = typedArray.getDimensionPixelSize(R.styleable.WaveLoadingView_radius, 15);
        circleCount = typedArray.getInteger(R.styleable.WaveLoadingView_circleCount, 4);
        int circleColorResId = typedArray.getResourceId(R.styleable.WaveLoadingView_colors, -1);

        WAVE_MAX_HEIGHT = typedArray.getDimensionPixelSize(R.styleable.WaveLoadingView_waveHeight, WAVE_MAX_HEIGHT);

        circlePos = new float[circleCount];
        animators = new ValueAnimator[circleCount];
        fillPaint = new Paint[circleCount];
        int[] colors = new int[circleCount];

        Resources resources = getResources();

        if (circleColorResId != -1) {
            int[] tmpColorArray = resources.getIntArray(circleColorResId);
            System.arraycopy(tmpColorArray, 0, colors, 0, tmpColorArray.length);
        }

        for (int i = 0; i < circleCount; i++) {
            fillPaint[i] = new Paint();
            fillPaint[i].setStrokeWidth(2f);
            fillPaint[i].setStyle(Paint.Style.FILL);
            if (colors[i] != 0) {
                fillPaint[i].setColor(colors[i]);
            } else {
                fillPaint[i].setColor(Color.BLACK);
            }
        }

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, getResources().getDisplayMetrics());
        int desiredWidth = (int) (radius * 2 * circlePos.length + padding * 2);
        int desiredHeight = (int) (radius * 2 + WAVE_MAX_HEIGHT + padding * 2);
        int width = reconcileSize(desiredWidth, widthMeasureSpec);
        int height = reconcileSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int reconcileSize(int size, int measureSpec) {
        final int specMode = MeasureSpec.getMode(measureSpec);
        final int specSize = MeasureSpec.getSize(measureSpec);
        final int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, size);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = size;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int widthPerCircle = getMeasuredWidth() / circleCount;
        int heightPerCircle = getMeasuredHeight();
        float centerX = widthPerCircle / 2;
        centerY = heightPerCircle / 2 + 80;

        for (int i = 0; i < circleCount; i++) {
            canvas.drawCircle(centerX, circlePos[i] == 0 ? centerY : circlePos[i], radius, fillPaint[i]);
            centerX += widthPerCircle;
        }

        if (isStarted && !isRunning) {
            startLoadingAnimation();
        }
    }

    public void start() {
        if (!isStarted) {
            isStarted = true;
            invalidate();
        }
    }

    public void stop() {
        if (isStarted) {
            isStarted = false;
            isRunning = false;
            stopLoadingAnimation();
        }
    }

    private void startLoadingAnimation() {
        initAnimators();
        isRunning = true;
        animators[0].start();
    }

    private void stopLoadingAnimation() {
        for (int i = 0; i < animators.length; i++) {
            animators[i].cancel();
            circlePos[i] = centerY;
        }

        invalidate();
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void initAnimators() {
        for (int i = 0; i < circleCount; i++) {
            final int circleId = i;
            circlePos[circleId] = centerY;

            ValueAnimator animator = ValueAnimator.ofFloat(circlePos[circleId], circlePos[circleId] - WAVE_MAX_HEIGHT, centerY);
            animators[circleId] = animator;
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setDuration(1000);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    circlePos[circleId] = (float) animation.getAnimatedValue();
                    int prevCircleId = circleId == 0 ? 0 : circleId;
                    int nextCircleId = circleId + 1 < animators.length ? circleId + 1 : circleId;

                    if (!animators[nextCircleId].isStarted()
                            && circlePos[prevCircleId] <= centerY - 30) {
                        animators[nextCircleId].start();
                    }

                    invalidate();
                }
            });
        }
    }
}
