package com.zj.loading;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private TextView tvHint, tvRefresh;
    protected L loading;
    protected N noData;
    protected E noNetwork;
    private DisplayMode oldMode = DisplayMode.NONE;
    private Map<DisplayMode, Float> disPlayViews;
    private View rootView;
    private ViewGroup contentView, blvFlDrawer;
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
    private float hintTextSize, refreshTextSize, btnTextSize;
    private int viewGravity, hintTextStyle, refreshTextStyle, btnTextStyle;
    private float maxRefreshTextWidth, maxHintTextWidth, refreshTextLineSpacing;
    private float hintMarginTop, hintMarginBottom, btnMarginTop;
    private int maxRefreshTextLines, maxHintTextLines;
    private boolean refreshEnable = true;
    private boolean btnEnable = false;
    private boolean refreshEnableWithView = false;
    protected DisplayMode modeDefault = DisplayMode.NONE;
    private int drawerWidth, drawerHeight, loadingWidth, loadingHeight, noDataWidth, noDataHeight, netErrWidth, netErrHeight;

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
        initAbsViews();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onViewInflated();
        setMode(modeDefault, true);
        setClipChildren(false);
        setClipToPadding(false);
    }

    private BaseLoadingValueAnimator valueAnimator;

    public abstract L inflateLoadingView(float loadingWidth, float loadingHeight);

    public abstract N inflateNoDataView(float noDataWidth, float noDataHeight);

    public abstract E inflateNetworkErrorView(float netErrWidth, float netErrHeight);

    protected abstract void onViewInflated();

    public void onViewVisibilityChanged(int viewId, boolean visible) { }

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
        if (btnEnable) {
            this.setOnClickListener(null);
            btnRefresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (refreshEnable && refreshEnableWithView && ZLoadingView.this.refresh != null) {
                        ZLoadingView.this.refresh.onTap();
                    }
                }
            });
        } else {
            btnRefresh.setOnClickListener(null);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (refreshEnable && refreshEnableWithView && ZLoadingView.this.refresh != null) {
                        ZLoadingView.this.refresh.onTap();
                    }
                }
            });
        }
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
                drawerWidth = array.getDimensionPixelSize(R.styleable.ZLoadingView_drawerWidth, -1);
                drawerHeight = array.getDimensionPixelSize(R.styleable.ZLoadingView_drawerHeight, -1);
                refreshTextColor = array.getColor(R.styleable.ZLoadingView_refreshTextColor, -1);
                loadingHint = array.getString(R.styleable.ZLoadingView_loadingText);
                noDataHint = array.getString(R.styleable.ZLoadingView_noDataText);
                networkErrorHint = array.getString(R.styleable.ZLoadingView_networkErrorText);
                hintTextStyle = array.getInt(R.styleable.ZLoadingView_hintTextStyle, -1);
                refreshTextStyle = array.getInt(R.styleable.ZLoadingView_refreshTextStyle, -1);
                btnTextStyle = array.getInt(R.styleable.ZLoadingView_btnTextStyle, -1);
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
                refreshTextLineSpacing = array.getDimension(R.styleable.ZLoadingView_refreshTextLineSpacing, -1f);
                viewGravity = array.getInt(R.styleable.ZLoadingView_viewGravity, -1);
                hintMarginTop = array.getDimension(R.styleable.ZLoadingView_hintMarginTop, -1);
                hintMarginBottom = array.getDimension(R.styleable.ZLoadingView_hintMarginBottom, -1);
                btnMarginTop = array.getDimension(R.styleable.ZLoadingView_btnMarginTop, -1);

                int lw = 0;
                try {
                    lw = array.getInt(R.styleable.ZLoadingView_loading_width, 0);
                } catch (Exception ignored) {
                }
                int lh = 0;
                try {
                    lh = array.getInt(R.styleable.ZLoadingView_loading_height, 0);
                } catch (Exception ignored) {
                }
                int nw = 0;
                try {
                    nw = array.getInt(R.styleable.ZLoadingView_no_data_width, 0);
                } catch (Exception ignored) {
                }
                int nh = 0;
                try {
                    nh = array.getInt(R.styleable.ZLoadingView_no_data_height, 0);
                } catch (Exception ignored) {
                }
                int ew = 0;
                try {
                    ew = array.getInt(R.styleable.ZLoadingView_network_error_width, 0);
                } catch (Exception ignored) {
                }
                int eh = 0;
                try {
                    eh = array.getInt(R.styleable.ZLoadingView_network_error_height, 0);
                } catch (Exception ignored) {
                }

                loadingWidth = lw == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_loading_width, -2) : lw;
                loadingHeight = lh == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_loading_height, -2) : lh;
                noDataWidth = nw == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_no_data_width, -2) : nw;
                noDataHeight = nh == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_no_data_height, -2) : nh;
                netErrWidth = ew == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_network_error_width, -2) : ew;
                netErrHeight = eh == 0 ? array.getDimensionPixelSize(R.styleable.ZLoadingView_network_error_height, -2) : eh;

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
        rootView = LayoutInflater.from(context).inflate(R.layout.loading_view, this, true);
        contentView = f(R.id.blv_cl_content);
        tvHint = f(R.id.blv_tvHint);
        btnRefresh = f(R.id.blv_btnRefresh);
        tvRefresh = f(R.id.blv_tvRefresh);
        blvChildBg = f(R.id.blv_child_bg);
        blvFlDrawer = f(R.id.blv_fl_drawer);
        if (drawerWidth <= 0) {
            drawerWidth = Math.max(loadingWidth, Math.max(noDataWidth, netErrWidth));
        }
        if (drawerHeight <= 0) {
            drawerHeight = Math.max(loadingHeight, Math.max(noDataHeight, netErrHeight));
        }
        if (drawerWidth > 0 && drawerHeight > 0) {
            ViewGroup.LayoutParams lp = blvFlDrawer.getLayoutParams();
            lp.width = drawerWidth;
            lp.height = drawerHeight;
            blvFlDrawer.setLayoutParams(lp);
            initDrawerCenter();
        }
        buildNewMargins(tvHint, -1, (int) hintMarginTop, -1, -1);
        buildNewMargins(tvRefresh, -1, (int) hintMarginBottom, -1, -1);
        buildNewMargins(btnRefresh, -1, (int) btnMarginTop, -1, -1);
        disPlayViews = new HashMap<>();
        disPlayViews.put(modeDefault, 0.0f);
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_PX, hintTextSize);
        setFontStyle(tvHint, hintTextStyle);
        setFontStyle(tvRefresh, refreshTextStyle);
        setFontStyle(btnRefresh, btnTextStyle);
        if (hintTextColor != 0) tvHint.setTextColor(hintTextColor);
        if (maxHintTextWidth > 0) tvHint.setMaxWidth((int) maxHintTextWidth);
        if (maxHintTextLines > 0) {
            tvHint.setMaxLines(maxHintTextLines);
            tvHint.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (refreshTextColor != 0) tvRefresh.setTextColor(refreshTextColor);
        tvRefresh.setTextSize(TypedValue.COMPLEX_UNIT_PX, refreshTextSize);
        if (refreshTextLineSpacing >= 0) tvRefresh.setLineSpacing(refreshTextLineSpacing, 1f);
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

    private void setFontStyle(TextView v, int style) {
        Typeface typeface = Typeface.DEFAULT;
        if (style >= 0) {
            int fs;
            switch (style) {
                case Typeface.BOLD:
                case Typeface.ITALIC:
                case Typeface.NORMAL:
                case Typeface.BOLD_ITALIC:
                    fs = style;
                    break;
                default:
                    throw new IllegalArgumentException("The font must follow the specified size and be in one of Typeface.BOLD, Typeface.ITALIC, Typeface.NORMAL, Typeface.BOLD_ITALIC");
            }
            typeface = Typeface.create(typeface, fs);
            v.setTypeface(typeface);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void buildNewMargins(View v, int l, int t, int r, int b) {
        if (l + t + r + b > -4) {
            MarginLayoutParams flp = (MarginLayoutParams) v.getLayoutParams();
            int mt = flp.topMargin;
            int mb = flp.bottomMargin;
            int ml = flp.bottomMargin;
            int mr = flp.bottomMargin;
            flp.setMargins(l >= 0 ? l : ml, t >= 0 ? t : mt, r >= 0 ? r : mr, b >= 0 ? b : mb);
            v.setLayoutParams(flp);
        }
    }

    private void initDrawerCenter() {
        int pl = this.getPaddingLeft();
        int pt = this.getPaddingTop();
        int pr = this.getPaddingRight();
        int pb = this.getPaddingBottom();

        /*The Drawer centered setting needs to be moved up (Padding) to center the bottom, and the Padding correction that has been set needs to be considered.*/
        if (pt > drawerHeight) {
            this.setPadding(pl, pt - drawerHeight, pr, pb);
        } else {
            int offset = pt - drawerHeight;
            this.setPadding(pl, 0, pr, -offset);
        }
    }

    private void initAbsViews() {
        blvFlDrawer.removeAllViews();
        if (loading == null) {
            loading = inflateLoadingView(loadingWidth, loadingHeight);
            assert loading != null;
            loading.setId(R.id.blv_loading_stub);
            FrameLayout.LayoutParams lp = resetLayoutParams(loading, loadingWidth, loadingHeight);
            blvFlDrawer.addView(loading, lp);
            loading.setVisibility(View.GONE);
        }
        if (noData == null) {
            noData = inflateNoDataView(noDataWidth, noDataHeight);
            assert noData != null;
            noData.setId(R.id.blv_noData_stub);
            ViewGroup.LayoutParams lp = resetLayoutParams(noData, noDataWidth, noDataHeight);
            blvFlDrawer.addView(noData, lp);
            noData.setVisibility(View.GONE);
        }
        if (noNetwork == null) {
            noNetwork = inflateNetworkErrorView(netErrWidth, netErrHeight);
            assert noNetwork != null;
            noNetwork.setId(R.id.blv_noNetWork_stub);
            ViewGroup.LayoutParams lp = resetLayoutParams(noNetwork, netErrWidth, netErrHeight);
            blvFlDrawer.addView(noNetwork, lp);
            noNetwork.setVisibility(View.GONE);
        }
    }

    private FrameLayout.LayoutParams resetLayoutParams(View view, int width, int height) {
        FrameLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp == null) lp = new FrameLayout.LayoutParams(0, 0);
        lp.width = width == ViewGroup.LayoutParams.MATCH_PARENT ? drawerWidth : width;
        lp.height = height == ViewGroup.LayoutParams.MATCH_PARENT ? drawerHeight : height;
        lp.gravity = viewGravity >= 0 ? viewGravity : Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        return lp;
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
        if (!isSetNow && delay > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ZLoadingView.this.setLoadingMode(mode, hint, subHint, overlapMode, false);
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

    private <T extends View> T f(int id) {
        return rootView.findViewById(id);
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
            int i = left + right + top + bottom;
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
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isInEditMode() && blvChildBg != null) {
            setContentBgSize(true);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int pt = getPaddingTop();
        int pb = getPaddingBottom();
        float paddingOffset = pt - pb;
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

    public interface BaseLoadingAnimatorListener {

        void onDurationChange(ValueAnimator animation, float duration, DisplayMode mode, OverLapMode overLapMode);

        void onAnimEnd(Animator animation, DisplayMode mode, OverLapMode overLapMode);
    }
}
