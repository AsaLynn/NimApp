package com.zxn.netease.nimsdk.support.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.module.GlideModule
import com.zxn.netease.nimsdk.common.util.log.LogUtil
import com.zxn.netease.nimsdk.common.util.log.sdk.wrapper.AbsNimLog
import java.io.File


class NIMGlideModule : GlideModule {
    /**
     * ************************ GlideModule override ************************
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // sdcard/Android/data/com.netease.nim.demo/glide
        val cachedDirName = "glide"
        builder.setDiskCache(
            ExternalCacheDiskCacheFactory(
                context,
                cachedDirName,
                MAX_DISK_CACHE_SIZE
            )
        )
        AbsNimLog.i(
            TAG,
            "NIMGlideModule apply options, disk cached path=" + context.externalCacheDir + File.pathSeparator + cachedDirName
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {}

    companion object {
        private const val TAG = "NIMGlideModule"
        private const val M = 1024 * 1024
        private const val MAX_DISK_CACHE_SIZE = 256 * M

        /**
         * ************************ Memory Cache ************************
         */
        fun clearMemoryCache(context: Context?) {
            Glide.get(context!!).clearMemory()
        }
    }
}