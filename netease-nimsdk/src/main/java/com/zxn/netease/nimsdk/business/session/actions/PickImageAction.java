package com.zxn.netease.nimsdk.business.session.actions;

import android.content.Intent;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.session.constant.RequestCode;
import com.zxn.netease.nimsdk.business.session.helper.SendImageHelper;
import com.zxn.netease.nimsdk.common.ToastHelper;
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher;
import com.zxn.netease.nimsdk.common.media.imagepicker.option.DefaultImagePickerOption;
import com.zxn.netease.nimsdk.common.media.imagepicker.option.ImagePickerOption;

import java.io.File;


public abstract class PickImageAction extends BaseAction {

    protected static final int PICK_IMAGE_COUNT = 9;

    public static final String MIME_JPEG = "image/jpeg";

    private final boolean multiSelect;

    protected abstract void onPicked(File file);

    protected PickImageAction(int iconResId, int titleId, boolean multiSelect) {
        super(iconResId, titleId);
        this.multiSelect = multiSelect;
    }

    @Override
    public void onClick() {
        int requestCode = makeRequestCode(RequestCode.PICK_IMAGE);
        showSelector(getTitleId(), requestCode, multiSelect);
    }

    /**
     * 打开图片选择器
     */
    protected void showSelector(int titleId, final int requestCode, final boolean multiSelect) {
        ImagePickerOption option = DefaultImagePickerOption.getInstance().setShowCamera(true).setPickType(
                ImagePickerOption.PickType.Image).setMultiMode(multiSelect).setSelectMax(PICK_IMAGE_COUNT);
        ImagePickerLauncher.selectImage(getActivity(), requestCode, option, titleId);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.PICK_IMAGE:
                onPickImageActivityResult(requestCode, data);
                break;
        }
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(int requestCode, Intent data) {
        if (data == null) {
            ToastHelper.showToastLong(getActivity(), R.string.picker_image_error);
            return;
        }
        sendImageAfterSelfImagePicker(data);
    }


    /**
     * 发送图片
     */
    private void sendImageAfterSelfImagePicker(final Intent data) {
        SendImageHelper.sendImageAfterSelfImagePicker(getActivity(), data, new SendImageHelper.Callback() {

            @Override
            public void sendImage(File file, boolean isOrig) {
                onPicked(file);
            }
        });
    }

}
