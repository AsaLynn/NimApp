package com.zxn.netease.nimsdk.common.fragment

abstract class TabFragment : TFragment() {
    interface State {
        fun isCurrent(fragment: TabFragment?): Boolean
    }

    private var state: State? = null
    fun setState(state: State?) {
        this.state = state
    }

    /**
     * is current
     *
     * @return
     */
    protected val isCurrent: Boolean
        protected get() = state!!.isCurrent(this)

    /**
     * notify current
     */
    open fun onCurrent() {
        // NO OP
    }

    /**
     * leave current page
     */
    fun onLeave() {}

    /**
     * notify current scrolled
     */
    fun onCurrentScrolled() {
        // NO OP
    }

    open fun onCurrentTabClicked() {
        // NO OP
    }

    fun onCurrentTabDoubleTap() {
        // NO OP
    }
}