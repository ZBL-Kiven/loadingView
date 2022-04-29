package com.zj.test.baseloadingview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.zj.loading.ZLoadingView;

public class CusLoadingView extends ZLoadingView<SurfaceView, ImageView, TextView> {

    public CusLoadingView(Context context) {
        super(context);
    }

    public CusLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void inflateLoadingView(ViewStub stub, float drawerWidth, float drawerHeight) {

    }

    @Override
    public void inflateNoDataView(ViewStub stub, float drawerWidth, float drawerHeight) {

    }

    @Override
    public void inflateNetworkErrorView(ViewStub stub, float drawerWidth, float drawerHeight) {

    }

    @Override
    protected void onViewInflated() {

    }
}
