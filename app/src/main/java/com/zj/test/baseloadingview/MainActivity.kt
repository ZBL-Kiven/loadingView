package com.zj.test.baseloadingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import com.zj.test.R

class MainActivity : AppCompatActivity() {

    private var bt_view: Button? = null
    private var iv_bg: ImageView? = null
    private var bld_view: BaseLoadingView? = null
    private var cb_override: CheckBox? = null
    private var isOverrideBg = true

    private var toast: Toast? = null;
    private var index: Int = 0

    var hints = HashMap<BaseLoadingView.DisplayMode, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        bld_view = findViewById(R.id.bld_view)
        bt_view = findViewById(R.id.bt_view)
        iv_bg = findViewById(R.id.iv_bg)
        cb_override = findViewById<CheckBox>(R.id.cb_override)

        hints.put(BaseLoadingView.DisplayMode.loading, "加载中,请稍候...")
        hints.put(BaseLoadingView.DisplayMode.noData, "暂无数据")
        hints.put(BaseLoadingView.DisplayMode.noNetwork, "网络连接失败")
        hints.put(BaseLoadingView.DisplayMode.normal, "")

        bld_view!!.setRefreshListener {
            if (toast == null) toast = Toast.makeText(this@MainActivity, "", Toast.LENGTH_SHORT)
            toast?.show()
            toast?.setText("call refresh with error")
        }
        bt_view!!.setOnClickListener {
            var mode = BaseLoadingView.DisplayMode.values()[index]
            bld_view!!.setMode(mode, hints.get(mode), isOverrideBg)
            index++
            if (index == BaseLoadingView.DisplayMode.values().size) index = 0
        }
        cb_override?.setOnCheckedChangeListener({ _, isCheck ->
            isOverrideBg = !isCheck

        })
    }
}
