package com.netease.nim.demo.session

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.netease.nim.demo.location.activity.LocationAmapActivity
import com.netease.nim.demo.location.activity.LocationExtras
import com.netease.nim.demo.location.activity.NavigationAmapActivity
import com.netease.nim.demo.location.helper.NimLocationManager
import com.zxn.netease.nimsdk.api.model.location.LocationProvider
import com.zxn.netease.nimsdk.common.ui.dialog.EasyAlertDialog
import com.zxn.netease.nimsdk.common.util.log.LogUtil

class NimDemoLocationProvider : LocationProvider {
    override fun requestLocation(context: Context?, callback: LocationProvider.Callback?) {
        if (!NimLocationManager.isLocationEnable(context)) {
            val alertDialog = EasyAlertDialog(context)
            alertDialog.setMessage("位置服务未开启")
            alertDialog.addNegativeButton(
                "取消", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE.toFloat()
            ) { alertDialog.dismiss() }
            alertDialog.addPositiveButton(
                "设置", EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE.toFloat()
            ) {
                alertDialog.dismiss()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                try {
                    context!!.startActivity(intent)
                } catch (e: Exception) {
                    LogUtil.e("LOC", "start ACTION_LOCATION_SOURCE_SETTINGS error")
                }
            }
            alertDialog.show()
            return
        }
        LocationAmapActivity.start(context, callback)
    }

    override fun openMap(context: Context?, longitude: Double, latitude: Double, address: String?) {
        val intent = Intent(context, NavigationAmapActivity::class.java)
        intent.putExtra(LocationExtras.LONGITUDE, longitude)
        intent.putExtra(LocationExtras.LATITUDE, latitude)
        intent.putExtra(LocationExtras.ADDRESS, address)
        context!!.startActivity(intent)
    }
}