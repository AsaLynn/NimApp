package com.zxn.netease.nimsdk.business.session.activity

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization.OptionsButton
import com.zxn.netease.nimsdk.business.preference.UserPreferences
import com.zxn.netease.nimsdk.business.session.audio.MessageAudioControl
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment
import com.zxn.netease.nimsdk.common.CommonUtil.isEmpty
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.util.sys.ScreenUtil

abstract class BaseMessageActivity : UI() {
    @JvmField
    protected var sessionId: String? = null
    private var customization: SessionCustomization? = null
    private var messageFragment: MessageFragment? = null
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    protected abstract fun fragment(): MessageFragment?
    protected abstract val contentViewId: Int
    protected abstract fun initToolBar()

    /**
     * 是否开启距离传感器
     */
    protected abstract fun enableSensor(): Boolean
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewId)
        initToolBar()
        parseIntent()
        messageFragment = switchContent(fragment()!!) as MessageFragment
        if (enableSensor()) {
            initSensor()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorManager != null && proximitySensor != null) {
            sensorManager!!.registerListener(
                sensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorManager != null && proximitySensor != null) {
            sensorManager!!.unregisterListener(sensorEventListener)
        }
    }

    override fun onBackPressed() {
        if (messageFragment != null && messageFragment!!.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (messageFragment != null) {
            messageFragment!!.onActivityResult(requestCode, resultCode, data)
        }
        if (customization != null) {
            customization!!.onActivityResult(this, requestCode, resultCode, data)
        }
    }

    private fun parseIntent() {
        val intent = intent
        sessionId = intent.getStringExtra(Extras.EXTRA_ACCOUNT)
        customization =
            intent.getSerializableExtra(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization?
        if (customization != null) {
            addRightCustomViewOnActionBar(this, customization!!.buttons)
        }
    }

    // 添加action bar的右侧按钮及响应事件
    private fun addRightCustomViewOnActionBar(activity: UI, buttons: List<OptionsButton?>) {
        if (isEmpty(buttons)) {
            return
        }
        val toolbar = toolBar ?: return
        val buttonContainer = LayoutInflater
            .from(activity)
            .inflate(R.layout.nim_action_bar_custom_view, null) as LinearLayout
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        for (button in buttons) {
            val imageView = ImageView(activity)
            imageView.setImageResource(button!!.iconId)
            //imageView.setBackgroundResource(R.drawable.nim_nim_action_bar_button_selector);
            imageView.setPadding(ScreenUtil.dip2px(10f), 0, ScreenUtil.dip2px(10f), 0)
            imageView.setOnClickListener { v: View? ->
                button.onClick(
                    this@BaseMessageActivity,
                    v,
                    sessionId
                )
            }
            buttonContainer.addView(imageView, params)
        }
        toolbar.addView(
            buttonContainer,
            Toolbar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.RIGHT or Gravity.CENTER
            )
        )
    }

    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val dis = event.values
            if (0.0f == dis[0]) {
                //靠近，设置为听筒模式
                MessageAudioControl.getInstance(this@BaseMessageActivity)
                    .setEarPhoneModeEnable(true)
            } else {
                //离开，复原
                MessageAudioControl.getInstance(this@BaseMessageActivity).setEarPhoneModeEnable(
                    UserPreferences.isEarPhoneModeEnable()
                )
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun initSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        }
    }
}