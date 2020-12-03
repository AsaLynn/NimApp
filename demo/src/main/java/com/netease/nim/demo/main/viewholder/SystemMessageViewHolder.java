package com.netease.nim.demo.main.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.main.helper.MessageHelper;
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper;
import com.zxn.netease.nimsdk.common.adapter.TViewHolder;
import com.zxn.netease.nimsdk.common.ui.imageview.HeadImageView;
import com.zxn.netease.nimsdk.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.model.SystemMessage;

/**
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageViewHolder extends TViewHolder {

    private SystemMessage message;
    private HeadImageView headImageView;
    private TextView fromAccountText;
    private TextView timeText;
    private TextView contentText;
    private TextView postcriptText;
    private View operatorLayout;
    private Button agreeButton;
    private Button rejectButton;
    private TextView operatorResultText;
    private SystemMessageListener listener;

    public interface SystemMessageListener {
        void onAgree(SystemMessage message);

        void onReject(SystemMessage message);

        void onLongPressed(SystemMessage message);
    }

    @Override
    protected int getResId() {
        return R.layout.message_system_notification_view_item;
    }

    @Override
    protected void inflate() {
        headImageView = (HeadImageView) view.findViewById(R.id.from_account_head_image);
        fromAccountText = (TextView) view.findViewById(R.id.from_account_text);
        contentText = (TextView) view.findViewById(R.id.content_text);
        postcriptText = (TextView) view.findViewById(R.id.postscript_text);
        timeText = (TextView) view.findViewById(R.id.notification_time);
        operatorLayout = view.findViewById(R.id.operator_layout);
        agreeButton = (Button) view.findViewById(R.id.agree);
        rejectButton = (Button) view.findViewById(R.id.reject);
        operatorResultText = (TextView) view.findViewById(R.id.operator_result);
    }

    @Override
    protected void refresh(Object item) {
        message = (SystemMessage) item;
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongPressed(message);
                }

                return true;
            }
        });
        headImageView.loadBuddyAvatar(message.getFromAccount());
        fromAccountText.setText(UserInfoHelper.getUserDisplayNameEx(message.getFromAccount(), "我"));
        contentText.setText(MessageHelper.getVerifyNotificationText(message));
        timeText.setText(TimeUtil.getTimeShowString(message.getTime(), false));
        if (!MessageHelper.isVerifyMessageNeedDeal(message)) {
            operatorLayout.setVisibility(View.GONE);
        } else {
            if (message.getStatus() == SystemMessageStatus.init) {
                // 未处理
                operatorResultText.setVisibility(View.GONE);
                operatorLayout.setVisibility(View.VISIBLE);
                agreeButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            } else {
                // 处理结果
                agreeButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
                operatorResultText.setVisibility(View.VISIBLE);
                operatorResultText.setText(MessageHelper.getVerifyNotificationDealResult(message));
            }
        }

        if (!MessageHelper.hasPostscript(message)) {
            postcriptText.setVisibility(View.GONE);
        } else {
            postcriptText.setText(message.getContent());
            postcriptText.setVisibility(View.VISIBLE);
        }
    }

    public void refreshDirectly(final SystemMessage message) {
        if (message != null) {
            refresh(message);
        }
    }

    public void setListener(final SystemMessageListener l) {
        if (l == null) {
            return;
        }

        this.listener = l;
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReplySending();
                listener.onAgree(message);
            }
        });
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReplySending();
                listener.onReject(message);
            }
        });
    }

    /**
     * 等待服务器返回状态设置
     */
    private void setReplySending() {
        agreeButton.setVisibility(View.GONE);
        rejectButton.setVisibility(View.GONE);
        operatorResultText.setVisibility(View.VISIBLE);
        operatorResultText.setText(R.string.team_apply_sending);
    }
}
