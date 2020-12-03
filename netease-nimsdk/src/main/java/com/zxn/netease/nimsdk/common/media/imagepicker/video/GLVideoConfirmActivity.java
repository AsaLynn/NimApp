package com.zxn.netease.nimsdk.common.media.imagepicker.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.common.media.imagepicker.Constants;

public class GLVideoConfirmActivity extends GLVideoActivity {
    public static void start(Activity context, Uri uri, long duration) {
        Intent intent = new Intent(context, GLVideoConfirmActivity.class);
        intent.setData(uri);
        intent.putExtra(KEY_Duration, duration);
        context.startActivityForResult(intent, Constants.RESULT_CODE_CONFIRM_VIDEO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.btn_back).setVisibility(View.GONE);
        findViewById(R.id.retake).setVisibility(View.VISIBLE);
        findViewById(R.id.confirm).setVisibility(View.VISIBLE);

        findViewById(R.id.retake).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
