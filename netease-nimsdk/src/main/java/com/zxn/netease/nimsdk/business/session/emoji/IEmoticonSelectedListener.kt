package com.zxn.netease.nimsdk.business.session.emoji

interface IEmoticonSelectedListener {
    fun onEmojiSelected(key: String?)
    fun onStickerSelected(categoryName: String?, stickerName: String?)
}