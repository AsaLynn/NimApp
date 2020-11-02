package com.zxn.netease.nimsdk.business.session.emoji;

public interface IEmoticonSelectedListener {
    void onEmojiSelected(String key);

    void onStickerSelected(String categoryName, String stickerName);
}
