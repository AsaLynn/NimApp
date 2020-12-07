package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.LoadMoreView
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.SimpleLoadMoreView

/**
 * 下拉自动加载消息的适配器基类.
 *
 * @param <T>
 * @param <K> ViewHolder
</K></T> */
abstract class BaseFetchLoadAdapter<T, VH : BaseViewHolder>(
    recyclerView: RecyclerView,
    layoutResId: Int,
    data: MutableList<T>?
) : BaseRvAdapter<T, VH>(recyclerView, layoutResId, data) {

    /**
     * 获取条目的总数量
     * @return
     */
    override fun getItemCount(): Int {
        return if (emptyViewCount == 1) {
            1
        } else {
            headerLayoutCount + getDefItemCount() + loadMoreViewCount
        }
    }

    /**
     * 获取条目的类型.
     * @param position
     * @return
     */
    override fun getItemViewType(position: Int): Int {
        if (emptyViewCount == 1) {
            return IRecyclerView.EMPTY_VIEW
        }
        val hasHeader = hasHeaderLayout()
        if (hasHeader && position == 0) {
            return IRecyclerView.HEADER_VIEW
        } else {
            // fetch
            autoRequestFetchMoreData(position)
            // load
            autoRequestLoadMoreData(position)
            //val fetchMoreCount = fetchMoreViewCount
            val fetchMoreCount = headerLayoutCount
            return if (position < fetchMoreCount) {
                Log.d(TAG, "FETCH pos=$position")
                IRecyclerView.FETCHING_VIEW
            } else {
                val adjPosition = position - fetchMoreCount
                val adapterCount = data.size
                if (adjPosition < adapterCount) {
                    Log.d(TAG, "DATA pos=$position")
                    getDefItemViewType(adjPosition)
                } else {
                    Log.d(TAG, "LOAD pos=$position")
                    IRecyclerView.LOADING_VIEW
                }
            }
        }
    }

    /**
     * 创建ViewHolder.
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            IRecyclerView.FETCHING_VIEW -> getFetchingView(parent)
            IRecyclerView.LOADING_VIEW -> getLoadingView(parent)
            IRecyclerView.EMPTY_VIEW -> createBaseViewHolder(mEmptyView)
            IRecyclerView.HEADER_VIEW -> createBaseViewHolder(mHeaderLayout)
            else -> onCreateDefViewHolder(parent, viewType)
        }
    }

    /**
     * To bind different types of holder and solve different the bind events
     * 绑定不同类型的持有者并解决不同的绑定事件
     *
     * @param holder
     * @param position
     * @see .getDefItemViewType
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder.itemViewType) {
            IRecyclerView.LOADING_VIEW -> mLoadMoreView.convert(holder)
            IRecyclerView.FETCHING_VIEW -> mFetchMoreView.convert(holder)
            IRecyclerView.EMPTY_VIEW, IRecyclerView.HEADER_VIEW -> {
            }
            //else -> convert(holder, data[holder.layoutPosition - fetchMoreViewCount], position, isScrolling)
            else -> convert(
                holder,
                data[holder.layoutPosition - headerLayoutCount],
                position,
                isScrolling
            )
        }
    }

    /**
     * 当此适配器创建的视图已附加到窗口时调用。
     * 简单解决项目将使用所有布局
     * [.setFullSpan]
     *
     * @param holder
     */
    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        val type = holder.itemViewType
        if (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW) {
            setFullSpan(holder)
        } else {
            addAnimation(holder)
        }
    }

    /**
     * 当RecyclerView开始观察此适配器时调用。
     * @param recyclerView
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = getItemViewType(position)
                    return if (mSpanSizeLookup == null) {
                        if (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW) manager.spanCount else 1
                    } else {
                        if (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW) manager
                            .spanCount else mSpanSizeLookup.getSpanSize(
                            manager,
                            position - fetchMoreViewCount
                        )
                    }
                }
            }
        }
    }

    /**
     * fetch more:获取更多的监听.
     */
    interface RequestFetchMoreListener {
        fun onFetchMoreRequested()
    }

    private var mFetching = false
    private var mFetchMoreEnable = false
    private var mNextFetchEnable = false
    private var mFirstFetchSuccess = true

    /**
     * 距离顶部多少条就开始拉取数据了
     */
    //private var mAutoFetchMoreSize = 1
    //private var mAutoFetchMoreSize = 2
    private var mAutoFetchMoreSize = if (hasHeaderLayout()) 2 else 1
    private var mRequestFetchMoreListener: RequestFetchMoreListener? = null
    private var mFetchMoreView: LoadMoreView = SimpleLoadMoreView()

    /**
     * load more:加载更多的监听.
     */
    interface RequestLoadMoreListener {
        fun onLoadMoreRequested()
    }

    private var mLoading = false
    private var mNextLoadEnable = false

    /**
     * Returns the enabled status for load more.
     *
     * @return True if load more is enabled, false otherwise.
     */
    var isLoadMoreEnable = false
        private set
    private var mFirstLoadSuccess = true

    /**
     * 距离底部多少条就开始加载数据了
     */
    private var mAutoLoadMoreSize = 1
    private var mRequestLoadMoreListener: RequestLoadMoreListener? = null
    private var mLoadMoreView: LoadMoreView = SimpleLoadMoreView()


    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper      A fully initialized helper.
     * @param item        the item that needs to be displayed.
     * @param position    the item position
     * @param isScrolling RecyclerView is scrolling
     */
    protected abstract fun convert(helper: VH, item: T, position: Int, isScrolling: Boolean)

    override val headerLayoutCount: Int
        get() {
            return if (hasHeaderLayout()) {
                1 + fetchMoreViewCount
            } else {
                fetchMoreViewCount
            }
        }

    /**
     * *********************************** fetch more 顶部下拉加载 ***********************************
     */
    fun setOnFetchMoreListener(requestFetchMoreListener: RequestFetchMoreListener?) {
        mRequestFetchMoreListener = requestFetchMoreListener
        mNextFetchEnable = true
        mFetchMoreEnable = true
        mFetching = false
    }

    fun setAutoFetchMoreSize(autoFetchMoreSize: Int) {
        if (autoFetchMoreSize > 1) {
            mAutoFetchMoreSize = autoFetchMoreSize
        }
    }

    fun setFetchMoreView(fetchMoreView: LoadMoreView) {
        mFetchMoreView = fetchMoreView // 自定义View
    }

    private val fetchMoreViewCount: Int
        private get() {
            if (mRequestFetchMoreListener == null || !mFetchMoreEnable) {
                return 0
            }
            return if (!mNextFetchEnable && mFetchMoreView.isLoadEndMoreGone) {
                0
            } else 1
        }

    /**
     * 列表滑动时自动拉取数据
     *
     * @param position
     */
    private fun autoRequestFetchMoreData(position: Int) {
        if (fetchMoreViewCount == 0) {
            return
        }
        if (position > mAutoFetchMoreSize - 1) {
            return
        }
        if (mFetchMoreView.loadMoreStatus != LoadMoreView.STATUS_DEFAULT) {
            return
        }
        // 都还没有数据，不自动触发加载，等外部塞入数据后再加载
        if (data.size == 0 && mFirstFetchSuccess) {
            return
        }
        Log.d(TAG, "auto fetch, pos=$position")
        mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_LOADING
        if (!mFetching) {
            mFetching = true
            mRequestFetchMoreListener!!.onFetchMoreRequested()
        }
    }

    /**
     * fetch complete
     */
    fun fetchMoreComplete(newData: List<T>) {
        addFrontData(newData)
        // notifyItemRangeInserted从顶部向下加入View，顶部View并没有改变
        if (fetchMoreViewCount == 0) {
            return
        }
        fetchMoreComplete(newData.size)
    }

    fun fetchMoreComplete(newDataSize: Int) {
        if (fetchMoreViewCount == 0) {
            return
        }
        mFetching = false
        mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        notifyItemChanged(0)

        // 定位到insert新消息前的top消息位置。必须移动，否则还在顶部，会继续fetch!!!
        if (mRecyclerView != null) {
            val layoutManager = mRecyclerView!!.layoutManager
            // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
            if (layoutManager is LinearLayoutManager) {
                //获取第一个可见view的位置
                val firstItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (firstItemPosition == 0) {
                    // 最顶部可见的View已经是FetchMoreView了，那么add数据&局部刷新后，要进行定位到上次的最顶部消息。
                    mRecyclerView!!.scrollToPosition(newDataSize + fetchMoreViewCount)
                }
            } else {
                mRecyclerView!!.scrollToPosition(newDataSize)
            }
        }
    }

    /**
     * fetch end, no more data
     *
     * @param data last load data to add
     * @param gone if true gone the fetch more view
     */
    fun fetchMoreEnd(data: List<T>?, gone: Boolean) {
        addFrontData(data)
        if (fetchMoreViewCount == 0) {
            return
        }
        mFetching = false
        mNextFetchEnable = false
        mFetchMoreView.setLoadMoreEndGone(gone)
        if (gone) {
            notifyItemRemoved(0)
        } else {
            mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_END
            notifyItemChanged(0)
        }
    }

    /**
     * fetch failed
     */
    fun fetchMoreFailed() {
        if (fetchMoreViewCount == 0) {
            return
        }
        mFetching = false
        if (data.size == 0) {
            mFirstFetchSuccess = false // 首次加载失败
        }
        mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_FAIL
        notifyItemChanged(0)
    }

    /**
     * *********************************** load more 底部上拉加载 ***********************************
     */
    fun setLoadMoreView(loadingView: LoadMoreView) {
        // 自定义View
        mLoadMoreView = loadingView
    }

    fun setOnLoadMoreListener(requestLoadMoreListener: RequestLoadMoreListener?) {
        mRequestLoadMoreListener = requestLoadMoreListener
        mNextLoadEnable = true
        isLoadMoreEnable = true
        mLoading = false
    }

    fun setAutoLoadMoreSize(autoLoadMoreSize: Int) {
        if (autoLoadMoreSize > 1) {
            mAutoLoadMoreSize = autoLoadMoreSize
        }
    }

    private val loadMoreViewCount: Int
        private get() {
            if (mRequestLoadMoreListener == null || !isLoadMoreEnable) {
                return 0
            }
            if (!mNextLoadEnable && mLoadMoreView.isLoadEndMoreGone) {
                return 0
            }
            return if (data.size == 0) {
                0
            } else 1
        }

    /**
     * 列表滑动时自动加载数据
     *
     * @param position
     */
    private fun autoRequestLoadMoreData(position: Int) {
        if (loadMoreViewCount == 0) {
            return
        }
        if (position < itemCount - mAutoLoadMoreSize) {
            return
        }
        if (mLoadMoreView.loadMoreStatus != LoadMoreView.STATUS_DEFAULT) {
            return
        }
        if (data.size == 0 && mFirstLoadSuccess) {
            return  // 都还没有数据，不自动触发加载，等外部塞入数据后再加载
        }
        Log.d(TAG, "auto load, pos=$position")
        mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_LOADING
        if (!mLoading) {
            mLoading = true
            mRequestLoadMoreListener!!.onLoadMoreRequested()
        }
    }

    /**
     * load complete
     */
    fun loadMoreComplete(newData: List<T>?) {
        appendData(newData)
        loadMoreComplete()
    }

    fun loadMoreComplete() {
        if (loadMoreViewCount == 0) {
            return
        }
        mLoading = false
        mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        notifyItemChanged(fetchMoreViewCount + data.size)
    }

    /**
     * load end, no more data
     *
     * @param data last data to append
     * @param gone if true gone the load more view
     */
    fun loadMoreEnd(data: List<T>?, gone: Boolean) {
        appendData(data)
        if (loadMoreViewCount == 0) {
            return
        }
        mLoading = false
        mNextLoadEnable = false
        mLoadMoreView.setLoadMoreEndGone(gone)
        if (gone) {
            notifyItemRemoved(fetchMoreViewCount + this.data.size)
        } else {
            mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_END
            notifyItemChanged(fetchMoreViewCount + this.data.size)
        }
    }

    /**
     * load failed
     */
    fun loadMoreFail() {
        if (loadMoreViewCount == 0) {
            return
        }
        mLoading = false
        if (data.size == 0) {
            mFirstLoadSuccess = false // 首次加载失败
        }
        mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_FAIL
        notifyItemChanged(fetchMoreViewCount + data.size)
    }

    /**
     * Set the enabled state of load more.
     *
     * @param enable True if load more is enabled, false otherwise.
     */
    fun setEnableLoadMore(enable: Boolean) {
        val oldLoadMoreCount = loadMoreViewCount
        isLoadMoreEnable = enable
        val newLoadMoreCount = loadMoreViewCount
        if (oldLoadMoreCount == 1) {
            if (newLoadMoreCount == 0) {
                notifyItemRemoved(fetchMoreViewCount + data.size)
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemInserted(fetchMoreViewCount + data.size)
            }
        }
    }

    /**
     * *********************************** 数据源管理 ***********************************
     */
    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    fun setNewData(data: List<T>?) {
        this.data = ((data ?: ArrayList()) as MutableList<T>?)!!
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true
            isLoadMoreEnable = true
            mLoading = false
            mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        }
        if (mRequestFetchMoreListener != null) {
            mNextFetchEnable = true
            mFetchMoreEnable = true
            mFetching = false
            mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        }
        mLastPosition = -1
        notifyDataSetChanged()
    }

    /**
     * clear data before reload
     */
    fun clearData() {
        data.clear()
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true
            mLoading = false
            mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        }
        if (mRequestFetchMoreListener != null) {
            mNextFetchEnable = true
            mFetching = false
            mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
        }
        mLastPosition = -1
        notifyDataSetChanged()
    }

    /**
     * insert  a item associated with the specified position of adapter
     *
     * @param position
     * @param item
     */
    fun add(position: Int, item: T) {
        data.add(position, item)
        notifyItemInserted(position + fetchMoreViewCount)
    }

    /**
     * add new data in to certain location
     *
     * @param position
     */
    fun addData(position: Int, data: List<T>) {
        if (0 <= position && position < data.size) {
            this.data.addAll(position, data)
            notifyItemRangeInserted(fetchMoreViewCount + position, data.size)
        } else {
            throw ArrayIndexOutOfBoundsException("inserted position most greater than 0 and less than data size")
        }
    }

    /**
     * remove the item associated with the specified position of adapter
     *
     * @param position
     */
    fun remove(position: Int) {
        val item = this.data[position]
        data.removeAt(position)
        notifyItemRemoved(position + headerLayoutCount)
        onRemove(item)
    }


    /**
     * add new data to head location
     */
    fun addFrontData(data: List<T>?) {
        if (data == null || data.isEmpty()) {
            return
        }
        this.data.addAll(0, data)
        notifyItemRangeInserted(
            fetchMoreViewCount,
            data.size
        ) // add到FetchMoreView之下，保持FetchMoreView在顶部
    }

    /**
     * additional data;
     *
     * @param newData
     */
    fun appendData(newData: List<T>?) {
        if (newData == null || newData.isEmpty()) {
            return
        }
        data.addAll(newData)
        notifyItemRangeInserted(data.size - newData.size + fetchMoreViewCount, newData.size)
    }

    fun appendData(newData: T) {
        val data: MutableList<T> = ArrayList(1)
        data.add(newData)
        appendData(data)
    }

    val bottomDataPosition: Int
        get() = headerLayoutCount + data.size - 1

    fun notifyDataItemChanged(dataIndex: Int) {
        notifyItemChanged(headerLayoutCount + dataIndex)
    }

    private fun getLoadingView(parent: ViewGroup): VH {
        val view = getItemView(mLoadMoreView.layoutId, parent)
        val holder = createBaseViewHolder(view)
        holder!!.itemView.setOnClickListener {
            if (mLoadMoreView.loadMoreStatus == LoadMoreView.STATUS_FAIL) {
                mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemChanged(fetchMoreViewCount + data.size)
            }
        }
        return holder
    }

    private fun getFetchingView(parent: ViewGroup): VH {
        val view = getItemView(mFetchMoreView.layoutId, parent)
        val holder = createBaseViewHolder(view)
        holder.itemView.setOnClickListener {
            if (mFetchMoreView.loadMoreStatus == LoadMoreView.STATUS_FAIL) {
                mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemChanged(0)
            }
        }
        return holder
    }

    private val mSpanSizeLookup: SpanSizeLookup? = null

    interface SpanSizeLookup {
        fun getSpanSize(gridLayoutManager: GridLayoutManager?, position: Int): Int
    }

}