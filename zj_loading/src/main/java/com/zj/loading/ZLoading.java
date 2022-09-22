package com.zj.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;


/**
 * @author ZJJ on 2018/7/3.
 */
@SuppressWarnings("unused")
public abstract class ZLoading extends FrameLayout {

    private DisplayMode oldMode = DisplayMode.NONE;
    private View rootView;
    private View blvChildBg;
    private ViewGroup contentView;
    private OnChangeListener onMode;
    private final Handler handler;
    private Drawable bg, bgOnContent;
    private int animateDuration = 0;
    private int shownModeDefault = 0;
    private boolean refreshEnable = true;
    private boolean refreshEnableWithView = false;
    protected DisplayMode modeDefault = DisplayMode.NONE;
    private OnTapListener refresh;
    private int curRefreshViewId = -1;
    private float cbLeftPadding, cbTopPadding, cbRightPadding, cbBottomPadding;
    private Map<DisplayMode, Float> disPlayViews;

    public ZLoading(Context context) {
        this(context, null, 0);
    }

    public ZLoading(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        initView(context);
        if (!isInEditMode()) {
            this.setVisibility(View.GONE);
        }
        //noinspection NullableProblems
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 998) {
                    setContentBgSize(false);
                } else {
                    onViewVisibilityChanged(msg.what, msg.arg1 == 0);
                }
            }
        };
    }

    private BaseLoadingValueAnimator valueAnimator;

    protected abstract void onViewInflated();

    protected abstract View inflateContentView(ViewStub vs);

    protected abstract void onMode(DisplayMode mode, boolean isSameMode, boolean viewEnable);

    protected abstract View getDisplayView(DisplayMode mode);

    public void onViewVisibilityChanged(int viewId, boolean visible) {}

    public void setRefreshEnable(boolean enable) {
        this.refreshEnable = enable;
    }

    public void setMode(DisplayMode mode) {
        setMode(mode, null, false);
    }

    public void setMode(DisplayMode mode, boolean isSetNow) {
        setMode(mode, null, isSetNow);
    }

    public void setMode(DisplayMode mode, OverLapMode overlapMode) {
        setMode(mode, overlapMode, false);
    }

    public void setMode(final DisplayMode mode, final OverLapMode overlapMode, final boolean isSetNow) {
        handler.removeCallbacksAndMessages(null);
        long delay = mode.delay;
        if (valueAnimator != null) valueAnimator.end();
        if (!isSetNow && delay > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ZLoading.this.setLoadingMode(mode, overlapMode, false);
                }
            }, delay);
        } else {
            ZLoading.this.setLoadingMode(mode, overlapMode, isSetNow);
        }
        mode.reset();
    }

    /**
     * when you set mode as NO_DATA/NO_NETWORK ,
     * you can get the event when this view was clicked,
     * and you can refresh content when the OnTapListener callback.
     */
    public void setOnTapListener(OnTapListener refresh) {
        this.refresh = refresh;
        View rv = curRefreshViewId == -1 ? null : contentView.findViewById(curRefreshViewId);
        if (rv == null) rv = rootView;
        if (curRefreshViewId != -1) {
            if (curRefreshViewId == rootView.getId()) {
                rootView.setOnClickListener(defaultOnclickListener);
            } else {
                View oldV = findViewById(curRefreshViewId);
                if (oldV != null) oldV.setOnClickListener(null);
            }
        }
        curRefreshViewId = rv.getId();
        rv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (refreshEnable && refreshEnableWithView && ZLoading.this.refresh != null) {
                    ZLoading.this.refresh.onTap();
                }
            }
        });
    }

    /**
     * set a mode changed listener
     */
    public void setOnChangeListener(OnChangeListener mode) {
        this.onMode = mode;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZLoading);
            try {
                bg = array.getDrawable(R.styleable.ZLoading_backgroundFill);
                shownModeDefault = array.getInt(R.styleable.ZLoading_shownMode, 0);
                refreshEnable = array.getBoolean(R.styleable.ZLoading_refreshEnable, true);
                animateDuration = array.getInt(R.styleable.ZLoading_changeAnimDuration, 400);
                bgOnContent = array.getDrawable(R.styleable.ZLoading_backgroundUnderTheContent);
                float contentPadding = array.getDimension(R.styleable.ZLoading_contentPadding, 0f);
                cbLeftPadding = array.getDimension(R.styleable.ZLoading_contentPaddingStart, contentPadding);
                cbRightPadding = array.getDimension(R.styleable.ZLoading_contentPaddingEnd, contentPadding);
                cbTopPadding = array.getDimension(R.styleable.ZLoading_contentPaddingTop, contentPadding);
                cbBottomPadding = array.getDimension(R.styleable.ZLoading_contentPaddingBottom, contentPadding);
                int mode = array.getInt(R.styleable.ZLoading_modeDefault, DisplayMode.NONE.value);
                for (DisplayMode m : DisplayMode.values()) {
                    if (mode == m.value) {
                        modeDefault = m;
                        break;
                    }
                }
            } finally {
                array.recycle();
            }
        }
    }

    private void initView(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.loading_view, this, true);
        blvChildBg = f(R.id.blv_child_bg);
        ViewStub vs = f(R.id.blv_child_content_stub);
        contentView = (ViewGroup) inflateContentView(vs);
        disPlayViews = new HashMap<>();
        disPlayViews.put(modeDefault, 0.0f);
        if (modeDefault != DisplayMode.NORMAL && modeDefault != DisplayMode.NONE) {
            OverLapMode defaultMode = getMode(shownModeDefault);
            resetBackground(defaultMode);
        }
    }

    private void resetBackground(OverLapMode mode) {
        setBackground((mode == OverLapMode.OVERLAP || mode == OverLapMode.FO) ? bg : null);
        Drawable drawable = (mode == OverLapMode.FLOATING || mode == OverLapMode.FO) ? bgOnContent : null;
        blvChildBg.setBackground(drawable);
        blvChildBg.setVisibility((drawable == null) ? View.GONE : View.VISIBLE);
    }

    private final BaseLoadingAnimatorListener listener = new BaseLoadingAnimatorListener() {

        @Override
        public void onDurationChange(ValueAnimator animation, float offset, DisplayMode mode, OverLapMode overLapMode) {
            synchronized (ZLoading.this) {
                onAnimationFraction(animation.getAnimatedFraction(), offset, mode);
            }
        }

        @Override
        public void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode) {
            synchronized (ZLoading.this) {
                onAnimationFraction(1.0f, 1.0f, mode);
            }
        }
    };

    /**
     * just call setMode after this View got,
     *
     * @param mode        the current display mode you need;
     * @param overlapMode is showing on content? or hide content?
     */
    private void setLoadingMode(DisplayMode mode, OverLapMode overlapMode, boolean isSetNow) {
        refreshEnableWithView = refreshEnable && (mode == DisplayMode.NO_DATA || mode == DisplayMode.NO_NETWORK);
        if (overlapMode == null) overlapMode = getMode(shownModeDefault);
        if (mode == DisplayMode.NONE) mode = DisplayMode.NORMAL;
        int newCode = overlapMode.value + mode.value;
        int oldCode = overlapMode.value + oldMode.value;
        oldMode = mode;
        boolean isSameMode = newCode == oldCode;
        if (mode != DisplayMode.NORMAL) onMode(mode, isSameMode, refreshEnableWithView);
        if (isSameMode) return;
        if (mode != DisplayMode.NORMAL) disPlayViews.put(mode, 0.0f);
        if (isSetNow || animateDuration <= 0) {
            if (valueAnimator != null) valueAnimator.end();
            onAnimationFraction(1f, 1f, mode);
        } else {
            if (valueAnimator == null) {
                valueAnimator = new BaseLoadingValueAnimator(listener);
                valueAnimator.setDuration(animateDuration);
            } else {
                valueAnimator.end();
            }
            valueAnimator.start(mode, overlapMode);
        }
        resetBackground(overlapMode);
        if (onMode != null) {
            onMode.onModeChange(mode);
        }
        handler.removeMessages(998);
        if (mode != DisplayMode.NORMAL) {
            handler.sendEmptyMessageDelayed(998, 32);
        }
    }

    private synchronized void onAnimationFraction(final float duration, final float offset, final DisplayMode curMode) {
        setViews(offset, curMode);
        setBackground(duration, curMode);
    }

    private synchronized void setViews(final float offset, final DisplayMode curMode) {
        if (curMode == DisplayMode.NONE || curMode == DisplayMode.NORMAL) {
            return;
        }
        for (Map.Entry<DisplayMode, Float> entry : disPlayViews.entrySet()) {
            final View curSetView = getDisplayView(entry.getKey());
            if (curSetView == null) {
                continue;
            }
            float curAlpha = entry.getValue();
            float newAlpha;
            if (entry.getKey() == curMode) {
                //need show
                if (curSetView.getVisibility() != VISIBLE) {
                    curSetView.setVisibility(VISIBLE);
                    curSetView.setAlpha(0);
                    onViewStateChanged(curSetView, true);
                }
                newAlpha = Math.min(1.0f, Math.max(0.0f, curAlpha) + offset);
                curSetView.setAlpha(newAlpha);
            } else {
                //need hide
                newAlpha = Math.max(Math.min(1.0f, curAlpha) - offset, 0);
                curSetView.setAlpha(newAlpha);
                if (newAlpha == 0 && curSetView.getVisibility() != GONE) {
                    curSetView.setVisibility(GONE);
                    onViewStateChanged(curSetView, false);
                }
            }
            disPlayViews.put(entry.getKey(), newAlpha);
        }
    }

    private void setBackground(float duration, DisplayMode curMode) {
        if (curMode != DisplayMode.NORMAL) {
            if (getVisibility() != VISIBLE) {
                setAlpha(0);
                setVisibility(VISIBLE);
            }
            if (getAlpha() >= 1.0f) {
                setAlpha(1);
            } else {
                setAlpha(Math.min(1.0f, duration));
            }
        } else {
            setAlpha(1.0f - duration);
            if (getAlpha() <= 0.0f) {
                setAlpha(0);
                for (Map.Entry<DisplayMode, Float> entry : disPlayViews.entrySet()) {
                    final View curSetView = getDisplayView(entry.getKey());
                    if (curSetView != null) {
                        curSetView.setVisibility(View.GONE);
                        onViewStateChanged(curSetView, false);
                    }
                }
                setBackground(null);
                setVisibility(GONE);
            }
        }
    }

    protected <T extends View> T f(int id) {
        return rootView.findViewById(id);
    }

    private OverLapMode getMode(int mode) {
        switch (mode) {
            case 0:
                return OverLapMode.OVERLAP;
            case 1:
                return OverLapMode.FLOATING;
            case 2:
                return OverLapMode.FO;
        }
        return OverLapMode.OVERLAP;
    }

    private void onViewStateChanged(View view, boolean visible) {
        handler.removeMessages(view.getId());
        Message msg = Message.obtain();
        msg.what = view.getId();
        msg.arg1 = visible ? 0 : 1;
        handler.sendMessage(msg);
    }

    private void setContentBgSize(boolean inEdit) {
        int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, right = 0, bottom = 0;
        for (int i = 0; i < contentView.getChildCount(); i++) {
            View v = contentView.getChildAt(i);
            if (v.getVisibility() == View.VISIBLE) {
                left = Math.min(v.getLeft(), left);
                top = Math.min(v.getTop(), top);
                right = Math.max(v.getRight(), right);
                bottom = Math.max(v.getBottom(), bottom);
            }
        }
        if (inEdit || left + right + top + bottom > 0) {
            LayoutParams lp = (LayoutParams) blvChildBg.getLayoutParams();
            if (lp == null) return;
            float pw = cbLeftPadding + cbRightPadding;
            lp.width = (right - left) + (int) pw;
            lp.height = (bottom - top) + (int) (cbBottomPadding + cbTopPadding);
            float rpd = 0f, lpd = 0f;
            if (cbLeftPadding > cbRightPadding) {
                rpd = cbLeftPadding - cbRightPadding;
            } else {
                lpd = cbRightPadding - cbLeftPadding;
            }
            lp.setMargins((int) lpd, (int) (top - cbTopPadding), (int) rpd, 0);
            blvChildBg.setLayoutParams(lp);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onViewInflated();
        setMode(modeDefault, true);
        setClipChildren(false);
        setClipToPadding(false);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isInEditMode() && blvChildBg != null) {
            setContentBgSize(true);
        }
    }

    public static class BaseLoadingValueAnimator extends ValueAnimator {

        private DisplayMode curMode;
        private OverLapMode overLapMode;
        private float curDuration;
        private boolean isCancel;

        private BaseLoadingAnimatorListener listener;

        private void start(DisplayMode mode, OverLapMode overLapMode) {
            if (isRunning()) cancel();
            this.curMode = mode;
            this.overLapMode = overLapMode;
            super.start();
        }

        @Override
        public void cancel() {
            removeAllListeners();
            if (listener != null) listener = null;
            isCancel = true;
            super.cancel();
        }

        private BaseLoadingValueAnimator(BaseLoadingAnimatorListener l) {
            this.listener = l;
            setFloatValues(0.0f, 1.1f);
            addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (curDuration != 0) curDuration = 0;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    curDuration = 0;
                    if (isCancel) return;
                    if (listener != null) listener.onAnimEnd(animation, curMode, overLapMode);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    curDuration = 0;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    curDuration = 0;
                }
            });

            addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (isCancel) return;
                    if (listener != null) {
                        float duration = (float) animation.getAnimatedValue();
                        float offset = duration - curDuration;
                        listener.onDurationChange(animation, offset, curMode, overLapMode);
                        curDuration = duration;
                    }
                }
            });
        }
    }

    private final View.OnClickListener defaultOnclickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {}
    };

    public interface BaseLoadingAnimatorListener {

        void onDurationChange(ValueAnimator animation, float duration, DisplayMode mode, OverLapMode overLapMode);

        void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode);
    }
}
