package com.zxn.netease.nimsdk.business.session.actions;

import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher;
import com.zxn.netease.nimsdk.common.media.imagepicker.option.DefaultImagePickerOption;
import com.zxn.netease.nimsdk.common.media.imagepicker.option.ImagePickerOption;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.zxn.netease.nimsdk.R;

import java.io.File;

/**
 *
 * Created by zxn on 2020/10/20.
 */
public class SelectImageAction extends PickImageAction {

    public SelectImageAction() {
        super(R.drawable.nim_message_plus_photo_selector, R.string.input_panel_photo, true);
    }

    public SelectImageAction(int iconResId, int titleId, boolean multiSelect) {
        super(iconResId, titleId, multiSelect);
    }

    @Override
    protected void showSelector(int titleId, int requestCode, boolean multiSelect) {
        //super.showSelector(titleId, requestCode, multiSelect);
        ImagePickerOption option = DefaultImagePickerOption.getInstance().setShowCamera(true).setPickType(
                ImagePickerOption.PickType.Image).setMultiMode(multiSelect).setSelectMax(PICK_IMAGE_COUNT);
        ImagePickerLauncher.selectImage(getActivity(), requestCode, option);
    }

    @Override
    protected void onPicked(File file) {
        IMMessage message;
        if (getContainer() != null && getContainer().sessionType == SessionTypeEnum.ChatRoom) {
            message = ChatRoomMessageBuilder.createChatRoomImageMessage(getAccount(), file, file.getName());
        } else {
            message = MessageBuilder.createImageMessage(getAccount(), getSessionType(), file, file.getName());
        }
        sendMessage(message);
    }

}
