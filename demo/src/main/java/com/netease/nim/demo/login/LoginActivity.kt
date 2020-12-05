package com.netease.nim.demo.login

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import com.netease.nim.demo.DemoCache
import com.netease.nim.demo.DemoPrivatizationConfig
import com.netease.nim.demo.R
import com.netease.nim.demo.config.preference.Preferences
import com.netease.nim.demo.config.preference.UserPreferences
import com.netease.nim.demo.contact.ContactHttpClient
import com.netease.nim.demo.contact.ContactHttpClient.ContactHttpCallback
import com.netease.nim.demo.login.LoginActivity
import com.netease.nim.demo.main.activity.MainActivity
import com.netease.nim.demo.main.activity.PrivatizationConfigActivity
import com.netease.nimlib.sdk.*
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.auth.ClientType
import com.netease.nimlib.sdk.auth.LoginInfo
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions
import com.zxn.netease.nimsdk.common.ToastHelper
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.ui.dialog.DialogMaker
import com.zxn.netease.nimsdk.common.ui.dialog.EasyAlertDialogHelper
import com.zxn.netease.nimsdk.common.ui.widget.ClearableEditTextWithIcon
import com.zxn.netease.nimsdk.common.util.log.LogUtil
import com.zxn.netease.nimsdk.common.util.string.MD5
import com.zxn.netease.nimsdk.common.util.sys.NetworkUtil
import com.zxn.netease.nimsdk.common.util.sys.ScreenUtil
import kotlinx.android.synthetic.main.login_activity.*

/**
 * 登录/注册界面
 *
 */
class LoginActivity : UI(), View.OnKeyListener {
    private var rightTopBtn // ActionBar完成按钮
            : TextView? = null
    private var leftTopBtn: TextView? = null
    private var switchModeBtn // 注册/登录切换按钮
            : TextView? = null
    private var loginPasswordEdit: ClearableEditTextWithIcon? = null
    private var loginSubtypeEdit: ClearableEditTextWithIcon? = null
    private var registerAccountEdit: ClearableEditTextWithIcon? = null
    private var registerNickNameEdit: ClearableEditTextWithIcon? = null
    private var registerPasswordEdit: ClearableEditTextWithIcon? = null
    private var loginLayout: View? = null
    private var registerLayout: View? = null
    private var loginRequest: AbortableFuture<LoginInfo>? = null
    private var registerMode = false // 注册模式
    private var registerPanelInited = false // 注册面板是否初始化

    override val layoutResId: Int = R.layout.login_activity

    override fun onInitView() {
        val options: ToolBarOptions = NimToolBarOptions()
        options.isNeedNavigate = false
        options.logoId = R.drawable.actionbar_white_logo_space
        setToolBar(R.id.toolbar, options)
        onParseIntent()
        initRightTopBtn()
        initLeftTopBtn()
        setupLoginPanel()
        setupRegisterPanel()
    }

    override fun displayHomeAsUpEnabled(): Boolean {
        return false
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        return false
    }

    private fun onParseIntent() {
        if (!intent.getBooleanExtra(KICK_OUT, false)) {
            return
        }
        val desc = intent.getStringExtra(KICK_OUT_DESC)
        if (!TextUtils.isEmpty(desc)) {
            EasyAlertDialogHelper.showOneButtonDiolag(
                this@LoginActivity,
                getString(R.string.kickout_notify), desc, getString(R.string.ok),
                true, null
            )
            return
        }
        val type = NIMClient.getService(
            AuthService::class.java
        ).kickedClientType
        val customType = NIMClient.getService(
            AuthService::class.java
        ).kickedCustomClientType
        val client: String
        client = when (type) {
            ClientType.Web -> "网页端"
            ClientType.Windows, ClientType.MAC -> "电脑端"
            ClientType.REST -> "服务端"
            else -> "移动端"
        }
        EasyAlertDialogHelper.showOneButtonDiolag(
            this@LoginActivity,
            getString(R.string.kickout_notify), String.format(
                getString(R.string.kickout_content),
                client + customType
            ), getString(R.string.ok),
            true, null
        )
    }

    /**
     * ActionBar 右上角按钮
     */
    private fun initLeftTopBtn() {
        leftTopBtn = addRegisterLeftTopBtn(this, R.string.login_privatization_config_str)
        leftTopBtn!!.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    PrivatizationConfigActivity::class.java
                )
            )
        }
    }

    /**
     * ActionBar 右上角按钮
     */
    private fun initRightTopBtn() {
        rightTopBtn = addRegisterRightTopBtn(this, R.string.login)
        rightTopBtn!!.setOnClickListener {
            if (registerMode) {
                register()
            } else {
                //fakeLoginTest(); // 假登录代码示例
                login()
            }
        }
    }

    /**
     * 登录面板
     */
    private fun setupLoginPanel() {
        loginPasswordEdit = findView(R.id.edit_login_password)
        loginSubtypeEdit = findViewById(R.id.edit_login_subtype)
        loginAccountEdit?.filters = arrayOf<InputFilter>(LengthFilter(32))
        loginPasswordEdit?.filters = arrayOf<InputFilter>(LengthFilter(32))
        loginSubtypeEdit?.filters = arrayOf<InputFilter>(LengthFilter(32))
        loginAccountEdit?.addTextChangedListener(textWatcher)
        loginPasswordEdit?.addTextChangedListener(textWatcher)
        loginPasswordEdit?.setOnKeyListener(this)
        val account = Preferences.getUserAccount()
        account?.let {
            loginAccountEdit?.setText(it)
        }
    }

    /**
     * 注册面板
     */
    private fun setupRegisterPanel() {
        loginLayout = findView(R.id.login_layout)
        registerLayout = findView(R.id.register_layout)
        switchModeBtn = findView(R.id.register_login_tip)
        switchModeBtn?.visibility =
            if (DemoPrivatizationConfig.isPrivateDisable(this)) View.VISIBLE else View.GONE
        switchModeBtn?.setOnClickListener { _: View? -> switchMode() }
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (registerMode) {
                return
            }
            // 登录模式  ，更新右上角按钮状态
            val isEnable = loginAccountEdit!!.text!!.length > 0 &&
                    loginPasswordEdit!!.text!!.length > 0
            updateRightTopBtn(rightTopBtn, isEnable)
        }
    }

    private fun updateRightTopBtn(rightTopBtn: TextView?, isEnable: Boolean) {
        rightTopBtn!!.setText(R.string.done)
        rightTopBtn.setBackgroundResource(R.drawable.g_white_btn_selector)
        rightTopBtn.isEnabled = isEnable
        rightTopBtn.setTextColor(resources.getColor(R.color.color_blue_0888ff))
        rightTopBtn.setPadding(ScreenUtil.dip2px(10f), 0, ScreenUtil.dip2px(10f), 0)
    }

    /**
     * ***************************************** 登录 **************************************
     */
    private fun login() {
        DialogMaker.showProgressDialog(
            this,
            null,
            getString(R.string.logining),
            true
        ) { dialog: DialogInterface? ->
            if (loginRequest != null) {
                loginRequest!!.abort()
                onLoginDone()
            }
        }.setCanceledOnTouchOutside(false)
        // 云信只提供消息通道，并不包含用户资料逻辑。开发者需要在管理后台或通过服务器接口将用户帐号和token同步到云信服务器。
        // 在这里直接使用同步到云信服务器的帐号和token登录。
        // 这里为了简便起见，demo就直接使用了密码的md5作为token。
        // 如果开发者直接使用这个demo，只更改appkey，然后就登入自己的账户体系的话，需要传入同步到云信服务器的token，而不是用户密码。
        val account = loginAccountEdit!!.editableText.toString().toLowerCase()
        val token = tokenFromPassword(loginPasswordEdit!!.editableText.toString())
        var subtype = 0
        try {
            val editable = loginSubtypeEdit!!.editableText
            if (editable != null && editable.length > 0) {
                subtype = editable.toString().toInt()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        // 登录
        loginRequest = NimUIKit.login(
            LoginInfo(account, token, null, subtype),
            object : RequestCallback<LoginInfo?> {
                override fun onSuccess(param: LoginInfo?) {
                    LogUtil.i(TAG, "login success")
                    onLoginDone()
                    DemoCache.setAccount(account)
                    saveLoginInfo(account, token)
                    // 初始化消息提醒配置
                    initNotificationConfig()
                    // 进入主界面
                    MainActivity.start(this@LoginActivity, null)
                    finish()
                }

                override fun onFailed(code: Int) {
                    onLoginDone()
                    if (code == 302 || code == 404) {
                        ToastHelper.showToast(
                            this@LoginActivity,
                            R.string.login_failed
                        )
                    } else {
                        ToastHelper.showToast(
                            this@LoginActivity,
                            "登录失败: $code"
                        )
                    }
                }

                override fun onException(exception: Throwable) {
                    ToastHelper.showToast(
                        this@LoginActivity,
                        R.string.login_exception
                    )
                    onLoginDone()
                }
            })
    }

    private fun initNotificationConfig() {
        // 初始化消息提醒
        NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
        // 加载状态栏配置
        var statusBarNotificationConfig = UserPreferences.getStatusConfig()
        if (statusBarNotificationConfig == null) {
            statusBarNotificationConfig = DemoCache.notificationConfig
            UserPreferences.setStatusConfig(statusBarNotificationConfig)
        }
        // 更新配置
        NIMClient.updateStatusBarNotificationConfig(statusBarNotificationConfig)
    }

    private fun onLoginDone() {
        loginRequest = null
        DialogMaker.dismissProgressDialog()
    }

    private fun saveLoginInfo(account: String, token: String) {
        Preferences.saveUserAccount(account)
        Preferences.saveUserToken(token)
    }

    //DEMO中使用 username 作为 NIM 的account ，md5(password) 作为 token
    //开发者需要根据自己的实际情况配置自身用户系统和 NIM 用户系统的关系
    private fun tokenFromPassword(password: String): String {
        val appKey = readAppKey(this)
        val isDemo =
            "45c6af3c98409b18a84451215d0bdd6e" == appKey || "fe416640c8e8a72734219e1847ad2547" == appKey || "a24e6c8a956a128bd50bdffe69b405ff" == appKey
        return if (isDemo) MD5.getStringMD5(password) else password
    }

    /**
     * ***************************************** 注册 **************************************
     */
    private fun register() {
        if (!registerMode || !registerPanelInited) {
            return
        }
        if (!checkRegisterContentValid()) {
            return
        }
        if (!NetworkUtil.isNetAvailable(this@LoginActivity)) {
            ToastHelper.showToast(this@LoginActivity, R.string.network_is_not_available)
            return
        }
        DialogMaker.showProgressDialog(this, getString(R.string.registering), false)
        // 注册流程
        val account = registerAccountEdit!!.text.toString()
        val nickName = registerNickNameEdit!!.text.toString()
        val password = registerPasswordEdit!!.text.toString()
        ContactHttpClient.getInstance().register(account, nickName, password,
            object : ContactHttpCallback<Void?> {

                override fun onFailed(
                    code: Int,
                    errorMsg: String
                ) {
                    ToastHelper.showToast(
                        this@LoginActivity,
                        getString(
                            R.string.register_failed,
                            code.toString(),
                            errorMsg
                        )
                    )
                    DialogMaker.dismissProgressDialog()
                }

                override fun onSuccess(t: Void?) {
                    ToastHelper.showToast(
                        this@LoginActivity,
                        R.string.register_success
                    )
                    switchMode() // 切换回登录
                    loginAccountEdit!!.setText(account)
                    loginPasswordEdit!!.setText(password)
                    registerAccountEdit!!.setText("")
                    registerNickNameEdit!!.setText("")
                    registerPasswordEdit!!.setText("")
                    DialogMaker.dismissProgressDialog()
                }
            })
    }

    private fun checkRegisterContentValid(): Boolean {
        if (!registerMode || !registerPanelInited) {
            return false
        }
        // 帐号检查
        val account = registerAccountEdit!!.text.toString().trim { it <= ' ' }
        if (account.length <= 0 || account.length > 20) {
            ToastHelper.showToast(this, R.string.register_account_tip)
            return false
        }
        // 昵称检查
        val nick = registerNickNameEdit!!.text.toString().trim { it <= ' ' }
        if (nick.length <= 0 || nick.length > 10) {
            ToastHelper.showToast(this, R.string.register_nick_name_tip)
            return false
        }
        // 密码检查
        val password = registerPasswordEdit!!.text.toString().trim { it <= ' ' }
        if (password.length < 6 || password.length > 20) {
            ToastHelper.showToast(this, R.string.register_password_tip)
            return false
        }
        return true
    }

    /**
     * ***************************************** 注册/登录切换 **************************************
     */
    private fun switchMode() {
        registerMode = !registerMode
        if (registerMode && !registerPanelInited) {
            registerAccountEdit = findView(R.id.edit_register_account)
            registerNickNameEdit = findView(R.id.edit_register_nickname)
            registerPasswordEdit = findView(R.id.edit_register_password)
            registerAccountEdit?.setIconResource(R.drawable.user_account_icon)
            registerNickNameEdit?.setIconResource(R.drawable.nick_name_icon)
            registerPasswordEdit?.setIconResource(R.drawable.user_pwd_lock_icon)
            registerAccountEdit?.filters = arrayOf<InputFilter>(LengthFilter(20))
            registerNickNameEdit?.filters = arrayOf<InputFilter>(LengthFilter(10))
            registerPasswordEdit?.filters = arrayOf<InputFilter>(LengthFilter(20))
            registerAccountEdit?.addTextChangedListener(textWatcher)
            registerNickNameEdit?.addTextChangedListener(textWatcher)
            registerPasswordEdit?.addTextChangedListener(textWatcher)
            registerPanelInited = true
        }
        setTitle(if (registerMode) R.string.register else R.string.login)
        loginLayout!!.visibility =
            if (registerMode) View.GONE else View.VISIBLE
        registerLayout!!.visibility = if (registerMode) View.VISIBLE else View.GONE
        switchModeBtn!!.setText(if (registerMode) R.string.login_has_account else R.string.register)
        if (registerMode) {
            rightTopBtn!!.isEnabled = true
        } else {
            val isEnable = loginAccountEdit!!.text!!.length > 0 &&
                    loginPasswordEdit!!.text!!.length > 0
            rightTopBtn!!.isEnabled = isEnable
        }
    }

    fun addRegisterRightTopBtn(activity: UI, strResId: Int): TextView {
        val text = activity.resources.getString(strResId)
        val textView = findView<TextView>(R.id.action_bar_right_clickable_textview)
        textView.text = text
        textView.setBackgroundResource(R.drawable.register_right_top_btn_selector)
        textView.setPadding(ScreenUtil.dip2px(10f), 0, ScreenUtil.dip2px(10f), 0)
        return textView
    }

    fun addRegisterLeftTopBtn(activity: UI, strResId: Int): TextView {
        val text = activity.resources.getString(strResId)
        val textView = findView<TextView>(R.id.action_bar_left_clickable_textview)
        textView.text = text
        textView.setBackgroundResource(R.drawable.register_right_top_btn_selector)
        textView.setPadding(ScreenUtil.dip2px(10f), 0, ScreenUtil.dip2px(10f), 0)
        return textView
    }

    /**
     * *********** 假登录示例：假登录后，可以查看该用户数据，但向云信发送数据会失败；随后手动登录后可以发数据 **************
     */
    private fun fakeLoginTest() {
        // 获取账号、密码；账号用于假登录，密码在手动登录时需要
        val account = loginAccountEdit!!.editableText.toString().toLowerCase()
        val token = tokenFromPassword(loginPasswordEdit!!.editableText.toString())
        // 执行假登录
        val res = NIMClient.getService(
            AuthService::class.java
        ).openLocalCache(
            account
        ) // SDK会将DB打开，支持查询。
        Log.i("test", "fake login " + if (res) "success" else "failed")
        if (!res) {
            return
        }
        // Demo缓存当前假登录的账号
        DemoCache.setAccount(account)
        // 初始化消息提醒配置
        initNotificationConfig()
        // 设置uikit
        NimUIKit.loginSuccess(account)
        // 进入主界面，此时可以查询数据（最近联系人列表、本地消息历史、群资料等都可以查询，但当云信服务器发起请求会返回408超时）
        MainActivity.start(this@LoginActivity, null)
        // 演示15s后手动登录，登录成功后，可以正常收发数据
        handler.postDelayed({
            loginRequest = NIMClient.getService(
                AuthService::class.java
            ).login(
                LoginInfo(account, token)
            )
            loginRequest?.setCallback(object : RequestCallbackWrapper<Any?>() {
                override fun onResult(code: Int, result: Any?, exception: Throwable) {
                    Log.i("test", "real login, code=$code")
                    if (code == ResponseCode.RES_SUCCESS.toInt()) {
                        saveLoginInfo(account, token)
                        finish()
                    }
                }
            })
        }, 15 * 1000.toLong())
    }

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val KICK_OUT = "KICK_OUT"
        private const val KICK_OUT_DESC = "KICK_OUT_DESC"

        @JvmStatic
        @JvmOverloads
        fun start(context: Context, kickOut: Boolean = false, kickOutDesc: String? = "") {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtra(KICK_OUT, kickOut)
            intent.putExtra(KICK_OUT_DESC, kickOutDesc)
            context.startActivity(intent)
        }

        private fun readAppKey(context: Context): String? {
            try {
                val appInfo = context.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA
                )
                return appInfo.metaData.getString("com.netease.nim.appKey")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }
}