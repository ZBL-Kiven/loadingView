package com.zj.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * @author ZJJ on 2018/7/3.
 */
@SuppressWarnings("unused")
public abstract class ZLoadingView<L extends View, N extends View, E extends View> extends ZLoading {

    private TextView tvHint, tvRefresh, btnRefresh;
    protected L loading;
    protected N noData;
    protected E noNetwork;
    private ViewGroup blvFlDrawer;
    private Drawable btnBg;
    private int hintTextColor, refreshTextColor, btnTextColor;
    private String loadingHint = "", noDataHint = "", networkErrorHint = "", refreshNoDataText = "", refreshNetworkText = "", btnText = "";
    private float hintTextSize, refreshTextSize, btnTextSize;
    private int viewGravity, hintTextStyle, refreshTextStyle, btnTextStyle;
    private float maxRefreshTextWidth, maxHintTextWidth, refreshTextLineSpacing;
    private float hintMarginTop, hintMarginBottom, btnMarginTop;
    private float btnPl, btnPt, btnPr, btnPb;
    private int maxRefreshTextLines, maxHintTextLines;
    private boolean btnEnable = false;
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
        initViews();
        initAbsViews();
    }

    public abstract L inflateLoadingView(float loadingWidth, float loadingHeight);

    public abstract N inflateNoDataView(float noDataWidth, float noDataHeight);

    public abstract E inflateNetworkErrorView(float netErrWidth, float netErrHeight);

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZLoadingView);
            try {
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
                refreshNoDataText = array.getString(R.styleable.ZLoadingView_refreshNoDataText);
                refreshNetworkText = array.getString(R.styleable.ZLoadingView_refreshNetworkText);
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
                btnPl = array.getDimension(R.styleable.ZLoadingView_btnPaddingLeft, -1);
                btnPt = array.getDimension(R.styleable.ZLoadingView_btnPaddingTop, -1);
                btnPr = array.getDimension(R.styleable.ZLoadingView_btnPaddingRight, -1);
                btnPb = array.getDimension(R.styleable.ZLoadingView_btnPaddingBottom, -1);

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

                if (btnEnable) {
                    btnBg = array.getDrawable(R.styleable.ZLoadingView_btnBackground);
                    btnText = array.getString(R.styleable.ZLoadingView_btnText);
                    btnTextSize = array.getDimension(R.styleable.ZLoadingView_btnTextSize, 36f);
                    btnTextColor = array.getColor(R.styleable.ZLoadingView_btnTextColor, Color.BLACK);
                }
            } finally {
                array.recycle();
            }
        }
    }

    @Override
    protected View inflateContentView(ViewStub vs) {
        vs.setLayoutResource(R.layout.loading_view_content);
        return vs.inflate();
    }

    private void initViews() {
        tvHint = f(R.id.blv_tvHint);
        btnRefresh = f(R.id.blv_btnRefresh);
        tvRefresh = f(R.id.blv_tvRefresh);
        blvFlDrawer = f(R.id.blv_fl_drawer);
        if (drawerWidth <= 0) {
            drawerWidth = Math.max(loadingWidth, Math.max(noDataWidth, netErrWidth));
        }
        if (drawerHeight <= 0) {
            drawerHeight = Math.max(loadingHeight, Math.max(noDataHeight, netErrHeight));
        }
        if (btnPl + btnPt + btnPr + btnPb > -4) {
            int pl = btnRefresh.getPaddingStart();
            int pt = btnRefresh.getPaddingTop();
            int pr = btnRefresh.getPaddingRight();
            int pb = btnRefresh.getPaddingBottom();
            btnRefresh.setPadding(btnPl >= 0 ? (int) btnPl : pl, btnPt >= 0 ? (int) btnPt : pt, btnPr >= 0 ? (int) btnPr : pr, btnPb >= 0 ? (int) btnPb : pb);
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
    }

    @Override
    protected void onMode(DisplayMode mode, boolean isSameMode, boolean viewEnable) {
        String hintText = getHintString(mode);
        if (!TextUtils.isEmpty(hintText)) {
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(hintText);
        } else tvHint.setVisibility(View.INVISIBLE);
        btnRefresh.setVisibility(viewEnable && btnEnable ? VISIBLE : INVISIBLE);
        if (btnEnable) {
            btnRefresh.setText(btnText);
        }
        String refreshHint = null;
        if (mode == DisplayMode.NO_DATA || mode == DisplayMode.NO_NETWORK) {
            refreshHint = mode == DisplayMode.NO_DATA ? refreshNoDataText : refreshNetworkText;
        }
        boolean hasHint = !TextUtils.isEmpty(refreshHint);
        tvRefresh.setVisibility(hasHint && viewEnable ? View.VISIBLE : View.INVISIBLE);
        if (hasHint) {
            tvRefresh.setText(refreshHint);
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

    @Override
    protected View getDisplayView(DisplayMode mode) {
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
}
