package com.zj.loading;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ZRotateLoadingView extends BaseLoadingView<ImageView, ImageView, ImageView> implements ValueAnimator.AnimatorUpdateListener {

    private int noDataRes = -1, noNetworkRes = -1, loadingRes = -1, duration = 1000;
    private ValueAnimator anim;

    public ZRotateLoadingView(Context context) {
        this(context, null, 0);
    }

    public ZRotateLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZRotateLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initView();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ZRotateLoadingView);
        try {
            loadingRes = ta.getResourceId(R.styleable.ZRotateLoadingView_rotate_loadingRes, R.mipmap.blv_loading_white);
            duration = ta.getInt(R.styleable.ZRotateLoadingView_rotate_Duration, duration);
            noDataRes = ta.getResourceId(R.styleable.ZRotateLoadingView_rotate_noDataRes, R.mipmap.blv_no_data_white);
            noNetworkRes = ta.getResourceId(R.styleable.ZRotateLoadingView_rotate_noNetworkRes, R.mipmap.blv_network_error_white);
        } finally {
            ta.recycle();
        }
    }

    private void initView() {
        loading.setImageResource(loadingRes);
        noData.setImageResource(noDataRes);
        noNetwork.setImageResource(noNetworkRes);
    }

    @Override
    public void inflateLoadingView(ViewStub stub, float drawerWidth, float drawerHeight) {
        stub.setLayoutResource(R.layout.blv_simple_iv);
        stub.inflate();
    }

    @Override
    public void inflateNoDataView(ViewStub stub, float drawerWidth, float drawerHeight) {
        stub.setLayoutResource(R.layout.blv_simple_iv);
        stub.inflate();
    }

    @Override
    public void inflateNetworkErrorView(ViewStub stub, float drawerWidth, float drawerHeight) {
        stub.setLayoutResource(R.layout.blv_simple_iv);
        stub.inflate();
    }

    @Override
    public void onViewVisibilityChanged(int viewId, boolean visible) {
        if (viewId == loading.getId()) {
            if (visible) {
                initAndStartAnim();
            } else {
                stopAnim();
            }
        }
    }

    private void initAndStartAnim() {
        if (anim == null) {
            anim = ValueAnimator.ofFloat(0.0f, 1.0f);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setDuration(duration);
            anim.addUpdateListener(this);
        }
        if (!anim.isStarted()) anim.start();
    }


    private void stopAnim() {
        if (anim != null) {
            anim.removeAllUpdateListeners();
            anim.end();
            anim.cancel();
        }
        anim = null;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (loading.getVisibility() == View.VISIBLE) {
            float fraction = animation.getAnimatedFraction();
            float curRotate = fraction * 360.0f;
            loading.setRotation(curRotate);
        }
    }
}
