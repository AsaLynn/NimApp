package com.zxn.netease.nimsdk.business.ait.selector.holder;

import android.widget.TextView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.ait.selector.model.AitContactItem;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseQuickAdapter;
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder;
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.RecyclerViewHolder;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class SimpleLabelViewHolder extends RecyclerViewHolder<BaseQuickAdapter, BaseViewHolder, AitContactItem<String>> {

    private TextView textView;

    public SimpleLabelViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public void convert(BaseViewHolder holder, AitContactItem<String> data, int position, boolean isScrolling) {
        inflate(holder);
        refresh(data.getModel());
    }

    public void inflate(BaseViewHolder holder) {
        textView = holder.getView(R.id.tv_label);
    }

    public void refresh(String label) {
        textView.setText(label);
    }
}
