package com.zxn.netease.nimsdk.business.recent.holder;

import com.zxn.netease.nimsdk.business.team.helper.TeamHelper;
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseQuickAdapter;

public class SuperTeamRecentViewHolder extends TeamRecentViewHolder {

    public SuperTeamRecentViewHolder(BaseQuickAdapter adapter) {
        super(adapter);
    }

    @Override
    public String getTeamUserDisplayName(String tid, String account) {
        return TeamHelper.getSuperTeamMemberDisplayName(tid, account);
    }
}
