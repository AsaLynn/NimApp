package com.zxn.netease.nimsdk.business.session.module.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.session.helper.MessageHelper
import com.zxn.netease.nimsdk.business.session.module.Container
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderFactory
import com.zxn.netease.nimsdk.common.CommonUtil.isEmpty
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import java.util.*

/**
 * 消息列表适配器.
 */
class MsgAdapter(recyclerView: RecyclerView?, data: List<IMMessage?>?, container: Container) :
    BaseMultiItemFetchLoadAdapter<IMMessage?, BaseViewHolder?>(recyclerView, data) {

    private val holder2ViewType: MutableMap<Class<out MsgViewHolderBase?>, Int>
    var eventListener: ViewHolderEventListener? = null

    // 有文件传输，需要显示进度条的消息ID map
    private val progresses: MutableMap<String, Float>

    var uuid: String? = null

    val container: Container

    override fun getViewType(message: IMMessage?): Int {
        return holder2ViewType[MsgViewHolderFactory.getViewHolderByType(message)]!!
    }

    override fun getItemKey(item: IMMessage?): String? {
        return item?.uuid
    }

    fun deleteItem(message: IMMessage?, isRelocateTime: Boolean) {
        if (message == null) {
            return
        }
        var index = 0
        for (item in data) {
            if (item!!.isTheSame(message)) {
                break
            }
            ++index
        }
        if (index < dataSize) {
            remove(index)
            if (isRelocateTime) {
                relocateShowTimeItemAfterDelete(message, index)
            }
        }
    }

    fun deleteItems(msgList: List<IMMessage?>, isRelocateTime: Boolean) {
        if (isEmpty(msgList)) {
            return
        }
        var index = 0
        val deleteIndexList: MutableList<Int> = ArrayList(msgList.size)
        val msgUuidSet = MessageHelper.getUuidSet(msgList)
        val items = data
        for (item in items) {
            if (msgUuidSet.contains(item!!.uuid)) {
                deleteIndexList.add(index)
            }
            ++index
        }
        if (!deleteIndexList.isEmpty()) {
            if (isRelocateTime) {
                var toDeleteMsg: IMMessage?
                for (i in deleteIndexList.indices.reversed()) {
                    index = deleteIndexList[i]
                    toDeleteMsg = items[index]
                    remove(index)
                    relocateShowTimeItemAfterDelete(toDeleteMsg, index)
                }
            }
        }
    }

    fun deleteItemsRange(fromTime: Long, toTime: Long, isRelocateTime: Boolean) {
        if (toTime <= 0 || fromTime >= toTime) {
            return
        }
        val items = data
        if (isEmpty(items)) {
            return
        }
        var index: Int
        var item: IMMessage
        var itemTime: Long
        val itemIterator = items.listIterator(items.size)
        while (itemIterator.hasPrevious()) {
            try {
                index = itemIterator.previousIndex()
                item = itemIterator.previous()!!
                itemTime = item.time
                if (itemTime < toTime && itemTime > fromTime) {
                    itemIterator.remove()
                    notifyItemRemoved(index)
                    onRemove(item)
                    if (isRelocateTime) {
                        relocateShowTimeItemAfterDelete(item, index)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun getProgress(message: IMMessage): Float?  = progresses[message.uuid]


    fun putProgress(message: IMMessage, progress: Float) {
        progresses[message.uuid] = progress
    }

    /**
     * *********************** 时间显示处理 ***********************
     */
    private val timedItems // 需要显示消息时间的消息ID
            : MutableSet<String>
    private var lastShowTimeItem // 用于消息时间显示,判断和上条消息间的时间间隔
            : IMMessage? = null

    fun needShowTime(message: IMMessage?): Boolean {
        return timedItems.contains(message!!.uuid)
    }

    /**
     * 列表加入新消息时，更新时间显示
     */
    fun updateShowTimeItem(items: List<IMMessage>, fromStart: Boolean, update: Boolean) {
        var anchor = if (fromStart) null else lastShowTimeItem
        for (message in items) {
            if (setShowTimeFlag(message, anchor)) {
                anchor = message
            }
        }
        if (update) {
            lastShowTimeItem = anchor
        }
    }

    /**
     * 是否显示时间item
     */
    private fun setShowTimeFlag(message: IMMessage, anchor: IMMessage?): Boolean {
        var update = false
        if (hideTimeAlways(message)) {
            setShowTime(message, false)
        } else {
            if (anchor == null) {
                setShowTime(message, true)
                update = true
            } else {
                val time = anchor.time
                val now = message.time
                if (now - time == 0L) {
                    // 消息撤回时使用
                    setShowTime(message, true)
                    lastShowTimeItem = message
                    update = true
                } else if (now - time < NimUIKitImpl.getOptions().displayMsgTimeWithInterval) {
                    setShowTime(message, false)
                } else {
                    setShowTime(message, true)
                    update = true
                }
            }
        }
        return update
    }

    private fun setShowTime(message: IMMessage?, show: Boolean) {
        if (show) {
            timedItems.add(message!!.uuid)
        } else {
            timedItems.remove(message!!.uuid)
        }
    }

    private fun relocateShowTimeItemAfterDelete(messageItem: IMMessage?, index: Int) {
        // 如果被删的项显示了时间，需要继承
        if (needShowTime(messageItem)) {
            setShowTime(messageItem, false)
            if (dataSize > 0) {
                val nextItem: IMMessage?
                nextItem = if (index == dataSize) {
                    //删除的是最后一项
                    getItem(index - 1)
                } else {
                    //删除的不是最后一项
                    getItem(index)
                }

                // 增加其他不需要显示时间的消息类型判断
                if (hideTimeAlways(nextItem)) {
                    setShowTime(nextItem, false)
                    if (lastShowTimeItem != null && lastShowTimeItem != null && lastShowTimeItem!!.isTheSame(
                            messageItem
                        )
                    ) {
                        lastShowTimeItem = null
                        for (i in dataSize - 1 downTo 0) {
                            val item = getItem(i)
                            if (needShowTime(item)) {
                                lastShowTimeItem = item
                                break
                            }
                        }
                    }
                } else {
                    setShowTime(nextItem, true)
                    if (lastShowTimeItem == null
                        || lastShowTimeItem != null && lastShowTimeItem!!.isTheSame(messageItem)
                    ) {
                        lastShowTimeItem = nextItem
                    }
                }
            } else {
                lastShowTimeItem = null
            }
        }
    }

    private fun hideTimeAlways(message: IMMessage?): Boolean {
        return if (message!!.sessionType == SessionTypeEnum.ChatRoom) {
            true
        } else when (message.msgType) {
            MsgTypeEnum.notification -> true
            else -> false
        }
    }

    interface ViewHolderEventListener {
        // 长按事件响应处理
        fun onViewHolderLongClick(
            clickView: View?,
            viewHolderView: View?,
            item: IMMessage?
        ): Boolean

        // 发送失败或者多媒体文件下载失败指示按钮点击响应处理
        fun onFailedBtnClick(resendMessage: IMMessage?)

        // viewholder footer按钮点击，如机器人继续会话
        fun onFooterClick(message: IMMessage?)

        /**
         * 消息对应的复选框的状况变化时回调
         * 状态: true: 选中; false: 未被选中; null: 选则无效（复选框不可见，且状态重置为未被选中）
         *
         * @param index    消息在列表中的位置
         * @param newState 变化后的状态
         */
        fun onCheckStateChanged(index: Int, newState: Boolean?)
    }

    /**
     * 为了在实现ViewHolderEventListener时只需要复写需要的部分
     */
    open class BaseViewHolderEventListener : ViewHolderEventListener {
        override fun onViewHolderLongClick(
            clickView: View?,
            viewHolderView: View?,
            item: IMMessage?
        ): Boolean {
            return false
        }

        override fun onFailedBtnClick(resendMessage: IMMessage?) {}
        override fun onFooterClick(message: IMMessage?) {}
        override fun onCheckStateChanged(index: Int, newState: Boolean?) {}
    }

    init {
        timedItems = HashSet()
        progresses = HashMap()

        // view type, view holder
        holder2ViewType = HashMap()
        val holders = MsgViewHolderFactory.getAllViewHolders()
        var viewType = 0
        for (holder in holders) {
            viewType++
            addItemType(viewType, R.layout.nim_message_item, holder)
            holder2ViewType[holder] = viewType
        }
        this.container = container
    }
}