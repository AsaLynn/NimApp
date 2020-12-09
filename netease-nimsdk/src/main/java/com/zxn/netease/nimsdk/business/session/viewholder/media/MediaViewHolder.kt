package com.zxn.netease.nimsdk.business.session.viewholder.media;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.zxn.netease.nimsdk.R;

/**
 * Created by winnie on 2017/9/18.
 */

public class MediaViewHolder extends RecyclerView.ViewHolder {

    public ImageView mediaImage;

    public ImageView playImage;

    public MediaViewHolder(View itemView) {
        super(itemView);
        mediaImage = itemView.findViewById(R.id.media_image);
        playImage = itemView.findViewById(R.id.play_image);
    }
}
