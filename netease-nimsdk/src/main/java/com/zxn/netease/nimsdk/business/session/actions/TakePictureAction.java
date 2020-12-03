package com.zxn.netease.nimsdk.business.session.actions;

import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.zxn.netease.nimsdk.R;

import java.io.File;

/**
 * Created by zxn on 2020/10/20.
 */
public class TakePictureAction extends PickImageAction{

    public TakePictureAction() {
        super(R.drawable.nim_message_plus_video_selector, R.string.input_panel_take, true);
    }

    @Override
    protected void showSelector(int titleId, int requestCode, boolean multiSelect) {
        //super.showSelector(titleId, requestCode, multiSelect);
        ImagePickerLauncher.takePhoto(getActivity(),requestCode);
    }

    @Override
    protected void onPicked(File file) {
        IMMessage message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
        sendMessage(message);
    }
}
