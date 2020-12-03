package com.zxn.netease.nimsdk.business.contact.core.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.business.contact.core.item.LabelItem;
import com.zxn.netease.nimsdk.business.contact.core.model.ContactDataAdapter;

public class LabelHolder extends AbsContactViewHolder<LabelItem> {

    private TextView name;

    @Override
    public void refresh(ContactDataAdapter contactAdapter, int position, LabelItem item) {
        this.name.setText(item.getText());
    }

    @Override
    public View inflate(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.nim_contacts_abc_item, null);
        this.name = view.findViewById(R.id.tv_nickname);
        return view;
    }

}
