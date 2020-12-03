package com.zxn.netease.nimsdk.common.adapter;

public interface IScrollStateListener {

    /**
     * move to scrap heap
     */
    void reclaim();


    /**
     * on idle
     */
    void onImmutable();
}
