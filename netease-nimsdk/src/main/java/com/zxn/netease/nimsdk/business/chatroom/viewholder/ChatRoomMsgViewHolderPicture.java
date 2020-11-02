package com.zxn.netease.nimsdk.business.chatroom.viewholder;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.session.activity.WatchMessagePictureActivity;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

public class ChatRoomMsgViewHolderPicture extends ChatRoomMsgViewHolderThumbBase {

    public ChatRoomMsgViewHolderPicture(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_picture;
    }

    @Override
    protected void onItemClick() {
        WatchMessagePictureActivity.start(context, message);
    }

    @Override
    protected String thumbFromSourceFile(String path) {
        return path;
    }
}
