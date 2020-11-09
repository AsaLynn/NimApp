package com.zxn.netease.nimsdk.common

object CommonUtil {
    @JvmStatic
    fun isEmpty(collection: Collection<*>?): Boolean {
        return collection == null || collection.isEmpty()
    }
}