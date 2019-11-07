package com.zj.test.baseloadingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import com.zj.loading.BaseLoadingView
import com.zj.test.R

class MainActivity : AppCompatActivity() {

    private var btView: Button? = null
    private var ivBg: ImageView? = null
    private var blvView: BaseLoadingView? = null
    private var cbOverride: CheckBox? = null
    private var isOverrideBg = true

    private var toast: Toast? = null
    private var index: Int = 0

    private var hints = HashMap<BaseLoadingView.DisplayMode, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        blvView = findViewById(R.id.bld_view)
        btView = findViewById(R.id.bt_view)
        ivBg = findViewById(R.id.iv_bg)
        cbOverride = findViewById<CheckBox>(R.id.cb_override)

        hints[BaseLoadingView.DisplayMode.loading] = getString(R.string.loading)
        hints[BaseLoadingView.DisplayMode.noData] = getString(R.string.noData)
        hints[BaseLoadingView.DisplayMode.noNetwork] = getString(R.string.noNetwork)
        hints[BaseLoadingView.DisplayMode.normal] = ""

        ivBg?.setOnClickListener {
            Log.e("zj --- ", "bg.onclick")
        }

        blvView?.setRefreshListener {
            if (toast == null) toast = Toast.makeText(this@MainActivity, "", Toast.LENGTH_SHORT)
            toast?.show()
            toast?.setText("call refresh with error")
        }
        btView?.setOnClickListener {
            val mode = BaseLoadingView.DisplayMode.values()[index]
            blvView!!.setMode(mode, hints[mode], isOverrideBg)
            index++
            if (index == BaseLoadingView.DisplayMode.values().size) index = 0
        }
        cbOverride?.setOnCheckedChangeListener { _, isCheck ->
            isOverrideBg = !isCheck
        }
    }
}
