package com.zxn.netease.nimsdk.common.media.imagepicker.adapter.vh;

import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.common.adapter.BaseViewHolder;
import com.zxn.netease.nimsdk.common.media.imagepicker.Constants;
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePicker;
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher;
import com.zxn.netease.nimsdk.common.media.imagepicker.camera.CaptureActivity;
import com.zxn.netease.nimsdk.common.media.imagepicker.ui.ImageBaseActivity;


/**
 */

public class CameraViewHolder extends BaseViewHolder<Object> {

    private final ImagePicker imagePicker;
    private final ImageBaseActivity activity;

    public CameraViewHolder(ViewGroup parent, ImageBaseActivity activity, ImagePicker picker) {
        super(parent, R.layout.nim_adapter_image_list_camera);
        this.imagePicker = picker;
        this.activity = activity;
    }

    @Override
    public void findViews() {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imagePicker.videoOnly()) {
                    CaptureActivity.RECORD_MAX_TIME = imagePicker.getOption().getMaxVideoDuration();
                    CaptureActivity.RECORD_MIN_TIME = 1;

                    CaptureActivity.start(activity);
                    return;
                } else if (imagePicker.imageOnly()) {
                    if (!(activity.checkPermission(Manifest.permission.CAMERA))) {
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA},
                                                          Constants.REQUEST_PERMISSION_CAMERA);
                    } else {
                        ImagePickerLauncher.takePicture(activity, Constants.REQUEST_CODE_TAKE, imagePicker.getOption());
                    }
                } else {
                    CaptureActivity.start(activity, Constants.RESULT_CODE_RECORD_VIDEO);
                }
            }
        });
    }

    @Override
    protected void onBindViewHolder(Object data) {

    }
}
