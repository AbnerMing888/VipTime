package com.abner.time

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vip.time.setInterval
import com.vip.time.setIntervalWireless
import com.vip.time.setTimeDown
import com.vip.time.setTimeOut

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var mTvTimeOut: TextView? = null
    private var mTvTimeDown: TextView? = null
    private var mTvInterval: TextView? = null
    private var mTvIntervalWireless: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_time_out).setOnClickListener(this)
        findViewById<Button>(R.id.btn_time_down).setOnClickListener(this)
        findViewById<Button>(R.id.btn_interval).setOnClickListener(this)
        findViewById<Button>(R.id.btn_interval_wireless).setOnClickListener(this)

        mTvTimeOut = findViewById(R.id.tv_time_out)
        mTvTimeDown = findViewById(R.id.tv_time_down)
        mTvInterval = findViewById(R.id.tv_interval)
        mTvIntervalWireless = findViewById(R.id.tv_interval_wireless)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_time_out -> {
                //倒计时（延时）
                setTimeOut(5) {
                    mTvTimeOut?.text = "倒计时完成"
                }
            }
            R.id.btn_time_down -> {
                //倒计时（延时）返回时间
                setTimeDown(5) {
                    if (it == end) {
                        mTvTimeDown?.text = "倒计时完成"
                    } else {
                        mTvTimeDown?.text = it.toString()
                    }
                }
            }
            R.id.btn_interval -> {
                //定时 10秒，每隔两秒返回一次
                setInterval(10, 2) {
                    if (it == end) {
                        mTvInterval?.text = "定时完成"
                    } else {
                        mTvInterval?.text = it.toString()
                    }
                }
            }
            R.id.btn_interval_wireless -> {
                //无限定时 1秒轮询一次
                setIntervalWireless(1) {
                    mTvIntervalWireless?.text = it.toString()
                }
            }
        }
    }

}