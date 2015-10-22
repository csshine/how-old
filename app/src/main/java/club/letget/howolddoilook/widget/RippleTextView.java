package club.letget.howolddoilook.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class RippleTextView extends TextView {

    private int mInitX;
    private int mInitY;


    private int mRadius;
    private int mStep;
    private int mDrawRadius;

    private boolean mDrawFinish;

    private boolean mDrawBack  ;

    private final int DURATION = 240;
    private final int FREQUENCY = 10;
    private int mCycle;
    private final Rect mRect = new Rect();


    private Paint mRevealPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RippleTextView(Context context) {
        super(context);
        initView(context);
    }

    public RippleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RippleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mRevealPaint.setColor(0x10000000);
        mCycle = DURATION / FREQUENCY;
        mDrawFinish = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawFinish) {
            super.onDraw(canvas);
            return;
        }
        canvas.drawColor(0x08000000);
        super.onDraw(canvas);
        if (mStep == 0) {
            return;
        }
        mDrawRadius = mDrawRadius + mStep;


        if (mDrawRadius > mRadius) {
            mDrawRadius = 0;
            canvas.drawCircle(mInitX, mInitY, mRadius, mRevealPaint);
            mDrawFinish = true;
            ViewCompat.postInvalidateOnAnimation(this);
            return;
        }

        canvas.drawCircle(mInitX, mInitY, mDrawRadius, mRevealPaint);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    private void updateDrawData() {
        int radiusLeftTop = (int) Math.sqrt((mRect.left - mInitX) * (mRect.left - mInitX) +
                (mRect.top - mInitY) * (mRect.top - mInitY));
        int radiusRightTop = (int) Math.sqrt((mRect.right - mInitX) * (mRect.right - mInitX) +
                (mRect.top - mInitY) * (mRect.top - mInitY));
        int radiusLeftBottom = (int) Math.sqrt((mRect.left - mInitX) * (mRect.left - mInitX) +
                (mRect.bottom - mInitY) * (mRect.bottom - mInitY));
        int radiusRightBottom = (int) Math.sqrt((mRect.right - mInitX) * (mRect.right - mInitX) +
                (mRect.bottom - mInitY) * (mRect.bottom - mInitY));
        mRadius = getMax(radiusLeftTop, radiusRightTop, radiusLeftBottom, radiusRightBottom);
        mStep = mRadius/mCycle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mDrawFinish = false;
                int index = MotionEventCompat.getActionIndex(event);
                int eventId = MotionEventCompat.getPointerId(event, index);
                if (eventId != -1) {
                    mInitX = (int) MotionEventCompat.getX(event, index);
                    mInitY = (int) MotionEventCompat.getY(event, index);
                    updateDrawData();
                    ViewCompat.postInvalidateOnAnimation(this);
                }
                //return true;//返回此值，不会响应点击事件
                return super.onTouchEvent(event);
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mStep = (int) (2.5f * mStep);
                mDrawBack = true ;
                //this.setVisibility(View.GONE);
                //this.setOnClickListener();
                break;
        }
        return super.onTouchEvent(event);
    }

    private int getMax(int... radius) {
        if (radius.length == 0) {
            return 0;
        }
        int max = radius[0];
        for (int m : radius) {
            if (m > max) {
                max = m;
            }
        }
        return max;
    }

}
