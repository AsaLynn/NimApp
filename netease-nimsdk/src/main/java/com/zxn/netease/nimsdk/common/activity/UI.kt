package com.zxn.netease.nimsdk.common.activity

import android.R
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.zxn.mvvm.view.BaseActivity
import com.zxn.netease.nimsdk.common.fragment.TFragment
import com.zxn.netease.nimsdk.common.util.sys.ReflectionUtil
import java.util.*

abstract class UI : BaseActivity<Nothing>() {
    override val layoutResId: Int
        get() = 0


    private var destroyed = false
    lateinit var toolBar: Toolbar

    override fun createObserver() {

    }

    override fun onInitView() {

    }

    override fun registerEventBus(isRegister: Boolean) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(mainLooper)
    }

    override fun onBackPressed() {
        invokeFragmentManagerNoteStateNotSaved()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                onNavigateUpClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setToolBar(toolBarId: Int, options: ToolBarOptions) {
        toolBar = findViewById(toolBarId)
        if (options.titleId != 0) {
            toolBar.setTitle(options.titleId)
        }
        if (!TextUtils.isEmpty(options.titleString)) {
            toolBar.setTitle(options.titleString)
        }
        setSupportActionBar(toolBar)
        if (options.isNeedNavigate) {
            toolBar.setNavigationIcon(options.navigateId)
            //toolbar.setContentInsetStartWithNavigation(0);
            toolBar.setNavigationOnClickListener(View.OnClickListener { onNavigateUpClicked() })
        }
    }

    fun setToolBar(toolbarId: Int, titleId: Int) {
        toolBar = findViewById(toolbarId)
        toolBar.setTitle(titleId)
        setSupportActionBar(toolBar)
    }

    val toolBarHeight: Int
        get() = if (toolBar != null) {
            toolBar!!.height
        } else 0

    fun onNavigateUpClicked() {
        onBackPressed()
    }

    override fun setTitle(title: CharSequence) {
        super.setTitle(title)
        if (toolBar != null) {
            toolBar!!.title = title
        }
    }

    fun setSubTitle(subTitle: String?) {
        if (toolBar != null) {
            toolBar!!.subtitle = subTitle
        }
    }

      protected lateinit var handler: Handler


    protected fun showKeyboard(isShow: Boolean) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (isShow) {
            if (currentFocus == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            } else {
                imm.showSoftInput(currentFocus, 0)
            }
        } else {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    /**
     * 延时弹出键盘
     *
     * @param focus 键盘的焦点项
     */
    protected fun showKeyboardDelayed(focus: View?) {
        focus?.requestFocus()
        handler!!.postDelayed({
            if (focus == null || focus.isFocused) {
                showKeyboard(true)
            }
        }, 200)
    }

    val isDestroyedCompatible: Boolean
        get() = if (Build.VERSION.SDK_INT >= 17) {
            isDestroyedCompatible17
        } else {
            destroyed || super.isFinishing()
        }

    @get:TargetApi(17)
    private val isDestroyedCompatible17: Boolean
        private get() = super.isDestroyed()

    /**
     * fragment management
     */
    fun addFragment(fragment: TFragment): TFragment? {
        val fragments: MutableList<TFragment> = ArrayList(1)
        fragments.add(fragment)
        val fragments2 = addFragments(fragments)
        return fragments2[0]
    }

    fun addFragments(fragments: List<TFragment>): List<TFragment?> {
        val fragments2: ArrayList<TFragment> = ArrayList(fragments.size)

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        var commit = false
        for (i in fragments.indices) {
            // install
            val fragment = fragments[i]
            val id = fragment.containerId

            // exists
            var fragment2 = fm.findFragmentById(id) as TFragment?
            if (fragment2 == null) {
                fragment2 = fragment
                transaction.add(id, fragment)
                commit = true
            }
            fragments2.add(i, fragment2)
        }
        if (commit) {
            try {
                transaction.commitAllowingStateLoss()
            } catch (e: Exception) {
            }
        }
        return fragments2
    }

    fun switchContent(fragment: TFragment): TFragment {
        return switchContent(fragment, false)
    }

    protected fun switchContent(fragment: TFragment, needAddToBackStack: Boolean): TFragment {
        val fm = supportFragmentManager
        val fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(fragment.containerId, fragment)
        if (needAddToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        try {
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
        }
        return fragment
    }

    protected open fun displayHomeAsUpEnabled(): Boolean {
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MENU) {
            onMenuKeyDown()
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    protected fun onMenuKeyDown(): Boolean {
        return false
    }

    private fun invokeFragmentManagerNoteStateNotSaved() {
        val fm = supportFragmentManager
        ReflectionUtil.invokeMethod(fm, "noteStateNotSaved", null)
    }

    protected fun switchFragmentContent(fragment: TFragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.replace(fragment.containerId, fragment)
        try {
            transaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun isCompatible(apiLevel: Int): Boolean {
        return Build.VERSION.SDK_INT >= apiLevel
    }

    protected fun <T : View?> findView(resId: Int): T {
        return findViewById<View>(resId) as T
    }

}