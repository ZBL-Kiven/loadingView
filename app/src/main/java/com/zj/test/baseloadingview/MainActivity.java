package com.zj.test.baseloadingview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.zj.loading.BaseLoadingView;
import com.zj.test.R;

import java.util.HashMap;

public class MainActivity extends Activity {

    private BaseLoadingView blvView;
    private boolean isOverrideBg = true;

    private Toast toast = null;
    private int index;

    private HashMap<BaseLoadingView.DisplayMode, String> hints = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        blvView = findViewById(R.id.bld_view);
        Button btView = findViewById(R.id.bt_view);
        ImageView ivBg = findViewById(R.id.iv_bg);
        CheckBox cbOverride = findViewById(R.id.cb_override);

        hints.put(BaseLoadingView.DisplayMode.LOADING, getString(R.string.loading));
        hints.put(BaseLoadingView.DisplayMode.NO_DATA, getString(R.string.noData));
        hints.put(BaseLoadingView.DisplayMode.NO_NETWORK, getString(R.string.noNetwork));
        hints.put(BaseLoadingView.DisplayMode.NORMAL, "");

        ivBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("zj --- ", "bg.onclick");
            }
        });
        blvView.setRefreshListener(new BaseLoadingView.CallRefresh() {
            @Override
            public void onCallRefresh() {
                if (toast == null) toast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
                toast.show();
                toast.setText("call refresh with error");
            }
        });
        btView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseLoadingView.DisplayMode mode = BaseLoadingView.DisplayMode.values()[index];
                blvView.setMode(mode, hints.get(mode), isOverrideBg);
                index++;
                if (index == BaseLoadingView.DisplayMode.values().length) index = 0;
            }
        });
        cbOverride.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isOverrideBg = !b;
            }
        });
    }
}
