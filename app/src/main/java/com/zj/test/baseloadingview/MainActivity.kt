package com.zj.test.baseloadingview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.zj.test.R

class MainActivity : AppCompatActivity() {

    private var bt_view: Button? = null
    private var bld_view: BaseLoadingView? = null
    private var cb_override: CheckBox? = null
    private var isOverrideBg = true

    private var index: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        bld_view = findViewById(R.id.bld_view)
        bt_view = findViewById(R.id.bt_view)
        cb_override = findViewById<CheckBox>(R.id.cb_override)
        bld_view!!.setRefreshListener {
            Toast.makeText(this, "call refresh with error", Toast.LENGTH_SHORT).show()
        }
        bt_view!!.setOnClickListener {
            bld_view!!.setMode(BaseLoadingView.DisplayMode.values()[index], "", isOverrideBg)
            index++
            if (index == BaseLoadingView.DisplayMode.values().size) index = 0
        }
        cb_override?.setOnCheckedChangeListener({ _, isCheck ->
            isOverrideBg = !isCheck

        })
    }
}
