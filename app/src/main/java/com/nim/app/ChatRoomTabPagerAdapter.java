package com.nim.app;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.nim.app.tab.ChatRoomTab;
import com.nim.app.common.ui.viewpager.SlidingTabPagerAdapter;

import java.util.List;

/**
 * 聊天室主TAB适配器
 * Created by hzxuwen on 2015/12/14.
 */
public class ChatRoomTabPagerAdapter extends SlidingTabPagerAdapter {
    public ChatRoomTabPagerAdapter(FragmentManager fm, Context context, ViewPager pager) {
        super(fm, ChatRoomTab.values().length, context.getApplicationContext(), pager);

        for (ChatRoomTab tab : ChatRoomTab.values()) {
            try {
                ChatRoomTabFragment fragment = null;

                List<Fragment> fs = fm.getFragments();
                if (fs != null) {
                    for (Fragment f : fs) {
                        /*if (f.getClass() == tab.clazz) {
                            fragment = (ChatRoomTabFragment) f;
                            break;
                        }*/
                    }
                }

                /*if (fragment == null) {
                    fragment = tab.clazz.newInstance();
                }*/

                fragment.setState(this);
                fragment.attachTabData(tab);

                fragments[tab.tabIndex] = fragment;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getCacheCount() {
        return ChatRoomTab.values().length;
    }

    @Override
    public int getCount() {
        return ChatRoomTab.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        ChatRoomTab tab = ChatRoomTab.fromTabIndex(position);

        int resId = tab != null ? tab.resId : 0;

        return resId != 0 ? context.getText(resId) : "";
    }
}
