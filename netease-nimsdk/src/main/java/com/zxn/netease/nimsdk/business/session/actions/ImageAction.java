package com.zxn.netease.nimsdk.business.session.actions;

import com.zxn.netease.nimsdk.R;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;


public class ImageAction extends PickImageAction {

    public ImageAction() {
        super(R.drawable.nim_message_plus_photo_selector, R.string.input_panel_photo, true);
    }

    @Override
    protected void onPicked(File file) {
        IMMessage message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
        sendMessage(message);
    }
}

