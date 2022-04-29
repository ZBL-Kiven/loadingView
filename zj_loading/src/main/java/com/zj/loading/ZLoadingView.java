package com.zj.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZJJ on 2018/7/3.
 */
@SuppressWarnings("unused")
public abstract class ZLoadingView<L extends View, N extends View, E extends View> extends FrameLayout {

    public ZLoadingView(Context context) {
        this(context, null, 0);
    }

    public ZLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
        initView(context);
        //noinspection NullableProblems
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 998) {
                    setContentBgSize();
                } else {
                    onViewVisibilityChanged(msg.what, msg.arg1 == 0);
                }
            }
        };
        post(new Runnable() {
            @Override
            public void run() {
                initAbsViews();
            }
        });
    }

    private TextView tvHint, tvRefresh;
    private ViewStub noDataStub, noNetworkStub, loadingStub;
    protected L loading;
    protected N noData;
    protected E noNetwork;
    private DisplayMode oldMode = DisplayMode.NONE;
    private Map<DisplayMode, Float> disPlayViews;
    private View rootView;
    private ViewGroup contentView;
    private View blvChildBg;
    private float cbLeftPadding, cbTopPadding, cbRightPadding, cbBottomPadding;
    private Button btnRefresh;
    private OnTapListener refresh;
    private OnChangeListener onMode;
    private final Handler handler;
    private Drawable bg, bgOnContent, btnBg;
    private int animateDuration = 0;
    private int shownModeDefault = 0;
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

    public abstract void inflateLoadingView(ViewStub stub, float drawerWidth, float drawerHeight);

    public abstract void inflateNoDataView(ViewStub stub, float drawerWidth, float drawerHeight);

    public abstract void inflateNetworkErrorView(ViewStub stub, float drawerWidth, float drawerHeight);

    protected abstract void onViewInflated();

    public void onViewVisibilityChanged(int viewId, boolean visible) {
    }

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
                if (refreshEnable && refreshEnableWithView && ZLoadingView.this.refresh != null) {
                    ZLoadingView.this.refresh.onTap();
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
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZLoadingView);
            try {
                bg = array.getDrawable(R.styleable.ZLoadingView_backgroundFill);
                refreshTextSize = array.getDimension(R.styleable.ZLoadingView_refreshTextSize, 48f);
                drawerWidth = array.getDimension(R.styleable.ZLoadingView_drawerWidth, -1);
                drawerHeight = array.getDimension(R.styleable.ZLoadingView_drawerHeight, -1);
                refreshTextColor = array.getColor(R.styleable.ZLoadingView_refreshTextColor, -1);
                loadingHint = array.getString(R.styleable.ZLoadingView_loadingText);
                noDataHint = array.getString(R.styleable.ZLoadingView_noDataText);
                networkErrorHint = array.getString(R.styleable.ZLoadingView_networkErrorText);
                shownModeDefault = array.getInt(R.styleable.ZLoadingView_shownMode, 0);
                refreshEnable = array.getBoolean(R.styleable.ZLoadingView_refreshEnable, true);
                refreshNoDataText = array.getString(R.styleable.ZLoadingView_refreshNoDataText);
                refreshNetworkText = array.getString(R.styleable.ZLoadingView_refreshNetworkText);
                animateDuration = array.getInt(R.styleable.ZLoadingView_changeAnimDuration, 400);
                hintTextColor = array.getColor(R.styleable.ZLoadingView_hintColor, -1);
                hintTextSize = array.getDimension(R.styleable.ZLoadingView_hintTextSize, 24f);
                btnEnable = array.getBoolean(R.styleable.ZLoadingView_btnEnable, false);
                maxRefreshTextWidth = array.getDimension(R.styleable.ZLoadingView_maxRefreshTextWidth, -1);
                maxRefreshTextLines = array.getInt(R.styleable.ZLoadingView_maxRefreshTextLines, -1);
                maxHintTextWidth = array.getDimension(R.styleable.ZLoadingView_maxHintTextWidth, -1);
                maxHintTextLines = array.getInt(R.styleable.ZLoadingView_maxHintTextLines, -1);

                bgOnContent = array.getDrawable(R.styleable.ZLoadingView_backgroundUnderTheContent);
                float contentPadding = array.getDimension(R.styleable.ZLoadingView_contentPadding, 0f);
                cbLeftPadding = array.getDimension(R.styleable.ZLoadingView_contentPaddingStart, contentPadding);
                cbRightPadding = array.getDimension(R.styleable.ZLoadingView_contentPaddingEnd, contentPadding);
                cbTopPadding = array.getDimension(R.styleable.ZLoadingView_contentPaddingTop, contentPadding);
                cbBottomPadding = array.getDimension(R.styleable.ZLoadingView_contentPaddingBottom, contentPadding);

                if (btnEnable) {
                    btnBg = array.getDrawable(R.styleable.ZLoadingView_btnBackground);
                    btnText = array.getString(R.styleable.ZLoadingView_btnText);
                    btnTextSize = array.getDimension(R.styleable.ZLoadingView_btnTextSize, 36f);
                    btnTextColor = array.getColor(R.styleable.ZLoadingView_btnTextColor, Color.BLACK);
                }
                int mode = array.getInt(R.styleable.ZLoadingView_modeDefault, DisplayMode.NONE.value);
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
        rootView = View.inflate(context, R.layout.loading_view, this);
        contentView = f(R.id.blv_cl_content);
        noDataStub = f(R.id.blv_vNoData_stub);
        noNetworkStub = f(R.id.blv_vNoNetwork_stub);
        loadingStub = f(R.id.blv_loading_stub);
        tvHint = f(R.id.blv_tvHint);
        btnRefresh = f(R.id.blv_btnRefresh);
        tvRefresh = f(R.id.blv_tvRefresh);
        blvChildBg = f(R.id.blv_child_bg);
        View blvFlDrawer = f(R.id.blv_fl_drawer);
        if (drawerWidth > 0 && drawerHeight > 0) {
            ViewGroup.LayoutParams lp = blvFlDrawer.getLayoutParams();
            lp.width = (int) (drawerWidth + 0.5f);
            lp.height = (int) (drawerHeight + 0.5f);
            blvFlDrawer.setLayoutParams(lp);
        }
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
        if (modeDefault != DisplayMode.NORMAL && modeDefault != DisplayMode.NONE) {
            OverLapMode defaultMode = getMode(shownModeDefault);
            resetBackground(defaultMode);
        }
    }

    private void initAbsViews() {
        if (loading == null) {
            int inflated = loadingStub.getInflatedId();
            inflateLoadingView(loadingStub, drawerWidth, drawerHeight);
            loading = f(inflated);
            loading.setVisibility(View.GONE);
        }
        if (noData == null) {
            int inflated = noDataStub.getInflatedId();
            inflateNoDataView(noDataStub, drawerWidth, drawerHeight);
            noData = f(inflated);
            noData.setVisibility(View.GONE);
        }
        if (noNetwork == null) {
            int inflated = noNetworkStub.getInflatedId();
            inflateNetworkErrorView(noNetworkStub, drawerWidth, drawerHeight);
            noNetwork = f(inflated);
            noNetwork.setVisibility(View.GONE);
        }
        onViewInflated();
        setMode(modeDefault, true);
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
            synchronized (ZLoadingView.this) {
                onAnimationFraction(animation.getAnimatedFraction(), offset, mode);
            }
        }

        @Override
        public void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode) {
            synchronized (ZLoadingView.this) {
                onAnimationFraction(1.0f, 1.0f, mode);
            }
        }
    };

    public void setMode(DisplayMode mode, boolean isSetNow) {
        setMode(mode, "", "", isSetNow);
    }

    public void setMode(DisplayMode mode, String hint, boolean isSetNow) {
        setMode(mode, hint, "", isSetNow);
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
                    ZLoadingView.this.setLoadingMode(mode, hint, subHint, overlapMode, isSetNow);
                }
            }, delay);
        } else {
            ZLoadingView.this.setLoadingMode(mode, hint, subHint, overlapMode, isSetNow);
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
        } else tvHint.setVisibility(View.INVISIBLE);
        btnRefresh.setVisibility(refreshEnableWithView && btnEnable ? VISIBLE : INVISIBLE);
        if (btnEnable) {
            btnRefresh.setText(btnText);
        }
        String refreshHint = null;
        if (mode == DisplayMode.NO_DATA || mode == DisplayMode.NO_NETWORK) {
            refreshHint = mode == DisplayMode.NO_DATA ? refreshNoDataText : refreshNetworkText;
        }
        boolean hasHint = TextUtils.isEmpty(subHint) || TextUtils.isEmpty(refreshHint);
        tvRefresh.setVisibility(hasHint && refreshEnableWithView ? View.VISIBLE : View.INVISIBLE);
        if (hasHint) {
            tvRefresh.setText(TextUtils.isEmpty(subHint) ? refreshHint : subHint);
        }
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

    @SuppressWarnings("unchecked")
    private <T extends View> T f(int id) {
        return (T) rootView.findViewById(id);
    }

    private void onViewStateChanged(View view, boolean visible) {
        handler.removeMessages(view.getId());
        Message msg = Message.obtain();
        msg.what = view.getId();
        msg.arg1 = visible ? 0 : 1;
        handler.sendMessage(msg);
    }

    private void setContentBgSize() {
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
        if (left + right + top + bottom > 0) {
            LayoutParams lp = (LayoutParams) blvChildBg.getLayoutParams();
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
            Log.e("------- ", "" + left + "   " + top + "   " + right + "   " + bottom);
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
    }

    public interface BaseLoadingAnimatorListener {

        void onDurationChange(ValueAnimator animation, float duration, DisplayMode mode, OverLapMode overLapMode);

        void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode);
    }
}