package com.netease.nim.demo.session.action;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.extension.GuessAttachment;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.zxn.netease.nimsdk.business.session.actions.BaseAction;

/**
 * Created by hzxuwen on 2015/6/11.
 */
public class GuessAction extends BaseAction {

    public GuessAction() {
        super(R.drawable.message_plus_guess_selector, R.string.input_panel_guess);
    }

    @Override
    public void onClick() {
        GuessAttachment attachment = new GuessAttachment();
        IMMessage message;

        message = MessageBuilder.createCustomMessage(getAccount(), getSessionType(), attachment.getValue().getDesc(), attachment);

        sendMessage(message);
    }
}
