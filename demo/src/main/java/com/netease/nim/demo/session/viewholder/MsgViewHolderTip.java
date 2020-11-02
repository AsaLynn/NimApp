package com.netease.nim.demo.session.viewholder;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.widget.TextView;

//import com.netease.nim.demo.R;
import com.netease.nim.demo.R;
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil;
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

import java.util.Map;

/**
 * Created by huangjun on 2015/11/25.
 * Tip类型消息ViewHolder
 */
public class MsgViewHolderTip extends MsgViewHolderBase {

    protected TextView notificationTextView;

    public MsgViewHolderTip(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

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
        String text = "未知通知提醒";
        if (TextUtils.isEmpty(message.getContent())) {
            Map<String, Object> content = message.getRemoteExtension();
            if (content != null && !content.isEmpty()) {
                text = (String) content.get("content");
            }
        } else {
            text = message.getContent();
        }

        handleTextNotification(text);
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
