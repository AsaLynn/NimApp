package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter

import android.animation.Animator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zxn.netease.nimsdk.common.ui.recyclerview.animation.*
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder

/**
 *  Created by zxn on 2020/12/5.
 */
abstract class BaseRvAdapter<T,VH : BaseViewHolder>
@JvmOverloads constructor(@LayoutRes private val layoutResId: Int,
                          data: MutableList<T>? = null) : RecyclerView.Adapter<VH>(), IRecyclerView,
    IAnimationType {

    @JvmField
    protected var mData: MutableList<T> = data?: arrayListOf()

    protected var mRecyclerView: RecyclerView? = null

    protected var isScrolling = false

    // basic
    protected var mContext: Context? = null
    protected var mLayoutResId = 0
    protected var mLayoutInflater: LayoutInflater? = null

    /**
     * empty:空视图.
     */
    protected var mEmptyView: FrameLayout? = null
    private var mIsUseEmpty = true

    /**
     * animation:动画.
     */
    private var mAnimationShowFirstOnly = true
    private var mOpenAnimationEnable = false
    private val mInterpolator: Interpolator = LinearInterpolator()
    private var mAnimationDuration = 200
    protected var mLastPosition = -1

    /**
     * AnimationType:动画类型.
     */
    private var mCustomAnimation: BaseAnimation? = null
    private var mSelectAnimation: BaseAnimation? = AlphaInAnimation()


    /***************************** Public property settings *************************************/

    /**
     * data, Only allowed to get.
     * 数据, 只允许 get。
     */
    var data: MutableList<T> = data ?: arrayListOf()
        internal set

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
    protected fun addAnimation(holder: RecyclerView.ViewHolder) {
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
            return if (data.size != 0) {
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
     * *********************************** ViewHolder/ViewType ***********************************
     */
    protected open fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): VH {
        return createBaseViewHolder(parent, mLayoutResId)
    }

    protected fun createBaseViewHolder(parent: ViewGroup?, layoutResId: Int): VH {
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
    protected fun createBaseViewHolder(view: View?): VH {
        return BaseViewHolder(view) as VH
    }

    protected open fun getDefItemViewType(position: Int): Int {
        return super.getItemViewType(position)
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

    /**
     * *********************************** 数据源管理 ***********************************
     */

    protected open fun onRemove(item: T) {}

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     * data set.
     * @return The data at the specified position.
     */
    fun getItem(position: Int): T {
        return this.data[position]
    }

    val dataSize: Int
        get() = data.size


}