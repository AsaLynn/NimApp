package com.zxn.netease.nimsdk.business.session.viewholder.media;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.zxn.netease.nimsdk.R;

/**
 * Created by winnie on 2017/9/18.
 */

public class DateViewHolder extends RecyclerView.ViewHolder {

    public TextView dateText;

    public DateViewHolder(View itemView) {
        super(itemView);
        dateText = itemView.findViewById(R.id.date_tip);
    }
}
