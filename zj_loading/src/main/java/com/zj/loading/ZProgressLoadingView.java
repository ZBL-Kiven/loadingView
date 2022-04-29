package com.zj.loading;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;

@SuppressWarnings("unused")
public class ZProgressLoadingView extends BaseLoadingView<ProgressBar, ImageView, ImageView> {

    private int noDataRes = -1, noNetworkRes = -1;
    private int progressColor = Color.WHITE;

    public ZProgressLoadingView(Context context) {
        this(context, null, -1);
    }

    public ZProgressLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ZProgressLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initViews();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ZProgressLoadingView);
        try {
            progressColor = ta.getColor(R.styleable.ZProgressLoadingView_progress_color, progressColor);
            noDataRes = ta.getResourceId(R.styleable.ZProgressLoadingView_progress_noDataRes, R.mipmap.blv_no_data_white);
            noNetworkRes = ta.getResourceId(R.styleable.ZProgressLoadingView_progress_noNetworkRes, R.mipmap.blv_network_error_white);
        } finally {
            ta.recycle();
        }
    }

    private void initViews() {
//        loading.setIndeterminate(true);
//        loading.setIndeterminateTintMode(PorterDuff.Mode.CLEAR);
//        loading.setIndeterminateTintList(ColorStateList.valueOf(progressColor));
        noData.setImageResource(noDataRes);
        noNetwork.setImageResource(noNetworkRes);
    }

    @Override
    public void inflateLoadingView(ViewStub stub, float drawerWidth, float drawerHeight) {
        stub.setLayoutResource(R.layout.blv_simple_pb);
        ProgressBar pb = (ProgressBar) stub.inflate();
//        pb.setIndeterminate(true);
//        Drawable drawable = new ColorDrawable(Color.CYAN);
//        Rect bounds = pb.getIndeterminateDrawable().getBounds();
//        drawable.setBounds(bounds);
//        pb.setIndeterminateDrawable(drawable);
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
}
