package com.zxn.netease.nimsdk.business.session.module.list;

import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil;
import com.zxn.netease.nimsdk.common.ui.imageview.HeadImageView;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseFetchLoadAdapter;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 新消息提醒模块
 * Created by hzxuwen on 2015/6/17.
 */
public class IncomingMsgPrompt {
    // 底部新消息提示条
    private View newMessageTipLayout;
    private TextView newMessageTipTextView;
    private HeadImageView newMessageTipHeadImageView;

    private final Context context;
    private final View view;
    private final RecyclerView messageListView;
    private final BaseFetchLoadAdapter adapter;
    private final Handler uiHandler;

    public IncomingMsgPrompt(Context context, View view, RecyclerView messageListView, BaseFetchLoadAdapter adapter,
                             Handler uiHandler) {
        this.context = context;
        this.view = view;
        this.messageListView = messageListView;
        this.adapter = adapter;
        this.uiHandler = uiHandler;
    }

    // 显示底部新信息提示条
    public void show(IMMessage newMessage) {
        if (newMessageTipLayout == null) {
            init();
        }

        if (!TextUtils.isEmpty(newMessage.getFromAccount())) {
            newMessageTipHeadImageView.loadBuddyAvatar(newMessage.getFromAccount());
        } else {
            newMessageTipHeadImageView.resetImageView();
        }

        //MoonUtil.identifyFaceExpression(context, newMessageTipTextView, TeamNotificationHelper.getMsgShowText(newMessage), ImageSpan.ALIGN_BOTTOM);
        newMessageTipLayout.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(showNewMessageTipLayoutRunnable);
        uiHandler.postDelayed(showNewMessageTipLayoutRunnable, 5 * 1000);
    }

    public void onBackPressed() {
        removeHandlerCallback();
    }

    // 初始化底部新信息提示条
    private void init() {
        ViewGroup containerView = view.findViewById(R.id.message_activity_list_view_container);
        View.inflate(context, R.layout.nim_new_message_tip_layout, containerView);
        newMessageTipLayout = containerView.findViewById(R.id.new_message_tip_layout);
        newMessageTipLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                messageListView.scrollToPosition(adapter.getBottomDataPosition());
                newMessageTipLayout.setVisibility(View.GONE);
            }
        });
        newMessageTipTextView = newMessageTipLayout.findViewById(R.id.new_message_tip_text_view);
        newMessageTipHeadImageView = newMessageTipLayout.findViewById(R.id.new_message_tip_head_image_view);
    }

    private final Runnable showNewMessageTipLayoutRunnable = new Runnable() {

        @Override
        public void run() {
            newMessageTipLayout.setVisibility(View.GONE);
        }
    };

    private void removeHandlerCallback() {
        if (showNewMessageTipLayoutRunnable != null) {
            uiHandler.removeCallbacks(showNewMessageTipLayoutRunnable);
        }
    }
}
