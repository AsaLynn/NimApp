package com.zxn.netease.nimsdk.business.session.fragment;

import com.zxn.netease.nimsdk.common.ToastHelper;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.api.NimUIKit;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public class TeamMessageFragment extends MessageFragment {

    private Team team;

    @Override
    public boolean isAllowSendMessage(IMMessage message) {
        if (team == null) {
            team = NimUIKit.getTeamProvider().getTeamById(sessionId);
        }

        if (team == null || !team.isMyTeam()) {
            ToastHelper.showToast(getActivity(), R.string.team_send_message_not_allow);
            return false;
        }

        return super.isAllowSendMessage(message);
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}