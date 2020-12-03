package com.zxn.netease.nimsdk.business.session.viewholder;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.session.activity.WatchMessagePictureActivity;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderPicture extends MsgViewHolderThumbBase {

    public MsgViewHolderPicture(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    public int getContentResId() {
        return R.layout.nim_message_item_picture;
    }

    @Override
    public void onItemClick() {
        WatchMessagePictureActivity.start(context, message);
    }

    @Override
    protected String thumbFromSourceFile(String path) {
        return path;
    }
}
