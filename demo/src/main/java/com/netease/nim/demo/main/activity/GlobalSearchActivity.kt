package com.netease.nim.demo.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import com.netease.nim.demo.R
import com.netease.nim.demo.session.SessionHelper.startP2PSession
import com.netease.nim.demo.session.search.DisplayMessageActivity
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions
import com.zxn.netease.nimsdk.business.contact.core.item.AbsContactItem
import com.zxn.netease.nimsdk.business.contact.core.item.ContactItem
import com.zxn.netease.nimsdk.business.contact.core.item.ItemTypes
import com.zxn.netease.nimsdk.business.contact.core.item.MsgItem
import com.zxn.netease.nimsdk.business.contact.core.model.ContactDataAdapter
import com.zxn.netease.nimsdk.business.contact.core.model.ContactGroupStrategy
import com.zxn.netease.nimsdk.business.contact.core.provider.ContactDataProvider
import com.zxn.netease.nimsdk.business.contact.core.query.IContactDataProvider
import com.zxn.netease.nimsdk.business.contact.core.viewholder.ContactHolder
import com.zxn.netease.nimsdk.business.contact.core.viewholder.LabelHolder
import com.zxn.netease.nimsdk.business.contact.core.viewholder.MsgHolder
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.util.string.StringUtil
import kotlinx.android.synthetic.main.global_search_result.*

/**
 * 全局搜索页面
 * 支持通讯录搜索、消息全文检索
 */
class GlobalSearchActivity : UI(), AdapterView.OnItemClickListener {
    private var adapter: ContactDataAdapter? = null
//    private var lvContacts: ListView = searchResultList
    private var searchView: SearchView? = null
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.global_search_menu, menu)
        val item = menu.findItem(R.id.action_search)
        handler.post { MenuItemCompat.expandActionView(item) }
        MenuItemCompat.setOnActionExpandListener(
            item,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    finish()
                    return false
                }
            })
        searchView = MenuItemCompat.getActionView(item) as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                showKeyboard(false)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (StringUtil.isEmpty(query)) {
                    lvContacts!!.visibility = View.GONE
                } else {
                    lvContacts!!.visibility = View.VISIBLE
                }
                adapter!!.query(query)
                return true
            }
        })
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.global_search_result)
        val options: ToolBarOptions = NimToolBarOptions()
        setToolBar(R.id.toolbar, options)
        //lvContacts = findViewById(R.id.searchResultList)
        lvContacts!!.visibility = View.GONE
        val searchGroupStrategy = SearchGroupStrategy()
        val dataProvider: IContactDataProvider =
            ContactDataProvider(ItemTypes.FRIEND, ItemTypes.TEAM, ItemTypes.MSG)
        adapter = ContactDataAdapter(this, searchGroupStrategy, dataProvider)
        adapter!!.addViewHolder(ItemTypes.LABEL, LabelHolder::class.java)
        adapter!!.addViewHolder(ItemTypes.FRIEND, ContactHolder::class.java)
        adapter!!.addViewHolder(ItemTypes.TEAM, ContactHolder::class.java)
        adapter!!.addViewHolder(ItemTypes.MSG, MsgHolder::class.java)
        lvContacts.adapter = adapter
        lvContacts.onItemClickListener = this
        lvContacts.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                showKeyboard(false)
            }

            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
            }
        })
        findViewById<View>(R.id.global_search_root).setOnTouchListener(
            OnTouchListener {_, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    finish()
                    return@OnTouchListener true
                }
                false
            })
    }

    override fun onResume() {
        super.onResume()
        if (searchView != null) {
            searchView!!.clearFocus()
        }
    }

    private class SearchGroupStrategy internal constructor() : ContactGroupStrategy() {
        override fun belongs(item: AbsContactItem): String {
            return when (item.itemType) {
                ItemTypes.FRIEND -> GROUP_FRIEND
                ItemTypes.TEAM -> GROUP_TEAM
                ItemTypes.MSG -> GROUP_MSG
                else -> GROUP_MSG
            }
        }

        companion object {
            const val GROUP_FRIEND = "FRIEND"
            const val GROUP_TEAM = "TEAM"
            const val GROUP_MSG = "MSG"
        }

        init {
            add(GROUP_NULL, 0, "")
            add(GROUP_TEAM, 1, "群组")
            add(GROUP_FRIEND, 2, "好友")
            add(GROUP_MSG, 3, "聊天记录")
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val item = adapter!!.getItem(position) as AbsContactItem
        when (item.itemType) {
            ItemTypes.TEAM -> {
            }
            ItemTypes.FRIEND -> {
                startP2PSession(this, (item as ContactItem).contact.contactId)
            }
            ItemTypes.MSG -> {
                val msgIndexRecord = (item as MsgItem).record
                if (msgIndexRecord.count > 1) {
                    GlobalSearchDetailActivity2.start(this, msgIndexRecord)
                } else {
                    DisplayMessageActivity.start(this, msgIndexRecord.message)
                }
            }
            else -> {
            }
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent()
            intent.setClass(context, GlobalSearchActivity::class.java)
            context.startActivity(intent)
        }
    }
}