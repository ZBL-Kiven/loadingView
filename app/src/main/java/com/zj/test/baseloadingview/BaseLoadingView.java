package com.zj.test.baseloadingview;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.zj.test.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaojie on 2018/7/3.
 */

@SuppressWarnings("unused")
public class BaseLoadingView extends FrameLayout {

    public BaseLoadingView(@NonNull Context context) {
        this(context, null, 0);
    }

    public BaseLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseLoadingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private static final int defaultBgColor = 0xfff4f4f4;
    private static final int defaultBgColorOnAct = 0x30000000;
    private static final int defaultAnimationDuration = 400;

    private DisplayMode curMode = DisplayMode.normal;
    private ValueAnimator valueAnimator;
    private List<View> disPlayViews;

    private View rootView;
    private View noData, noNetwork;
    private ProgressBar loading;
    private TextView tvHint, tvRefresh;
    private CallRefresh refresh;
    private int bgColor;
    private int bgColorOnAct;
    private int needBackgroundColor, curBackgroundColor, oldBackgroundColor;
    private int noDataRes = -1;
    private int noNetworkRes = -1;
    private int loadingRes = -1;
    private ArgbEvaluator argbEvaluator;
    private boolean refreshEnable = true;
    private boolean refreshEnableWithView = false;

    public void setRefreshEnable(boolean enable) {
        this.refreshEnable = enable;
    }

    public interface CallRefresh {
        void onCallRefresh();
    }

    public enum DisplayMode {
        loading, noData, noNetwork, normal
    }

    private enum ShowOnAct {
        TRUE, FALSE, NONE
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
            if (array != null) {
                try {
                    bgColor = array.getColor(R.styleable.BaseLoadingView_backgroundFill, defaultBgColor);
                    bgColorOnAct = array.getColor(R.styleable.BaseLoadingView_backgroundOnAct, defaultBgColorOnAct);
                    noDataRes = array.getResourceId(R.styleable.BaseLoadingView_noDataRes, -1);
                    noNetworkRes = array.getResourceId(R.styleable.BaseLoadingView_noNetworkRes, -1);
                    loadingRes = array.getResourceId(R.styleable.BaseLoadingView_loadingRes, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    array.recycle();
                }
            }
        }
        initView(context);
    }

    private void initView(Context context) {
        rootView = inflate(context, R.layout.base_loading_view, this);
        noData = f(R.id.blv_vNoData);
        noNetwork = f(R.id.blv_vNoNetwork);
        loading = f(R.id.blv_pb);
        tvHint = f(R.id.blv_tvHint);
        tvRefresh = f(R.id.blv_tvRefresh);
        disPlayViews = new ArrayList<>();
        disPlayViews.add(loading);
        disPlayViews.add(noData);
        disPlayViews.add(noNetwork);
        resetUi();
        argbEvaluator = new ArgbEvaluator();
        valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setDuration(defaultAnimationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                onAnimationFraction(animator.getAnimatedFraction());
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * @param drawableRes must be an animatorDrawable in progressBar;
     * @link call resetUi() after set this
     */
    public BaseLoadingView setLoadingDrawable(@DrawableRes int drawableRes) {
        this.loadingRes = drawableRes;
        return this;
    }

    //call resetUi() after set this
    public BaseLoadingView setNoDataDrawable(@DrawableRes int drawableRes) {
        this.noDataRes = drawableRes;
        return this;
    }

    //call resetUi() after set this
    public BaseLoadingView setNoNetworkDrawable(@DrawableRes int drawableRes) {
        this.noNetworkRes = drawableRes;
        return this;
    }

    //reset loading/noData/noNetwork drawable
    private void resetUi() {
        if (loadingRes > 0) {
            Drawable drawable = ContextCompat.getDrawable(getContext(), loadingRes);
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

    /**
     * just call setMode after this View got,
     *
     * @param mode      the current display mode you need;
     * @param showOnAct is showing on content? or hide content?
     * @param hint      show something when it`s change a mode;
     */
    public void setMode(DisplayMode mode, String hint, boolean showOnAct) {
        if (mode == curMode) return;
        needBackgroundColor = showOnAct ? bgColorOnAct : bgColor;
        this.curMode = mode;
        curOffset = 0;
        if (!TextUtils.isEmpty(hint))
            tvHint.setText(hint);
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
        refreshEnableWithView = (mode == DisplayMode.noData) || (mode == DisplayMode.noNetwork);
        tvRefresh.setVisibility(refreshEnableWithView ? View.VISIBLE : View.GONE);
    }


    private float curOffset = 0;

    private void onAnimationFraction(float animatedFraction) {
        float offset = animatedFraction - curOffset;
        //is need drawing background?
        if (curMode != DisplayMode.normal) {
            if (getVisibility() != VISIBLE) {
                setAlpha(0);
                setVisibility(VISIBLE);
            }
            if (getAlpha() < 1) {
                float nextAlpha = getAlpha() + offset;
                setAlpha(Math.min(nextAlpha, 1f));
                if (oldBackgroundColor != needBackgroundColor) {
                    setBackgroundColor(needBackgroundColor);
                    oldBackgroundColor = needBackgroundColor;
                }
            } else {
                if (oldBackgroundColor != needBackgroundColor) {
                    int curBackgroundColor = (int) argbEvaluator.evaluate(animatedFraction, oldBackgroundColor, needBackgroundColor);
                    oldBackgroundColor = curBackgroundColor;
                    setBackgroundColor(curBackgroundColor);
                }
            }
        } else {
            if (getAlpha() > 0) {
                float nextAlpha = getAlpha() - offset;
                setAlpha(Math.max(nextAlpha, 0));
            } else {
                setBackgroundColor(oldBackgroundColor = curBackgroundColor = 0);
                setVisibility(GONE);
            }
        }
        //drawing hint icons
        View curSetView = getDisplayView(curMode);
        for (View v : disPlayViews) {
            if (v != curSetView) {
                float nextAlpha = v.getAlpha() - offset;
                if (v.getVisibility() != GONE)
                    v.setAlpha(Math.max(nextAlpha, 0));
                if (nextAlpha <= 0 && v.getVisibility() != GONE) {
                    v.setVisibility(GONE);
                }
            } else {
                if (v != null) {
                    if (v.getVisibility() != VISIBLE) v.setVisibility(VISIBLE);
                    float nextAlpha = v.getAlpha() + offset;
                    if (nextAlpha <= 1)
                        v.setAlpha(nextAlpha);
                }
            }
        }
        curOffset = animatedFraction;
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
}
