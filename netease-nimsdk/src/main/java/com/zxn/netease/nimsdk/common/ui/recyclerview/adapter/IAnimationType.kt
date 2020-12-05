package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter

/**
 * Created by zxn on 2020/12/5.
 */
interface IAnimationType {

    companion object {

        /**
         * Use with [.openLoadAnimation]
         */
        const val ALPHAIN: Int = 0x00000001

        /**
         * Use with [.openLoadAnimation]
         */
        const val SCALEIN = 0x00000002

        /**
         * Use with [.openLoadAnimation]
         */
        const val SLIDEIN_BOTTOM = 0x00000003

        /**
         * Use with [.openLoadAnimation]
         */
        const val SLIDEIN_LEFT = 0x00000004

        /**
         * Use with [.openLoadAnimation]
         */
        const val SLIDEIN_RIGHT = 0x00000005
    }

}