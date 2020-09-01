package com.zj.test.baseloadingview;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.zj.loading.BaseLoadingView;
import com.zj.loading.OnTapListener;
import com.zj.loading.DisplayMode;
import com.zj.loading.OverLapMode;
import com.zj.test.R;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private BaseLoadingView blvView;
    private int lapModeId = 0;
    private OverLapMode curOverlapMod = OverLapMode.OVERLAP;
    private Toast toast = null;
    private int index;
    private HashMap<DisplayMode, String> hints = new HashMap<>();
    private OverLapMode[] modes = new OverLapMode[]{
            OverLapMode.OVERLAP, OverLapMode.FLOATING, OverLapMode.FO
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        blvView = findViewById(R.id.bld_view);
        Button btView = findViewById(R.id.bt_view);
        ImageView ivBg = findViewById(R.id.iv_bg);
        TextView tvOverride = findViewById(R.id.tv_override);

        hints.put(DisplayMode.LOADING, getString(R.string.loading));
        hints.put(DisplayMode.NO_DATA, getString(R.string.noData));
        hints.put(DisplayMode.NO_NETWORK, getString(R.string.noNetwork));
        hints.put(DisplayMode.NORMAL, "");

        ivBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("zj --- ", "bg.onclick");
            }
        });
        blvView.setOnTapListener(new OnTapListener() {
            @Override
            public void onTap() {
                if (toast == null)
                    toast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);
                toast.show();
                toast.setText("call refresh with error");
            }
        });
        btView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayMode mode = DisplayMode.values()[index];
                blvView.setMode(mode, hints.get(mode), curOverlapMod);
                index++;
                if (index == DisplayMode.values().length) index = 0;
            }
        });
        tvOverride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curOverlapMod = modes[++lapModeId % 3];
            }
        });
    }
}
