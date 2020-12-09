package com.netease.nim.demo.session.viewholder;

import com.netease.nim.demo.session.extension.DefaultCustomAttachment;
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderText;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;

/**
 * Created by zhoujianghua on 2015/8/4.
 */
public class MsgViewHolderDefCustom extends MsgViewHolderText {

    public MsgViewHolderDefCustom(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected String getDisplayText() {
        DefaultCustomAttachment attachment = (DefaultCustomAttachment) message.getAttachment();
        return "type: " + attachment.getType() + ", data: " + attachment.getContent();
    }
}
