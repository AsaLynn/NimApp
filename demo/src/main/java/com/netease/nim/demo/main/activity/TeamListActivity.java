package com.netease.nim.demo.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.zxn.netease.nimsdk.common.ToastHelper;

import com.netease.nim.demo.R;
import com.netease.nim.demo.session.SessionHelper;
import com.zxn.netease.nimsdk.business.contact.core.item.AbsContactItem;
import com.zxn.netease.nimsdk.business.contact.core.item.ContactItem;
import com.zxn.netease.nimsdk.business.contact.core.item.ItemTypes;
import com.zxn.netease.nimsdk.business.contact.core.model.ContactDataAdapter;
import com.zxn.netease.nimsdk.business.contact.core.model.ContactGroupStrategy;
import com.zxn.netease.nimsdk.business.contact.core.provider.ContactDataProvider;
import com.zxn.netease.nimsdk.business.contact.core.query.IContactDataProvider;
import com.zxn.netease.nimsdk.business.contact.core.viewholder.ContactHolder;
import com.zxn.netease.nimsdk.business.contact.core.viewholder.LabelHolder;
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions;
import com.zxn.netease.nimsdk.common.activity.UI;
import com.zxn.netease.nimsdk.api.NimUIKit;
import com.zxn.netease.nimsdk.api.model.team.TeamDataChangedObserver;
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.List;

/**
 * 群列表(通讯录)
 * <p/>
 * Created by huangjun on 2015/4/21.
 */
public class TeamListActivity extends UI implements AdapterView.OnItemClickListener {

    private static final String EXTRA_DATA_ITEM_TYPES = "EXTRA_DATA_ITEM_TYPES";

    private ContactDataAdapter adapter;

    private ListView lvContacts;

    private int itemType;

    public static final void start(Context context, int teamItemTypes) {
        Intent intent = new Intent();
        intent.setClass(context, TeamListActivity.class);
        intent.putExtra(EXTRA_DATA_ITEM_TYPES, teamItemTypes);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemType = getIntent().getIntExtra(EXTRA_DATA_ITEM_TYPES, ItemTypes.TEAMS.ADVANCED_TEAM);

        setContentView(R.layout.group_list_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = itemType == ItemTypes.TEAMS.ADVANCED_TEAM ? R.string.advanced_team : R.string.normal_team;
        setToolBar(R.id.toolbar, options);

        lvContacts = (ListView) findViewById(R.id.group_list);

        GroupStrategy groupStrategy = new GroupStrategy();
        IContactDataProvider dataProvider = new ContactDataProvider(itemType);

        adapter = new ContactDataAdapter(this, groupStrategy, dataProvider) {
            @Override
            protected List<AbsContactItem> onNonDataItems() {
                return null;
            }

            @Override
            protected void onPreReady() {
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
            }
        };
        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        adapter.addViewHolder(ItemTypes.TEAM, ContactHolder.class);

        lvContacts.setAdapter(adapter);
        lvContacts.setOnItemClickListener(this);
        lvContacts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                showKeyboard(false);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        // load data
        int count = NIMClient.getService(TeamService.class).queryTeamCountByTypeBlock(itemType == ItemTypes.TEAMS
                .ADVANCED_TEAM ? TeamTypeEnum.Advanced : TeamTypeEnum.Normal);
        if (count == 0) {
            if (itemType == ItemTypes.TEAMS.ADVANCED_TEAM) {
                ToastHelper.showToast(TeamListActivity.this, R.string.no_team);
            } else if (itemType == ItemTypes.TEAMS.NORMAL_TEAM) {
                ToastHelper.showToast(TeamListActivity.this, R.string.no_normal_team);
            }
        }

        adapter.load(true);

        registerTeamUpdateObserver(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerTeamUpdateObserver(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AbsContactItem item = (AbsContactItem) adapter.getItem(position);
        switch (item.getItemType()) {
            case ItemTypes.TEAM:
                SessionHelper.startTeamSession(this, ((ContactItem) item).getContact().getContactId());
                break;
        }
    }

    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
    }

    TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            adapter.load(true);
        }

        @Override
        public void onRemoveTeam(Team team) {
            adapter.load(true);
        }
    };

    private static class GroupStrategy extends ContactGroupStrategy {
        GroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, 0, ""); // 默认分组
        }

        @Override
        public String belongs(AbsContactItem item) {
            switch (item.getItemType()) {
                case ItemTypes.TEAM:
                    return GROUP_NULL;
                default:
                    return null;
            }
        }
    }

}
