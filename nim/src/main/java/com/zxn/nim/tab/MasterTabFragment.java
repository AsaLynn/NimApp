package com.zxn.nim.tab;

import com.zxn.nim.MasterFragment;
import com.zxn.nim.ChatRoomTabFragment;
import com.zxn.nim.R;

/**
 * 主播基类fragment
 * Created by hzxuwen on 2015/12/14.
 */
public class MasterTabFragment extends ChatRoomTabFragment {

    private MasterFragment fragment;

    @Override
    protected void onInit() {
        fragment = (MasterFragment) getActivity().getSupportFragmentManager().findFragmentById(
                R.id.master_fragment);
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        if (fragment != null) {
            fragment.onCurrent();
        }
    }
}
