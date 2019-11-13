package com.zj.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZJJ on 2018/7/3.
 */
@SuppressWarnings({"unused", "unchecked"})
public class BaseLoadingView extends FrameLayout {

    public BaseLoadingView(Context context) {
        this(context, null, 0);
    }

    public BaseLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private static final int defaultAnimationDuration = 400;

    private DisplayMode oldMode = DisplayMode.none;
    private Map<DisplayMode, Float> disPlayViews;
    private View rootView;
    private View noData, noNetwork, blvChildBg, curBackgroundView;
    private ProgressBar loading;
    private TextView tvHint, tvRefresh;
    private CallRefresh refresh;
    private Handler handler;

    private Drawable bg, bgOnContent, needBackground, oldBackground;
    private int noDataRes = -1;
    private int noNetworkRes = -1;
    private int loadingRes = -1;
    private int hintTextColor, refreshTextColor;

    private boolean showOnContentDefault;

    private String loadingHint = "";
    private String noDataHint = "";
    private String networkErrorHint = "";
    private String refreshHint = "";

    private boolean refreshEnable = true;
    private boolean refreshEnableWithView = false;

    private BaseLoadingValueAnimator valueAnimator;

    public void setRefreshEnable(boolean enable) {
        this.refreshEnable = enable;
    }

    public interface CallRefresh {
        void onCallRefresh();
    }

    public enum DisplayMode {
        none(0), loading(1), noData(2), noNetwork(3), normal(4);

        private final int value;
        private long delay;

        public DisplayMode delay(long mills) {
            this.delay = mills;
            return this;
        }

        public void reset() {
            this.delay = 0L;
        }

        DisplayMode(int value) {
            this.value = value;
        }
    }

    /**
     * when you set mode as noData/noNetwork ,
     * you can get the event when this view was clicked
     * and you can refresh content  when the  "onCallRefresh()" callback
     */
    public void setRefreshListener(CallRefresh refresh) {
        this.refresh = refresh;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (refreshEnable && refreshEnableWithView && BaseLoadingView.this.refresh != null) {
                    BaseLoadingView.this.refresh.onCallRefresh();
                }
            }
        });
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BaseLoadingView);
            try {
                bg = array.getDrawable(R.styleable.BaseLoadingView_backgroundFill);
                bgOnContent = array.getDrawable(R.styleable.BaseLoadingView_backgroundUnderTheContent);
                noDataRes = array.getResourceId(R.styleable.BaseLoadingView_noDataRes, -1);
                noNetworkRes = array.getResourceId(R.styleable.BaseLoadingView_noNetworkRes, -1);
                loadingRes = array.getResourceId(R.styleable.BaseLoadingView_loadingRes, -1);
                hintTextColor = array.getColor(R.styleable.BaseLoadingView_hintColor, -1);
                refreshTextColor = array.getColor(R.styleable.BaseLoadingView_refreshTextColor, -1);
                loadingHint = array.getString(R.styleable.BaseLoadingView_loadingText);
                noDataHint = array.getString(R.styleable.BaseLoadingView_noDataText);
                networkErrorHint = array.getString(R.styleable.BaseLoadingView_networkErrorText);
                refreshHint = array.getString(R.styleable.BaseLoadingView_refreshText);
                showOnContentDefault = array.getBoolean(R.styleable.BaseLoadingView_shownUnderTheContentDefault, false);
                refreshEnable = array.getBoolean(R.styleable.BaseLoadingView_refreshEnable, true);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
        initView(context);
        handler = new Handler(Looper.getMainLooper());
    }

    private void initView(Context context) {
        rootView = inflate(context, R.layout.loading_view, this);
        noData = f(R.id.blv_vNoData);
        noNetwork = f(R.id.blv_vNoNetwork);
        loading = f(R.id.blv_pb);
        tvHint = f(R.id.blv_tvHint);
        tvRefresh = f(R.id.blv_tvRefresh);
        blvChildBg = f(R.id.blv_child_bg);
        if (refreshHint != null && !refreshHint.isEmpty()) tvRefresh.setText(refreshHint);
        if (hintTextColor != 0)
            tvHint.setTextColor(hintTextColor);
        if (refreshTextColor != 0)
            tvRefresh.setTextColor(refreshTextColor);
        disPlayViews = new HashMap<>();
        disPlayViews.put(DisplayMode.loading, 0.0f);
        tvHint.setText(loadingHint);
        resetUi();
        resetBackground(showOnContentDefault);
    }

    private void resetBackground(boolean showOnContent) {
        curBackgroundView = showOnContent ? blvChildBg : this;
        blvChildBg.setBackground(showOnContent ? bgOnContent : null);
        setBackground(showOnContent ? null : bg);
    }

    private BaseLoadingAnimatorListener listener = new BaseLoadingAnimatorListener() {

        @Override
        public void onDurationChange(ValueAnimator animation, float offset, DisplayMode mode, boolean isShowOnContent) {
            synchronized (BaseLoadingView.this) {
                onAnimationFraction(animation.getAnimatedFraction(), offset, mode);
            }
        }

        @Override
        public void onAnimEnd(Animator animation, DisplayMode mode, boolean isShowOnContent) {
            synchronized (BaseLoadingView.this) {
                onAnimationFraction(1.0f, 1.0f, mode);
            }
        }
    };

    /**
     * @param drawableRes must be an animatorDrawable in progressBar;
     * @link call resetUi() after set this
     */
    public BaseLoadingView setLoadingDrawable(int drawableRes) {
        this.loadingRes = drawableRes;
        return this;
    }

    //call resetUi() after set this
    public BaseLoadingView setNoDataDrawable(int drawableRes) {
        this.noDataRes = drawableRes;
        return this;
    }

    //call resetUi() after set this
    public BaseLoadingView setNoNetworkDrawable(int drawableRes) {
        this.noNetworkRes = drawableRes;
        return this;
    }

    //reset loading/noData/noNetwork drawable
    private void resetUi() {
        if (loadingRes > 0) {
            Drawable drawable = getContext().getDrawable(loadingRes);
            if (drawable != null) {
                Rect rect = loading.getIndeterminateDrawable().getBounds();
                drawable.setBounds(rect);
                loading.setIndeterminateDrawable(drawable);
            }
        }
        if (noDataRes > 0) {
            noData.setBackgroundResource(noDataRes);
        }
        if (noNetworkRes > 0) {
            noNetwork.setBackgroundResource(noNetworkRes);
        }
    }


    public void setMode(DisplayMode mode) {
        setMode(mode, "", "");
    }

    public void setMode(DisplayMode mode, String hint) {
        setMode(mode, hint, "");
    }

    public void setMode(DisplayMode mode, boolean showOnContent) {
        setMode(mode, "", "", showOnContent);
    }

    public void setMode(DisplayMode mode, String hint, String subHint) {
        setMode(mode, hint, subHint, null);
    }

    public void setMode(DisplayMode mode, String hint, boolean showOnContent) {
        setMode(mode, hint, "", showOnContent);
    }

    public void setMode(final DisplayMode mode, final String hint, final String subHint, final Boolean showOnContent) {
        handler.removeCallbacksAndMessages(null);
        Log.e("----- ", "" + mode.delay);
        if (mode.delay > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BaseLoadingView.this.setLoadingMode(mode, hint, subHint, showOnContent);
                }
            }, mode.delay);
        } else {
            setLoadingMode(mode, hint, subHint, showOnContent);
        }
    }

    /**
     * just call setMode after this View got,
     *
     * @param mode          the current display mode you need;
     * @param showOnContent is showing on content? or hide content?
     * @param hint          show something when it`s change a mode;
     */
    private void setLoadingMode(DisplayMode mode, String hint, String subHint, Boolean showOnContent) {
        if (showOnContent == null) showOnContent = showOnContentDefault;
        if (mode == DisplayMode.none) mode = DisplayMode.normal;
        int newCode = (showOnContent ? -10 : 10) + mode.value;
        int oldCode = (showOnContent ? -10 : 10) + oldMode.value;
        oldMode = mode;
        boolean isSameMode = newCode == oldCode;
        String hintText = (!TextUtils.isEmpty(hint) ? hint : getHintString(mode));
        if (hintText != null) {
            tvHint.setText(hintText);
        }
        refreshEnableWithView = refreshEnable && (mode == DisplayMode.noData || mode == DisplayMode.noNetwork);
        tvRefresh.setVisibility(refreshEnableWithView ? View.VISIBLE : View.INVISIBLE);
        if (refreshEnableWithView) {
            tvRefresh.setText(TextUtils.isEmpty(subHint) ? refreshHint : subHint);
        }
        if (valueAnimator == null) {
            valueAnimator = new BaseLoadingValueAnimator(listener);
            valueAnimator.setDuration(defaultAnimationDuration);
        } else {
            valueAnimator.end();
        }
        disPlayViews.put(mode, 0.0f);
        if (!isSameMode) {
            resetBackground(showOnContent);
            needBackground = showOnContent ? bgOnContent : bg;
            valueAnimator.start(mode, showOnContent);
        }
    }

    private String getHintString(DisplayMode mode) {
        switch (mode) {
            case loading:
                return (loadingHint == null || loadingHint.isEmpty()) ? "loading" : loadingHint;
            case noData:
                return (noDataHint == null || noDataHint.isEmpty()) ? "no data found" : noDataHint;
            case noNetwork:
                return (networkErrorHint == null || networkErrorHint.isEmpty()) ? "no network access" : networkErrorHint;
            default:
                return "";
        }
    }

    private synchronized void onAnimationFraction(float duration, float offset, DisplayMode curMode) {
        setViews(offset, curMode);
        setBackground(duration, offset, curMode);
    }


    private void setViews(float offset, DisplayMode curMode) {
        for (Map.Entry<DisplayMode, Float> entry : disPlayViews.entrySet()) {
            View curSetView = getDisplayView(entry.getKey());
            if (curSetView != null) {
                float curAlpha = entry.getValue();
                float newAlpha;
                if (entry.getKey() == curMode) {
                    //need show
                    if (curSetView.getVisibility() != VISIBLE) {
                        curSetView.setVisibility(VISIBLE);
                        curSetView.setAlpha(0);
                    }
                    newAlpha = Math.min(1.0f, Math.max(0.0f, curAlpha) + offset);
                    curSetView.setAlpha(newAlpha);
                } else {
                    //need hide
                    newAlpha = Math.max(Math.min(1.0f, curAlpha) - offset, 0);
                    curSetView.setAlpha(newAlpha);
                    if (newAlpha == 0 && curSetView.getVisibility() != GONE)
                        curSetView.setVisibility(GONE);
                }
                disPlayViews.put(entry.getKey(), newAlpha);
            }
        }
    }

    private void setBackground(float duration, float offset, DisplayMode curMode) {
        if (curMode != DisplayMode.normal) {
            if (getVisibility() != VISIBLE) {
                setAlpha(0);
                setVisibility(VISIBLE);
            }
            if (oldBackground != needBackground) {
                curBackgroundView.setBackground(needBackground);
                oldBackground = needBackground;
            }
            if (getAlpha() >= 1.0f) {
                setAlpha(1);
            } else {
                setAlpha(Math.min(1.0f, duration));
            }
        } else {
            setAlpha(1.0f - duration);
            if (getAlpha() <= 0.05f) {
                setAlpha(0);
                setBackground(oldBackground = null);
                setVisibility(GONE);
            }
        }
    }

    private View getDisplayView(DisplayMode mode) {
        switch (mode) {
            case noData:
                return noData;
            case loading:
                return loading;
            case noNetwork:
                return noNetwork;
        }
        return null;
    }

    private <T extends View> T f(int id) {
        return (T) rootView.findViewById(id);
    }


    public static class BaseLoadingValueAnimator extends ValueAnimator {

        private DisplayMode curMode;
        private boolean isShowOnContent;
        private float curDuration;
        private boolean isCancel;

        private BaseLoadingAnimatorListener listener;

        private void start(DisplayMode mode, boolean isShowOnContent) {
            if (isRunning()) cancel();
            this.curMode = mode;
            this.isShowOnContent = isShowOnContent;
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
            setFloatValues(0.0f, 1.0f);
            addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (curDuration != 0) curDuration = 0;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    curDuration = 0;
                    if (isCancel) return;
                    if (listener != null)
                        listener.onAnimEnd(animation, curMode, isShowOnContent);
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
                        listener.onDurationChange(animation, offset, curMode, isShowOnContent);
                        curDuration = duration;
                    }
                }
            });
        }

        public void setAnimatorListener(BaseLoadingAnimatorListener listener) {
            this.listener = listener;
        }
    }

    public interface BaseLoadingAnimatorListener {

        void onDurationChange(ValueAnimator animation, float duration, DisplayMode mode, boolean isShowOnContent);

        void onAnimEnd(Animator animation, DisplayMode mode, boolean isShowOnContent);
    }
}
