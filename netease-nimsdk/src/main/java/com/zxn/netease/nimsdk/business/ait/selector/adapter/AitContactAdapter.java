package com.zxn.netease.nimsdk.business.ait.selector.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.ait.selector.holder.SimpleLabelViewHolder;
import com.zxn.netease.nimsdk.business.ait.selector.model.AitContactItem;
import com.zxn.netease.nimsdk.business.ait.selector.model.ItemType;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemQuickAdapter;
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.List;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class AitContactAdapter extends BaseMultiItemQuickAdapter<AitContactItem, BaseViewHolder> {

    public AitContactAdapter(RecyclerView recyclerView, List<AitContactItem> data) {
        super(recyclerView, data);
        addItemType(ItemType.SIMPLE_LABEL, R.layout.nim_ait_contact_label_item, SimpleLabelViewHolder.class);
    }

    @Override
    protected int getViewType(AitContactItem item) {
        return item.getViewType();
    }

    @Override
    protected String getItemKey(AitContactItem item) {
        return "" + item.getViewType() + item.hashCode();
    }
}
