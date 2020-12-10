package com.netease.nim.demo.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import com.netease.nim.demo.R
import com.netease.nim.demo.session.SessionHelper.startP2PSession
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.search.model.MsgIndexRecord
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions
import com.zxn.netease.nimsdk.business.contact.core.item.AbsContactItem
import com.zxn.netease.nimsdk.business.contact.core.item.ItemTypes
import com.zxn.netease.nimsdk.business.contact.core.item.MsgItem
import com.zxn.netease.nimsdk.business.contact.core.model.ContactDataAdapter
import com.zxn.netease.nimsdk.business.contact.core.provider.ContactDataProvider
import com.zxn.netease.nimsdk.business.contact.core.query.IContactDataProvider
import com.zxn.netease.nimsdk.business.contact.core.query.TextQuery
import com.zxn.netease.nimsdk.business.contact.core.viewholder.LabelHolder
import com.zxn.netease.nimsdk.business.contact.core.viewholder.MsgHolder
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions
import com.zxn.netease.nimsdk.common.activity.UI

/**
 * 消息全文检索详细页面
 */
class GlobalSearchDetailActivity : UI(), AdapterView.OnItemClickListener {
    private var adapter: ContactDataAdapter? = null
    private var lvContacts: ListView? = null
    private var sessionId: String? = null
    private var sessionType: SessionTypeEnum? = null
    private var query: String? = null
    private var resultCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseIntent()
        setContentView(R.layout.global_search_detail)

        // title name
        val options: ToolBarOptions = NimToolBarOptions()
        if (sessionType == SessionTypeEnum.P2P) {
            options.titleString = UserInfoHelper.getUserDisplayName(sessionId)
        }
        setToolBar(R.id.toolbar, options)

        // textView tip
        val tip = String.format("共%d条与\"%s\"相关的聊天记录", resultCount, query)
        val tipTextView = findView<TextView>(R.id.search_result_tip)
        tipTextView.text = tip

        // listView adapter
        lvContacts = findView<ListView>(R.id.search_result_list)
        val dataProvider: IContactDataProvider = ContactDataProvider(ItemTypes.MSG)
        adapter = ContactDataAdapter(this, null, dataProvider)
        adapter!!.addViewHolder(ItemTypes.LABEL, LabelHolder::class.java)
        adapter!!.addViewHolder(ItemTypes.MSG, MsgHolder::class.java)
        lvContacts!!.adapter = adapter
        lvContacts!!.onItemClickListener = this

        // query data
        val textQuery = TextQuery(query)
        textQuery.extra = arrayOf<Any?>(sessionType, sessionId)
        adapter!!.query(textQuery)
    }

    private fun parseIntent() {
        sessionType = SessionTypeEnum.typeOfValue(intent.getIntExtra(EXTRA_SESSION_TYPE, 0))
        sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        query = intent.getStringExtra(EXTRA_QUERY)
        resultCount = intent.getIntExtra(EXTRA_RESULT_COUNT, 0)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val item = adapter!!.getItem(position - lvContacts!!.headerViewsCount) as AbsContactItem
        when (item.itemType) {
            ItemTypes.MSG -> {
                val msgIndexRecord = (item as MsgItem).record
                if (msgIndexRecord.sessionType == SessionTypeEnum.P2P) {
                    startP2PSession(this, msgIndexRecord.sessionId, msgIndexRecord.message)
                } else if (msgIndexRecord.sessionType == SessionTypeEnum.Team) {
                    //SessionHelper.startTeamSession(this, msgIndexRecord.getSessionId(), msgIndexRecord.getMessage());
                }
            }
            else -> {
            }
        }
    }

    companion object {
        private const val EXTRA_SESSION_TYPE = "EXTRA_SESSION_TYPE"
        private const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        private const val EXTRA_QUERY = "EXTRA_QUERY"
        private const val EXTRA_RESULT_COUNT = "EXTRA_RESULT_COUNT"
        fun start(context: Context, record: MsgIndexRecord) {
            val intent = Intent()
            intent.setClass(context, GlobalSearchDetailActivity::class.java)
            intent.putExtra(EXTRA_SESSION_TYPE, record.sessionType.value)
            intent.putExtra(EXTRA_SESSION_ID, record.sessionId)
            intent.putExtra(EXTRA_QUERY, record.query)
            intent.putExtra(EXTRA_RESULT_COUNT, record.count)
            context.startActivity(intent)
        }
    }
}