package com.zxn.netease.nimsdk.business.ait.selector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.api.NimUIKit;
import com.zxn.netease.nimsdk.api.model.SimpleCallback;
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions;
import com.zxn.netease.nimsdk.business.ait.selector.adapter.AitContactAdapter;
import com.zxn.netease.nimsdk.business.ait.selector.model.AitContactItem;
import com.zxn.netease.nimsdk.business.ait.selector.model.ItemType;
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions;
import com.zxn.netease.nimsdk.common.activity.UI;
import com.zxn.netease.nimsdk.common.ui.recyclerview.listener.OnItemClickListener;
import com.netease.nimlib.sdk.robot.model.NimRobotInfo;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.List;


public class AitContactSelectorActivity extends UI {
    private static final String EXTRA_ID = "EXTRA_ID";
    private static final String EXTRA_ROBOT = "EXTRA_ROBOT";

    public static final int REQUEST_CODE = 0x10;
    public static final String RESULT_TYPE = "type";
    public static final String RESULT_DATA = "data";

    private AitContactAdapter adapter;

    private String teamId;

    private boolean addRobot;

    private List<AitContactItem> items;

    public static void start(Context context, String tid, boolean addRobot) {
        Intent intent = new Intent();
        if (tid != null) {
            intent.putExtra(EXTRA_ID, tid);
        }
        if (addRobot) {
            intent.putExtra(EXTRA_ROBOT, true);
        }
        intent.setClass(context, AitContactSelectorActivity.class);

        ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_list_layout);
        parseIntent();
        initViews();
        initData();
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.member_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initAdapter(recyclerView);
        ToolBarOptions options = new NimToolBarOptions();
        options.titleString = "选择提醒的人";
        setToolBar(R.id.toolbar, options);
    }

    private void initAdapter(RecyclerView recyclerView) {
        items = new ArrayList<>();
        adapter = new AitContactAdapter(recyclerView, items);
        recyclerView.setAdapter(adapter);

        List<Integer> noDividerViewTypes = new ArrayList<>(1);
        noDividerViewTypes.add(ItemType.SIMPLE_LABEL);
        recyclerView.addItemDecoration(new AitContactDecoration(this, LinearLayoutManager.VERTICAL, noDividerViewTypes));

        recyclerView.addOnItemTouchListener(new OnItemClickListener<AitContactAdapter>() {

            @Override
            public void onItemClick(AitContactAdapter adapter, View view, int position) {
                AitContactItem item = adapter.getItem(position);
                Intent intent = new Intent();
                intent.putExtra(RESULT_TYPE, item.getViewType());
                if (item.getViewType() == ItemType.TEAM_MEMBER) {
                    intent.putExtra(RESULT_DATA, (TeamMember) item.getModel());
                } else if (item.getViewType() == ItemType.ROBOT) {
                    intent.putExtra(RESULT_DATA, (NimRobotInfo) item.getModel());
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void parseIntent() {
        Intent intent = getIntent();
        teamId = intent.getStringExtra(EXTRA_ID);
        addRobot = intent.getBooleanExtra(EXTRA_ROBOT, false);
    }

    private void initData() {
        items = new ArrayList<AitContactItem>();

        //data 加载结束，通知更新
        adapter.setNewData(items);
    }
}
