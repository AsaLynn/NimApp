package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zxn on 2020/12/5.
 */
@IntDef({IAnimationType.ALPHAIN,
        IAnimationType.SCALEIN,
        IAnimationType.SLIDEIN_BOTTOM,
        IAnimationType.SLIDEIN_LEFT,
        IAnimationType.SLIDEIN_RIGHT})
@Retention(RetentionPolicy.SOURCE)
public @interface AnimationType {
}
