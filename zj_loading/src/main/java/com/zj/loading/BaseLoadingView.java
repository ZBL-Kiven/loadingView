package com.zj.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
        initView(context);
        handler = new Handler(Looper.getMainLooper());
        setMode(modeDefault, true);
    }

    private TextView tvHint, tvRefresh;
    private ImageView noData;
    private ImageView noNetwork;
    private ProgressBar loading;
    private DisplayMode oldMode = DisplayMode.NONE;
    private Map<DisplayMode, Float> disPlayViews;
    private View rootView;
    private View blvChildBg;
    private Button btnRefresh;
    private OnTapListener refresh;
    private OnChangeListener onMode;
    private final Handler handler;
    private Drawable bg, bgOnContent, btnBg;
    private int animateDuration = 0;
    private int shownModeDefault = 0;
    private int noDataRes = -1, noNetworkRes = -1, loadingRes = -1;
    private int hintTextColor, refreshTextColor, btnTextColor;
    private String loadingHint = "", noDataHint = "", networkErrorHint = "", refreshNoDataText = "", refreshNetworkText = "", btnText = "";
    private float hintTextSize, refreshTextSize, btnTextSize, drawerWidth, drawerHeight;
    private float maxRefreshTextWidth, maxHintTextWidth;
    private int maxRefreshTextLines, maxHintTextLines;
    private boolean refreshEnable = true;
    private boolean btnEnable = false;
    private boolean refreshEnableWithView = false;
    private DisplayMode modeDefault = DisplayMode.NONE;

    private BaseLoadingValueAnimator valueAnimator;

    public void setRefreshEnable(boolean enable) {
        this.refreshEnable = enable;
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

    /**
     * when you set mode as NO_DATA/NO_NETWORK ,
     * you can get the event when this view was clicked
     * and you can refresh content  when the  "onCallRefresh()" callback
     */
    public void setOnTapListener(OnTapListener refresh) {
        this.refresh = refresh;
        (btnEnable ? btnRefresh : this).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (refreshEnable && refreshEnableWithView && BaseLoadingView.this.refresh != null) {
                    BaseLoadingView.this.refresh.onTap();
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
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BaseLoadingView);
            try {
                bg = array.getDrawable(R.styleable.BaseLoadingView_backgroundFill);
                bgOnContent = array.getDrawable(R.styleable.BaseLoadingView_backgroundUnderTheContent);
                noDataRes = array.getResourceId(R.styleable.BaseLoadingView_noDataRes, -1);
                noNetworkRes = array.getResourceId(R.styleable.BaseLoadingView_noNetworkRes, -1);
                loadingRes = array.getResourceId(R.styleable.BaseLoadingView_loadingRes, -1);
                refreshTextSize = array.getDimension(R.styleable.BaseLoadingView_refreshTextSize, 48f);
                drawerWidth = array.getDimension(R.styleable.BaseLoadingView_drawerWidth, -1);
                drawerHeight = array.getDimension(R.styleable.BaseLoadingView_drawerHeight, -1);
                refreshTextColor = array.getColor(R.styleable.BaseLoadingView_refreshTextColor, -1);
                loadingHint = array.getString(R.styleable.BaseLoadingView_loadingText);
                noDataHint = array.getString(R.styleable.BaseLoadingView_noDataText);
                networkErrorHint = array.getString(R.styleable.BaseLoadingView_networkErrorText);
                shownModeDefault = array.getInt(R.styleable.BaseLoadingView_shownMode, 0);
                refreshEnable = array.getBoolean(R.styleable.BaseLoadingView_refreshEnable, true);
                refreshNoDataText = array.getString(R.styleable.BaseLoadingView_refreshNoDataText);
                refreshNetworkText = array.getString(R.styleable.BaseLoadingView_refreshNetworkText);
                animateDuration = array.getInt(R.styleable.BaseLoadingView_changeAnimDuration, 400);
                hintTextColor = array.getColor(R.styleable.BaseLoadingView_hintColor, -1);
                hintTextSize = array.getDimension(R.styleable.BaseLoadingView_hintTextSize, 24f);
                btnEnable = array.getBoolean(R.styleable.BaseLoadingView_btnEnable, false);
                maxRefreshTextWidth = array.getDimension(R.styleable.BaseLoadingView_maxRefreshTextWidth, -1);
                maxRefreshTextLines = array.getInt(R.styleable.BaseLoadingView_maxRefreshTextLines, -1);
                maxHintTextWidth = array.getDimension(R.styleable.BaseLoadingView_maxHintTextWidth, -1);
                maxHintTextLines = array.getInt(R.styleable.BaseLoadingView_maxHintTextLines, -1);
                if (btnEnable) {
                    btnBg = array.getDrawable(R.styleable.BaseLoadingView_btnBackground);
                    btnText = array.getString(R.styleable.BaseLoadingView_btnText);
                    btnTextSize = array.getDimension(R.styleable.BaseLoadingView_btnTextSize, 36f);
                    btnTextColor = array.getColor(R.styleable.BaseLoadingView_btnTextColor, Color.BLACK);
                }
                int mode = array.getInt(R.styleable.BaseLoadingView_modeDefault, DisplayMode.NONE.value);
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
        rootView = inflate(context, R.layout.loading_view, this);
        noData = f(R.id.blv_vNoData);
        noNetwork = f(R.id.blv_vNoNetwork);
        loading = f(R.id.blv_pb);
        tvHint = f(R.id.blv_tvHint);
        btnRefresh = f(R.id.blv_btnRefresh);
        View blvFlDrawer = f(R.id.blv_fl_drawer);
        if (drawerWidth > 0 && drawerHeight > 0) {
            ViewGroup.LayoutParams lp = blvFlDrawer.getLayoutParams();
            lp.width = (int) (drawerWidth + 0.5f);
            lp.height = (int) (drawerHeight + 0.5f);
            blvFlDrawer.setLayoutParams(lp);
        }
        tvRefresh = f(R.id.blv_tvRefresh);
        blvChildBg = f(R.id.blv_child_bg);
        disPlayViews = new HashMap<>();
        disPlayViews.put(modeDefault, 0.0f);
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintTextSize);
        if (hintTextColor != 0) tvHint.setTextColor(hintTextColor);
        if (maxHintTextWidth > 0) tvHint.setMaxWidth((int) maxHintTextWidth);
        if (maxHintTextLines > 0) {
            tvHint.setMaxLines(maxHintTextLines);
            tvHint.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (refreshTextColor != 0) tvRefresh.setTextColor(refreshTextColor);
        tvRefresh.setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshTextSize);
        if (maxRefreshTextWidth > 0) tvRefresh.setMaxWidth((int) maxRefreshTextWidth);
        if (maxRefreshTextLines > 0) {
            tvRefresh.setMaxLines(maxRefreshTextLines);
            tvRefresh.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (btnEnable) {
            if (btnTextColor != 0) btnRefresh.setTextColor(btnTextColor);
            btnRefresh.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnTextSize);
            btnRefresh.setBackground(btnBg);
        }
        resetUi();
        OverLapMode defaultMode = getMode(shownModeDefault);
    }

    private void resetBackground(OverLapMode mode) {
        setBackground((mode == OverLapMode.OVERLAP || mode == OverLapMode.FO) ? bg : null);
        blvChildBg.setBackground((mode == OverLapMode.FLOATING || mode == OverLapMode.FO) ? bgOnContent : null);
    }

    private final BaseLoadingAnimatorListener listener = new BaseLoadingAnimatorListener() {

        @Override
        public void onDurationChange(ValueAnimator animation, float offset, DisplayMode mode, OverLapMode overLapMode) {
            synchronized (BaseLoadingView.this) {
                onAnimationFraction(animation.getAnimatedFraction(), offset, mode, overLapMode);
            }
        }

        @Override
        public void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode) {
            synchronized (BaseLoadingView.this) {
                onAnimationFraction(1.0f, 1.0f, mode, overLapMode);
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

    //reset LOADING/NO_DATA/NO_NETWORK drawable
    private void resetUi() {
        if (loadingRes != -1) {
            Drawable drawable = getContext().getDrawable(loadingRes);
            if (drawable != null) {
                Rect rect = loading.getIndeterminateDrawable().getBounds();
                drawable.setBounds(rect);
                loading.setIndeterminateDrawable(drawable);
            }
        } else loading.setProgressDrawable(null);
        if (noDataRes != -1) {
            noData.setImageResource(noDataRes);
        }
        if (noNetworkRes != -1) {
            noNetwork.setImageResource(noNetworkRes);
        }
    }

    public void setMode(DisplayMode mode, boolean isSetNow) {
        setMode(mode, "", "", isSetNow);
    }

    public void setMode(DisplayMode mode, String hint, boolean isSetNow) {
        setMode(mode, hint, "");
    }

    public void setMode(DisplayMode mode, OverLapMode overlapMode, boolean isSetNow) {
        setMode(mode, "", "", overlapMode, isSetNow);
    }

    public void setMode(DisplayMode mode, String hint, String subHint, boolean isSetNow) {
        setMode(mode, hint, subHint, null, isSetNow);
    }

    public void setMode(DisplayMode mode, String hint, OverLapMode overlapMode, boolean isSetNow) {
        setMode(mode, hint, "", overlapMode, isSetNow);
    }

    public void setMode(DisplayMode mode) {
        setMode(mode, "", "");
    }

    public void setMode(DisplayMode mode, String hint) {
        setMode(mode, hint, "");
    }

    public void setMode(DisplayMode mode, OverLapMode overlapMode) {
        setMode(mode, "", "", overlapMode, false);
    }

    public void setMode(DisplayMode mode, String hint, String subHint) {
        setMode(mode, hint, subHint, null, false);
    }

    public void setMode(DisplayMode mode, String hint, OverLapMode overlapMode) {
        setMode(mode, hint, "", overlapMode, false);
    }

    public void setMode(DisplayMode mode, String hint, final String subHint, OverLapMode overlapMode) {
        setMode(mode, hint, subHint, overlapMode, false);
    }

    public TextView getHintView() {
        return tvHint;
    }

    public TextView getRefreshView() {
        return tvRefresh;
    }

    public void setMode(final DisplayMode mode, final String hint, final String subHint, final OverLapMode overlapMode, final boolean isSetNow) {
        handler.removeCallbacksAndMessages(null);
        long delay = mode.delay;
        if (valueAnimator != null) valueAnimator.end();
        if (delay > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BaseLoadingView.this.setLoadingMode(mode, hint, subHint, overlapMode, isSetNow);
                }
            }, delay);
        } else {
            BaseLoadingView.this.setLoadingMode(mode, hint, subHint, overlapMode, isSetNow);
        }
        mode.reset();
    }

    /**
     * just call setMode after this View got,
     *
     * @param mode        the current display mode you need;
     * @param overlapMode is showing on content? or hide content?
     * @param hint        show something when it`s change a mode;
     */
    private void setLoadingMode(DisplayMode mode, String hint, String subHint, OverLapMode overlapMode, boolean isSetNow) {
        refreshEnableWithView = refreshEnable && (mode == DisplayMode.NO_DATA || mode == DisplayMode.NO_NETWORK);
        if (overlapMode == null) overlapMode = getMode(shownModeDefault);
        if (mode == DisplayMode.NONE) mode = DisplayMode.NORMAL;
        int newCode = overlapMode.value + mode.value;
        int oldCode = overlapMode.value + oldMode.value;
        oldMode = mode;
        boolean isSameMode = newCode == oldCode;
        String hintText = (!TextUtils.isEmpty(hint) ? hint : getHintString(mode));
        if (!TextUtils.isEmpty(hintText)) {
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(hintText);
        } else tvHint.setVisibility(View.GONE);
        btnRefresh.setVisibility(refreshEnableWithView && btnEnable ? VISIBLE : GONE);
        if (btnEnable) {
            btnRefresh.setText(btnText);
        }
        String refreshHint = null;
        if (mode == DisplayMode.NO_DATA || mode == DisplayMode.NO_NETWORK) {
            refreshHint = mode == DisplayMode.NO_DATA ? refreshNoDataText : refreshNetworkText;
        }
        boolean hasHint = TextUtils.isEmpty(subHint) || TextUtils.isEmpty(refreshHint);
        tvRefresh.setVisibility(hasHint && refreshEnableWithView ? View.VISIBLE : View.GONE);
        if (hasHint) {
            tvRefresh.setText(TextUtils.isEmpty(subHint) ? refreshHint : subHint);
        }
        if (isSameMode) return;
        disPlayViews.put(mode, 0.0f);
        resetBackground(overlapMode);
        if (isSetNow || animateDuration <= 0) {
            if (valueAnimator != null) valueAnimator.end();
            onAnimationFraction(1f, 1f, mode, overlapMode);
        } else {
            if (valueAnimator == null) {
                valueAnimator = new BaseLoadingValueAnimator(listener);
                valueAnimator.setDuration(animateDuration);
            } else {
                valueAnimator.end();
            }
            valueAnimator.start(mode, overlapMode);
        }
        if (onMode != null) {
            onMode.onModeChange(mode);
        }
    }

    private String getHintString(DisplayMode mode) {
        switch (mode) {
            case LOADING:
                return (loadingHint == null || loadingHint.isEmpty()) ? "" : loadingHint;
            case NO_DATA:
                return (noDataHint == null || noDataHint.isEmpty()) ? "" : noDataHint;
            case NO_NETWORK:
                return (networkErrorHint == null || networkErrorHint.isEmpty()) ? "" : networkErrorHint;
            default:
                return "";
        }
    }

    private synchronized void onAnimationFraction(float duration, float offset, DisplayMode curMode, OverLapMode overLapMode) {
        setViews(offset, curMode);
        setBackground(duration, curMode, overLapMode);
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

    private void setBackground(float duration, DisplayMode curMode, OverLapMode overLapMode) {
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
            if (getAlpha() <= 0.05f) {
                setAlpha(0);
                setBackground(null);
                setVisibility(GONE);
            }
        }
    }

    private View getDisplayView(DisplayMode mode) {
        switch (mode) {
            case NO_DATA:
                return noData;
            case LOADING:
                return loading;
            case NO_NETWORK:
                return noNetwork;
        }
        return null;
    }

    private <T extends View> T f(int id) {
        return (T) rootView.findViewById(id);
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
                        listener.onAnimEnd(animation, curMode, overLapMode);
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

        public void setAnimatorListener(BaseLoadingAnimatorListener listener) {
            this.listener = listener;
        }
    }

    public interface BaseLoadingAnimatorListener {

        void onDurationChange(ValueAnimator animation, float duration, DisplayMode mode, OverLapMode overLapMode);

        void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode);
    }
}
