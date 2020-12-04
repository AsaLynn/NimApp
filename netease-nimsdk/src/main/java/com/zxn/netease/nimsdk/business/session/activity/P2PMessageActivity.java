package com.zxn.netease.nimsdk.business.session.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.zxn.netease.nimsdk.common.ToastHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.api.NimUIKit;
import com.zxn.netease.nimsdk.api.model.contact.ContactChangedObserver;
import com.zxn.netease.nimsdk.api.model.main.OnlineStateChangeObserver;
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization;
import com.zxn.netease.nimsdk.api.model.user.UserInfoObserver;
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions;
import com.zxn.netease.nimsdk.business.session.constant.Extras;
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment;
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper;
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions;
import com.zxn.netease.nimsdk.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import java.util.List;


/**
 * 点对点聊天界面
 */
public class P2PMessageActivity extends BaseMessageActivity {

    private boolean isResume = false;

    public static void start(Context context, String contactId, SessionCustomization customization, IMMessage anchor) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, contactId);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        if (anchor != null) {
            intent.putExtra(Extras.EXTRA_ANCHOR, anchor);
        }
        intent.setClass(context, P2PMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 单聊特例话数据，包括个人信息，
        requestBuddyInfo();
        displayOnlineState();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResume = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isResume = false;
    }

    private void requestBuddyInfo() {
        setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
    }

    private void displayOnlineState() {
        if (!NimUIKitImpl.enableOnlineState()) {
            return;
        }
        String detailContent = NimUIKitImpl.getOnlineStateContentProvider().getDetailDisplay(sessionId);
        setSubTitle(detailContent);
    }


    /**
     * 命令消息接收观察者
     */
    private final Observer<CustomNotification> commandObserver = (Observer<CustomNotification>) message -> {
        if (!sessionId.equals(message.getSessionId()) || message.getSessionType() != SessionTypeEnum.P2P) {
            return;
        }
        showCommandMessage(message);
    };


    /**
     * 用户信息变更观察者
     */
    private final UserInfoObserver userInfoObserver = accounts -> {
        if (!accounts.contains(sessionId)) {
            return;
        }
        requestBuddyInfo();
    };

    /**
     * 好友资料变更（eg:关系）
     */
    private final ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            setTitle(UserInfoHelper.getUserTitleName(sessionId, SessionTypeEnum.P2P));
        }
    };

    /**
     * 好友在线状态观察者
     */
    private final OnlineStateChangeObserver onlineStateChangeObserver = accounts -> {
        if (!accounts.contains(sessionId)) {
            return;
        }
        // 按照交互来展示
        displayOnlineState();
    };

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(commandObserver, register);
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, register);
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
        if (NimUIKit.enableOnlineState()) {
            NimUIKit.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(onlineStateChangeObserver, register);
        }
    }


    protected void showCommandMessage(CustomNotification message) {
        if (!isResume) {
            return;
        }
        String content = message.getContent();
        try {
            JSONObject json = JSON.parseObject(content);
            int id = json.getIntValue("id");
            if (id == 1) {
                // 正在输入
                ToastHelper.showToastLong(P2PMessageActivity.this, "对方正在输入...");
            } else {
                ToastHelper.showToast(P2PMessageActivity.this, "command: " + content);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    protected MessageFragment fragment() {
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.P2P);
        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_message_activity;
    }

    @Override
    protected void initToolBar() {
        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);
    }

    @Override
    protected boolean enableSensor() {
        return true;
    }


}
