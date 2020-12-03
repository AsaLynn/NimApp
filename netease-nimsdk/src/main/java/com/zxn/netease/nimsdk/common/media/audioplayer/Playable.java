package com.zxn.netease.nimsdk.common.media.audioplayer;

public interface Playable {
    long getDuration();

    String getPath();

    boolean isAudioEqual(Playable audio);
}