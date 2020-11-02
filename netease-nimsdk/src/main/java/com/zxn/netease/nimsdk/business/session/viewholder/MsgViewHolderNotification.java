package com.zxn.netease.nimsdk.business.session.viewholder;

import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil;
import com.zxn.netease.nimsdk.business.session.helper.TeamNotificationHelper;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

public class MsgViewHolderNotification extends MsgViewHolderBase {

    public MsgViewHolderNotification(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    protected TextView notificationTextView;

    @Override
    public int getContentResId() {
        return R.layout.nim_message_item_notification;
    }

    @Override
    public void inflateContentView() {
        notificationTextView = (TextView) view.findViewById(R.id.message_item_notification_label);
    }

    @Override
    public void bindContentView() {
        handleTextNotification(getDisplayText());
    }

    protected String getDisplayText() {
        return TeamNotificationHelper.getTeamNotificationText(message, message.getSessionId());
    }

    private void handleTextNotification(String text) {
        MoonUtil.identifyFaceExpressionAndATags(context, notificationTextView, text, ImageSpan.ALIGN_BOTTOM);
        notificationTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected boolean isMiddleItem() {
        return true;
    }
}

