package com.zxn.netease.nimsdk.support.glide

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.nos.NosService
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.common.framework.NimSingleThreadExecutor
import com.zxn.netease.nimsdk.common.ui.imageview.HeadImageView
import java.util.concurrent.TimeUnit

/**
 * 图片缓存管理组件
 */
class ImageLoaderKit(private val context: Context) {
    /**
     * 清空图像缓存
     */
    fun clear() {
        NIMGlideModule.clearMemoryCache(context)
    }

    /**
     * 构建图像缓存
     */
    fun buildImageCache() {
        // 不必清除缓存，并且这个缓存清除比较耗时
        // clear();
        // build self avatar cache
        asyncLoadAvatarBitmapToCache(NimUIKit.getAccount())
    }

    /**
     * 获取通知栏提醒所需的头像位图，只存内存缓存/磁盘缓存中取，如果没有则返回空，自动发起异步加载
     * 注意：该方法在后台线程执行
     */
    fun getNotificationBitmapFromCache(url: String?): Bitmap? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        val imageSize = HeadImageView.DEFAULT_AVATAR_NOTIFICATION_ICON_SIZE
        var cachedBitmap: Bitmap? = null
        try {
            cachedBitmap = Glide.with(context).asBitmap().load(url).apply(
                RequestOptions().centerCrop().override(imageSize, imageSize)
            ).submit()[200, TimeUnit.MILLISECONDS] // 最大等待200ms
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cachedBitmap
    }

    /**
     * 异步加载头像位图到Glide缓存中
     */
    private fun asyncLoadAvatarBitmapToCache(account: String) {
        NimSingleThreadExecutor.getInstance().execute {
            val userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account)
            if (userInfo != null) {
                loadAvatarBitmapToCache(userInfo.avatar)
            }
        }
    }

    /**
     * 如果图片是上传到云信服务器，并且用户开启了文件安全功能，那么这里可能是短链，需要先换成源链才能下载。
     * 如果没有使用云信存储或没开启文件安全，那么不用这样做
     */
    private fun loadAvatarBitmapToCache(url: String) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        /*
         * 若使用网易云信云存储，这里可以设置下载图片的压缩尺寸，生成下载URL
         * 如果图片来源是非网易云信云存储，请不要使用NosThumbImageUtil
         */NIMClient.getService(NosService::class.java).getOriginUrlFromShortUrl(url).setCallback(
            object : RequestCallbackWrapper<String?>() {
                override fun onResult(code: Int, result: String?, exception: Throwable) {
                    var result = result
                    if (TextUtils.isEmpty(result)) {
                        result = url
                    }
                    val imageSize = HeadImageView.DEFAULT_AVATAR_THUMB_SIZE
                    Glide.with(context).load(result).submit(imageSize, imageSize)
                }
            })
    }

    companion object {
        private const val TAG = "ImageLoaderKit"
    }
}