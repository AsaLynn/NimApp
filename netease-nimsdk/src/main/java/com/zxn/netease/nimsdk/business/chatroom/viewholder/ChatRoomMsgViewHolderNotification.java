package com.zxn.netease.nimsdk.business.chatroom.viewholder;

import android.widget.TextView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.chatroom.helper.ChatRoomNotificationHelper;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;

public class ChatRoomMsgViewHolderNotification extends ChatRoomMsgViewHolderBase {

    protected TextView notificationTextView;

    public ChatRoomMsgViewHolderNotification(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_notification;
    }

    @Override
    protected boolean shouldDisplayNick() {
        return false;
    }

    @Override
    protected void inflateContentView() {
        notificationTextView = (TextView) view.findViewById(R.id.message_item_notification_label);
    }

    @Override
    protected void bindContentView() {
        notificationTextView.setText(ChatRoomNotificationHelper.getNotificationText((ChatRoomNotificationAttachment) message.getAttachment()));
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

