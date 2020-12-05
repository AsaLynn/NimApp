package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter

import android.animation.Animator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zxn.netease.nimsdk.common.ui.recyclerview.animation.*
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.LoadMoreView
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.SimpleLoadMoreView
import com.zxn.netease.nimsdk.common.ui.recyclerview.util.RecyclerViewUtil
import java.util.*
import kotlin.collections.ArrayList

/**
 * 消息适配器基类.
 *
 * @param <T>
 * @param <K> ViewHolder
</K></T> */
abstract class BaseFetchLoadAdapter<T, K : BaseViewHolder?>(
    recyclerView: RecyclerView,
    layoutResId: Int,
    data: List<T>?
) : RecyclerView.Adapter<K>(), IRecyclerView, IAnimationType {

    /**
     * 获取条目的总数量
     *
     * @return
     */
    override fun getItemCount(): Int {
        return if (emptyViewCount == 1) {
            1
        } else {
            fetchMoreViewCount + mData.size + loadMoreViewCount
        }
    }

    /**
     * 获取条目的类型.
     *
     * @param position
     * @return
     */
    override fun getItemViewType(position: Int): Int {
        if (emptyViewCount == 1) {
            return IRecyclerView.EMPTY_VIEW
        }

        // fetch
        autoRequestFetchMoreData(position)
        // load
        autoRequestLoadMoreData(position)
        val fetchMoreCount = fetchMoreViewCount
        return if (position < fetchMoreCount) {
            Log.d(TAG, "FETCH pos=$position")
            IRecyclerView.FETCHING_VIEW
        } else {
            val adjPosition = position - fetchMoreCount
            val adapterCount = mData.size
            if (adjPosition < adapterCount) {
                Log.d(TAG, "DATA pos=$position")
                getDefItemViewType(adjPosition)
            } else {
                Log.d(TAG, "LOAD pos=$position")
                IRecyclerView.LOADING_VIEW
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): K {
        mContext = parent.context
        mLayoutInflater = LayoutInflater.from(mContext)
        return when (viewType) {
            IRecyclerView.FETCHING_VIEW -> getFetchingView(parent)
            IRecyclerView.LOADING_VIEW -> getLoadingView(parent)
            IRecyclerView.EMPTY_VIEW -> createBaseViewHolder(mEmptyView)
            else -> onCreateDefViewHolder(parent, viewType)
        }
    }

    /**
     * To bind different types of holder and solve different the bind events
     * 绑定不同类型的持有者并解决不同的绑定事件
     *
     * @param holder
     * @param positions
     * @see .getDefItemViewType
     */
    override fun onBindViewHolder(holder: K, positions: Int) {
        when (holder!!.itemViewType) {
            IRecyclerView.LOADING_VIEW -> mLoadMoreView.convert(holder)
            IRecyclerView.FETCHING_VIEW -> mFetchMoreView.convert(holder)
            IRecyclerView.EMPTY_VIEW, IRecyclerView.HEADER_VIEW -> {
            }
            else -> convert(
                holder,
                mData[holder.layoutPosition - fetchMoreViewCount],
                positions,
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
    override fun onViewAttachedToWindow(holder: K) {
        super.onViewAttachedToWindow(holder)
        val type = holder!!.itemViewType
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
            val gridManager = manager
            gridManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = getItemViewType(position)
                    return if (mSpanSizeLookup == null) {
                        if (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW) gridManager.spanCount else 1
                    } else {
                        if (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW) gridManager
                            .spanCount else mSpanSizeLookup.getSpanSize(
                            gridManager,
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

    protected var mRecyclerView: RecyclerView?
    private var mFetching = false
    private var mFetchMoreEnable = false
    private var mNextFetchEnable = false
    private var mFirstFetchSuccess = true

    /**
     * 距离顶部多少条就开始拉取数据了
     */
    private var mAutoFetchMoreSize = 1
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
     * animation:动画.
     */
    private var mAnimationShowFirstOnly = true
    private var mOpenAnimationEnable = false
    private val mInterpolator: Interpolator = LinearInterpolator()
    private var mAnimationDuration = 200
    private var mLastPosition = -1

    /**
     * AnimationType:动画类型.
     */
    private var mCustomAnimation: BaseAnimation? = null
    private var mSelectAnimation: BaseAnimation? = AlphaInAnimation()

    /**
     * empty:空视图.
     */
    private var mEmptyView: FrameLayout? = null
    private var mIsUseEmpty = true

    // basic
    protected var mContext: Context? = null
    protected var mLayoutResId = 0
    protected var mLayoutInflater: LayoutInflater? = null
    @JvmField
    protected var mData: MutableList<T>
    private var isScrolling = false

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper      A fully initialized helper.
     * @param item        the item that needs to be displayed.
     * @param position    the item position
     * @param isScrolling RecyclerView is scrolling
     */
    protected abstract fun convert(helper: K, item: T, position: Int, isScrolling: Boolean)
    override val headerLayoutCount: Int
        get() = fetchMoreViewCount

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
        if (mData.size == 0 && mFirstFetchSuccess) {
            return  // 都还没有数据，不自动触发加载，等外部塞入数据后再加载
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
        addFrontData(newData) // notifyItemRangeInserted从顶部向下加入View，顶部View并没有改变
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
        if (mData.size == 0) {
            mFirstFetchSuccess = false // 首次加载失败
        }
        mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_FAIL
        notifyItemChanged(0)
    }

    /**
     * *********************************** load more 底部上拉加载 ***********************************
     */
    fun setLoadMoreView(loadingView: LoadMoreView) {
        mLoadMoreView = loadingView // 自定义View
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
            return if (mData.size == 0) {
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
        if (mData.size == 0 && mFirstLoadSuccess) {
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
        notifyItemChanged(fetchMoreViewCount + mData.size)
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
            notifyItemRemoved(fetchMoreViewCount + mData.size)
        } else {
            mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_END
            notifyItemChanged(fetchMoreViewCount + mData.size)
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
        if (mData.size == 0) {
            mFirstLoadSuccess = false // 首次加载失败
        }
        mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_FAIL
        notifyItemChanged(fetchMoreViewCount + mData.size)
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
                notifyItemRemoved(fetchMoreViewCount + mData.size)
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemInserted(fetchMoreViewCount + mData.size)
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
        mData = ((data ?: ArrayList()) as MutableList<T>?)!!
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
        mData.clear()
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
        mData.add(position, item)
        notifyItemInserted(position + fetchMoreViewCount)
    }

    /**
     * add new data in to certain location
     *
     * @param position
     */
    fun addData(position: Int, data: List<T>) {
        if (0 <= position && position < mData.size) {
            mData.addAll(position, data)
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
        val item = mData[position]
        mData.removeAt(position)
        notifyItemRemoved(position + headerLayoutCount)
        onRemove(item)
    }

    protected open fun onRemove(item: T) {}

    /**
     * add new data to head location
     */
    fun addFrontData(data: List<T>?) {
        if (data == null || data.isEmpty()) {
            return
        }
        mData.addAll(0, data)
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
        mData.addAll(newData)
        notifyItemRangeInserted(mData.size - newData.size + fetchMoreViewCount, newData.size)
    }

    fun appendData(newData: T) {
        val data: MutableList<T> = ArrayList(1)
        data.add(newData)
        appendData(data)
    }

    /**
     * Get the data of list
     *
     * @return
     */
    val data: List<T>
        get() = mData

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    fun getItem(position: Int): T {
        return mData[position]
    }

    val dataSize: Int
        get() = mData.size
    val bottomDataPosition: Int
        get() = headerLayoutCount + mData.size - 1

    fun notifyDataItemChanged(dataIndex: Int) {
        notifyItemChanged(headerLayoutCount + dataIndex)
    }

    /**
     * *********************************** ViewHolder/ViewType ***********************************
     */
    protected open fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): K {
        return createBaseViewHolder(parent, mLayoutResId)
    }

    protected fun createBaseViewHolder(parent: ViewGroup?, layoutResId: Int): K {
        return createBaseViewHolder(getItemView(layoutResId, parent))
    }

    /**
     * @param layoutResId ID for an XML layout resource to load
     * @param parent      Optional view to be the parent of the generated hierarchy or else simply an object that
     * provides a set of LayoutParams values for root of the returned
     * hierarchy
     * @return view will be return
     */
    protected fun getItemView(layoutResId: Int, parent: ViewGroup?): View {
        return mLayoutInflater!!.inflate(layoutResId, parent, false)
    }

    /**
     * if you want to use subclass of BaseViewHolder in the adapter,
     * you must override the method to create new ViewHolder.
     *
     * @param view view
     * @return new ViewHolder
     */
    protected fun createBaseViewHolder(view: View?): K {
        return BaseViewHolder(view) as K
    }

    protected open fun getDefItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    private fun getLoadingView(parent: ViewGroup): K {
        val view = getItemView(mLoadMoreView.layoutId, parent)
        val holder = createBaseViewHolder(view)
        holder!!.itemView.setOnClickListener {
            if (mLoadMoreView.loadMoreStatus == LoadMoreView.STATUS_FAIL) {
                mLoadMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemChanged(fetchMoreViewCount + mData.size)
            }
        }
        return holder
    }

    private fun getFetchingView(parent: ViewGroup): K {
        val view = getItemView(mFetchMoreView.layoutId, parent)
        val holder = createBaseViewHolder(view)
        holder!!.itemView.setOnClickListener {
            if (mFetchMoreView.loadMoreStatus == LoadMoreView.STATUS_FAIL) {
                mFetchMoreView.loadMoreStatus = LoadMoreView.STATUS_DEFAULT
                notifyItemChanged(0)
            }
        }
        return holder
    }

    /**
     * 当设置为true时, 条目将使用所有跨度区域进行布局. 这意味着, 如果定向
     * 是垂直的, 视图将有全宽度; 如果方向是水平的, 视图将
     * 有完整的高度
     * 如果保持视图使用交错网格布局管理器，则应使用所有跨度区域
     *
     * @param holder 如果此项应遍历所有跨度
     */
    protected fun setFullSpan(holder: RecyclerView.ViewHolder) {
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val params = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }

    private val mSpanSizeLookup: SpanSizeLookup? = null

    interface SpanSizeLookup {
        fun getSpanSize(gridLayoutManager: GridLayoutManager?, position: Int): Int
    }
    /**
     * *********************************** EmptyView ***********************************
     */
    /**
     * if mEmptyView will be return 1 or not will be return 0
     *
     * @return
     */
    val emptyViewCount: Int
        get() {
            if (mEmptyView == null || mEmptyView!!.childCount == 0) {
                return 0
            }
            if (!mIsUseEmpty) {
                return 0
            }
            return if (mData.size != 0) {
                0
            } else 1
        }

    fun setEmptyView(emptyView: View) {
        var insert = false
        if (mEmptyView == null) {
            mEmptyView = FrameLayout(emptyView.context)
            mEmptyView!!.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            insert = true
        }
        mEmptyView!!.removeAllViews()
        mEmptyView!!.addView(emptyView)
        mIsUseEmpty = true
        if (insert) {
            if (emptyViewCount == 1) {
                notifyItemInserted(0)
            }
        }
    }

    /**
     * Set whether to use empty view
     *
     * @param isUseEmpty
     */
    fun isUseEmpty(isUseEmpty: Boolean) {
        mIsUseEmpty = isUseEmpty
    }

    /**
     * When the current adapter is empty, the BaseQuickAdapter can display a special view
     * called the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    val emptyView: View?
        get() = mEmptyView
    /**
     * *********************************** 动画 ***********************************
     */
    /**
     * Set the view animation type.
     *
     * @param animationType One of [.ALPHAIN], [.SCALEIN], [.SLIDEIN_BOTTOM], [.SLIDEIN_LEFT], [.SLIDEIN_RIGHT].
     */
    fun openLoadAnimation(@AnimationType animationType: Int) {
        mOpenAnimationEnable = true
        mCustomAnimation = null
        when (animationType) {
            IAnimationType.ALPHAIN -> mSelectAnimation = AlphaInAnimation()
            IAnimationType.SCALEIN -> mSelectAnimation = ScaleInAnimation()
            IAnimationType.SLIDEIN_BOTTOM -> mSelectAnimation = SlideInBottomAnimation()
            IAnimationType.SLIDEIN_LEFT -> mSelectAnimation = SlideInLeftAnimation()
            IAnimationType.SLIDEIN_RIGHT -> mSelectAnimation = SlideInRightAnimation()
            else -> {
            }
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    fun openLoadAnimation(animation: BaseAnimation?) {
        mOpenAnimationEnable = true
        mCustomAnimation = animation
    }

    /**
     * To open the animation when loading
     */
    fun openLoadAnimation() {
        mOpenAnimationEnable = true
    }

    /**
     * To close the animation when loading
     */
    fun closeLoadAnimation() {
        mOpenAnimationEnable = false
        mSelectAnimation = null
        mCustomAnimation = null
        mAnimationDuration = 0
    }

    /**
     * [.addAnimation]
     *
     * @param firstOnly true just show anim when first loading false show anim when load the data every time
     */
    fun setAnimationShowFirstOnly(firstOnly: Boolean) {
        mAnimationShowFirstOnly = firstOnly
    }

    /**
     * Sets the duration of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    fun setAnimationDuration(duration: Int) {
        mAnimationDuration = duration
    }

    /**
     * add animation when you want to show time
     *
     * @param holder
     */
    private fun addAnimation(holder: RecyclerView.ViewHolder) {
        if (mOpenAnimationEnable) {
            if (!mAnimationShowFirstOnly || holder.layoutPosition > mLastPosition) {
                val animation: BaseAnimation?
                animation = if (mCustomAnimation != null) {
                    mCustomAnimation
                } else {
                    mSelectAnimation
                }
                for (anim in animation!!.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.layoutPosition)
                }
                mLastPosition = holder.layoutPosition
            }
        }
    }

    /**
     * set anim to start when loading
     *
     * @param anim
     * @param index
     */
    protected fun startAnim(anim: Animator, index: Int) {
        anim.setDuration(mAnimationDuration.toLong()).start()
        anim.interpolator = mInterpolator
    }

    companion object {
        private val TAG = BaseFetchLoadAdapter::class.java.simpleName
    }

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     */
    init {
        mRecyclerView = recyclerView
        mData = ((data ?: ArrayList()) as MutableList<T>?)!!
        if (layoutResId != 0) {
            mLayoutResId = layoutResId
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            }
        })
        /**
         * 关闭默认viewholder item动画
         */
        RecyclerViewUtil.changeItemAnimation(recyclerView, false)
    }

}