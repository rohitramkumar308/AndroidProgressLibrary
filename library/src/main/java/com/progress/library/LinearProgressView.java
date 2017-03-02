package com.progress.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by rramkumar on 2/28/17
 */

public class LinearProgressView extends View {

    public static final int DURATION = 800;
    private int radius = 30;
    private RectF[] rectPositions;
    private Point[] circlePositions;
    private boolean[] circleVisited;
    private boolean[] completedCircleAnimations;
    private boolean isStarted, isRunning;
    private Paint[] fillPaint;
    private int circleCount;
    private int counter, nextCounter;
    private ValueAnimator frontAnimator, backAnimator;
    private boolean isReverseDirection = false;

    public LinearProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        resetCounters(false);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveLoadingView, 0, 0);

        radius = typedArray.getDimensionPixelSize(R.styleable.WaveLoadingView_radius, 15);
        circleCount = typedArray.getInteger(R.styleable.WaveLoadingView_circleCount, 4);
        int circleColorResId = typedArray.getResourceId(R.styleable.WaveLoadingView_colors, -1);

        completedCircleAnimations = new boolean[circleCount];
        circleVisited = new boolean[circleCount];
        circlePositions = new Point[circleCount];
        rectPositions = new RectF[circleCount];
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

            circlePositions[i] = new Point();
            rectPositions[i] = new RectF();
        }

        typedArray.recycle();

        frontAnimator = ValueAnimator.ofFloat();
        backAnimator = ValueAnimator.ofFloat();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, getResources().getDisplayMetrics());
        int desiredWidth = (int) (radius * 2 * 4 + padding * 2);
        int desiredHeight = (int) (radius * 2 + padding * 2);
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
        int centerX = widthPerCircle / 2;
        int centerY = heightPerCircle / 2;

        for (int i = 0; i < circleCount; i++) {
            if (!isRunning) {
                rectPositions[i].top = centerY - radius;
                rectPositions[i].left = centerX - radius;
                rectPositions[i].bottom = centerY + radius;
                rectPositions[i].right = centerX + radius;
                circlePositions[i].set(centerX, centerY);

                if (i == 0) {
                    canvas.drawRoundRect(rectPositions[0], radius, radius, fillPaint[i]);
                }
            } else {
                canvas.drawRoundRect(rectPositions[counter], radius, radius, fillPaint[counter]);
            }
            if (circleVisited[i]) {
                canvas.drawCircle(circlePositions[i].x, circlePositions[i].y, radius, fillPaint[i]);
            }

            centerX += widthPerCircle;
        }

        if (isStarted) {
            if (!isRunning) {
                startAnimation();
            } else {

                if (completedCircleAnimations[counter]) {
                    if (!isReverseDirection) {
                        counter += 1;
                        nextCounter = counter + 1;
                        if (nextCounter == circleCount) {
                            resetCounters(true);
                            isReverseDirection = true;
                            reset();
                        }
                    }

                    if (isReverseDirection) {
                        counter -= 1;
                        nextCounter = counter - 1;
                        if (nextCounter < 0) {
                            resetCounters(false);
                            isReverseDirection = false;
                            reset();
                        }
                    }
                }

                initAnimators();
            }
        }

    }

    private void resetCounters(boolean isReverse) {
        if (!isReverse) {
            counter = 0;
            nextCounter = 1;
        } else {
            counter = circleCount;
            nextCounter = circleCount - 1;
        }
    }

    private void reset() {
        for (int i = 0; i < circleCount; i++) {
            rectPositions[i].top = circlePositions[i].y - radius;
            rectPositions[i].left = circlePositions[i].x - radius;
            rectPositions[i].bottom = circlePositions[i].y + radius;
            rectPositions[i].right = circlePositions[i].x + radius;
            completedCircleAnimations[i] = false;

            if (!isStarted)
                circleVisited[i] = false;
        }
    }

    public void start() {
        if (!isStarted) {
            isStarted = true;
            invalidate();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        if (isStarted) {
            isStarted = false;
            isRunning = false;
            resetCounters(false);
            stopAnimation();
        }
    }

    private void startAnimation() {
        initAnimators();
        isRunning = true;
    }

    private void stopAnimation() {
        frontAnimator.cancel();
        backAnimator.cancel();
        reset();
        invalidate();
    }

    private void initAnimators() {
        if (!(frontAnimator.isStarted() || backAnimator.isStarted())) {
            if (!isReverseDirection) {
                frontAnimator.setFloatValues(rectPositions[counter].right, rectPositions[nextCounter].right);
                backAnimator.setFloatValues(rectPositions[counter].left, rectPositions[nextCounter].left);
            } else {
                frontAnimator.setFloatValues(rectPositions[counter].left, rectPositions[nextCounter].left);
                backAnimator.setFloatValues(rectPositions[counter].right, rectPositions[nextCounter].right);
            }

            frontAnimator.setDuration(DURATION);
            frontAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!isReverseDirection) {
                        rectPositions[counter].right = (float) animation.getAnimatedValue();
                    } else {
                        rectPositions[counter].left = (float) animation.getAnimatedValue();
                    }
                    invalidate();
                }
            });

            frontAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    circleVisited[counter] = !isReverseDirection;
                    backAnimator.start();
                }
            });
            frontAnimator.start();

            backAnimator.setDuration(DURATION);
            backAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (!isReverseDirection) {
                        rectPositions[counter].left = (float) animation.getAnimatedValue();
                    } else {
                        rectPositions[counter].right = (float) animation.getAnimatedValue();
                    }
                    invalidate();
                }
            });

            backAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    completedCircleAnimations[counter] = true;
                    invalidate();
                }
            });
        }
    }
}
