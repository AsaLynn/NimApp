package com.netease.nim.demo.msg

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.avsignalling.SignallingService
import com.netease.nimlib.sdk.avsignalling.builder.CallParamBuilder
import com.netease.nimlib.sdk.avsignalling.constant.ChannelType
import com.netease.nimlib.sdk.avsignalling.model.ChannelBaseInfo
import com.netease.nimlib.sdk.avsignalling.model.ChannelFullInfo
import com.zxn.mvvm.viewmodel.BaseViewModel
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.common.ToastHelper


/**
 * Copyright(c) ${}YEAR} ZhuLi co.,Ltd All Rights Reserved.
 *
 * @className: MsgViewModel$
 * @description: TODO 类描述
 * @version: v0.0.1
 * @author: zxn < a href=" ">zhangxiaoning@17biyi.com</ a>
 * @date: 2020/12/24$ 20:36$
 * @updateUser: 更新者：
 * @updateDate: 2020/12/24$ 20:36$
 * @updateRemark: 更新说明：
 * @version: 1.0
 * */
class MsgViewModel : BaseViewModel<Nothing>() {

    companion object {
        private const val TAG = "MsgViewModel"
    }

    /**
     * 呼叫对方
     */
    fun call(context: Context, account: String?) {
        val requestId = NimUIKit.getAccount()
        val paramBuilder =
            CallParamBuilder(ChannelType.AUDIO, account, requestId);
        //paramBuilder.selfUid(selfUid)
        NIMClient.getService(SignallingService::class.java).call(paramBuilder).setCallback(
            object : RequestCallbackWrapper<ChannelFullInfo>() {
                override fun onResult(code: Int, result: ChannelFullInfo?, throwable: Throwable?) {
                    Log.i(TAG, "onResult: $code")
                    if (code.toShort() == ResponseCode.RES_SUCCESS) {
                        channelInfo = result?.channelBaseInfo
                        ToastHelper.showToast(context, "邀请成功，等待对方接听")
                    } else {
                        ToastHelper.showToast(
                            context, ("邀请返回的结果 ， code = $code" +
                                    if (throwable == null) "" else ", throwable = ")
                        )
                    }
                }
            })
    }

    var channelInfo: ChannelBaseInfo? = null

    /**
     * 创建频道.
     */
    /**
     * 创建房间:
     */
    fun create(activity: Context?, roomId: String) {
        if (TextUtils.isEmpty(roomId)) {
            showToast(activity, "请输入房间号码")
            return
        }
        NIMClient.getService(SignallingService::class.java).create(ChannelType.AUDIO, roomId, "")
            .setCallback(
                object : RequestCallbackWrapper<ChannelBaseInfo>() {
                    override fun onResult(
                        i: Int, channelBaseInfo: ChannelBaseInfo?, throwable: Throwable?
                    ) {
                        Log.i(TAG, "onResult: $i")
                        if (i.toShort() == ResponseCode.RES_SUCCESS) {
                            channelInfo = channelBaseInfo
                            showToast(activity, "创建成功")
                        } else {
                            showToast(
                                activity, ("创建失败， code = $i" +
                                        if (throwable == null) "" else ", throwable = " +
                                                throwable.message)
                            )

                        }
                    }
                })
    }

    /**
     * 吐司.
     */
    private fun showToast(context: Context?, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}