// Source repository: https://github.com/duzhaokun123/FuckCainiao (upstream 9da78f2).
package io.github.duzhaokun123.fuckcainiao

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.TextSwitcher
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Method
import java.util.zip.ZipFile

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val TAG = "FuckCainiao"
        const val URL_ABOUT_FUCK_CAINIAO = CleanupRules.ABOUT_URL

        /** 寄件相关 H5 路径（page.cainiao.com 下），用于注入清理脚本。 */
        private val SEND_PATHS = setOf("/mcn/union-post", "/cn-app-web/send")
    }

    private lateinit var hostLoader: ClassLoader
    private lateinit var modulePath: String
    private val personalScript by lazy { readAsset("assets/personal-cleanup.js") }
    private val sendCleanupScript by lazy { readAsset("assets/send-cleanup.js") }

    private fun readAsset(name: String): String =
        ZipFile(modulePath).use { zip ->
            zip.getInputStream(zip.getEntry(name)).bufferedReader().use { it.readText() }
        }
    private val reported = mutableSetOf<String>()

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.cainiao.wireless" ||
            lpparam.processName != lpparam.packageName) return
        hostLoader = lpparam.classLoader
        EzXHelperInit.initHandleLoadPackage(lpparam)
        EzXHelperInit.setLogTag(TAG)
        log("initializing ${BuildConfig.VERSION_NAME}")
        installNavigation()
        installHome()
        installSearchCleanup()
        installLogistics()
        installPersonal()
        installPersonalWebPage()
        installPopupShield()
        for (method in listOf("startActivity", "startActivityForResult")) {
            hook(Activity::class.java, method, before = { param ->
                val intent = param.args.firstOrNull() as? Intent ?: return@hook
                val extras = intent.extras
                val isAbout = CleanupRules.isAboutUrl(intent.dataString) ||
                    extras?.keySet()?.any { CleanupRules.isAboutUrl(extras.get(it) as? String) } == true
                if (!isAbout) return@hook
                param.result = null
                AlertDialog.Builder(param.thisObject as Activity)
                    .setTitle("关于 FuckCainiao")
                    .setMessage("作者：老余\n版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            }, matches = { parameterTypes.firstOrNull() == Intent::class.java })
        }
    }

    private fun installPersonalWebPage() {
        hook("android.taobao.windvane.extra.uc.WVUCWebViewClient", "onPageFinished",
            after = { param ->
                val raw = param.args.getOrNull(1) as? String ?: return@hook
                val url = Uri.parse(raw)
                val webView = param.args[0] ?: return@hook
                if (url.scheme != "https" || url.host != "page.cainiao.com") return@hook
                val path = url.path ?: return@hook
                val evaluate = webView.javaClass.methods.first {
                    it.name == "evaluateJavascript" && it.parameterTypes.size == 2
                }
                when {
                    path == "/cn-app-web/profile/index.html" -> evaluate.invoke(webView, personalScript, null)
                    SEND_PATHS.any { path.startsWith(it) } -> evaluate.invoke(webView, sendCleanupScript, null)
                }
            }, matches = { parameterTypes.size == 2 })
    }

    private fun installNavigation() {
        hook("com.cainiao.commonlibrary.navigation.NavigationView", "renderNavigationTabs",
            after = { param ->
                val tabs = XposedHelpers.getObjectField(param.thisObject, "tabItemLayout") as? ViewGroup
                    ?: return@hook
                for (index in 0 until tabs.childCount) {
                    val child = tabs.getChildAt(index)
                    val key = XposedHelpers.callMethod(child, "getKey") as? String ?: continue
                    child.visibility = if (CleanupRules.keepNavigation(key)) View.VISIBLE else View.GONE
                }
            })
        hook("com.cainiao.wireless.homepage.view.activity.HomePageActivity", "onCreate",
            after = { param ->
                val activity = param.thisObject as Activity
                activity.window.navigationBarColor = Color.rgb(0x1a, 0x1a, 0x1a)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    activity.window.navigationBarDividerColor = Color.rgb(0x1a, 0x1a, 0x1a)
                }
            }, matches = { parameterTypes.size == 1 })
    }

    private fun installHome() {
        hook("com.cainiao.wireless.homepage.view.activity.WelcomeActivity", "requestMamaAndRtbSplash",
            before = { param ->
                // Hot launches have no caller that advances to the existing home activity.
                if ((param.thisObject as Activity).intent.getBooleanExtra("isHotLaunch", false)) {
                    (param.thisObject as Activity).finish()
                }
                param.result = false
            }, matches = { returnType == Boolean::class.javaPrimitiveType })
        hook("com.cainiao.wireless.homepage.v9.HomeHeaderSection", "processHeaderData",
            before = { param ->
                val rows = param.args.firstOrNull() as? MutableList<*> ?: return@hook
                CleanupRules.cleanHeader(rows)
            }, matches = { parameterTypes.firstOrNull()?.name == "com.alibaba.fastjson.JSONArray" })
        hook("com.cainiao.wireless.cubex.mvvm.view.CubeXLinearLayoutFragment", "setEmpty",
            before = { param ->
                val rows = param.args.getOrNull(1) as? MutableList<*> ?: return@hook
                while (rows.size > 1) rows.removeAt(rows.lastIndex)
            }, matches = { parameterTypes.size == 2 })
        hook("com.cainiao.wireless.recommend.CNRecommendView", "initView",
            after = { (it.thisObject as View).visibility = View.GONE })
        hook("com.cainiao.wireless.homepage.view.widget.bottom.NewBottomFloatBanner", "init",
            before = { it.result = null })
        hook("com.cainiao.wireless.homepage.v9.GuideAdsSection", "getBizDataCount",
            before = { it.result = 0 })
        hook("com.cainiao.wireless.homepage.view.widget.PackageTimeLineDecorateView", "setData",
            before = { param ->
                val data = param.args.firstOrNull() ?: return@hook
                XposedHelpers.setObjectField(data, "tagIconList", arrayListOf<Any>())
            }, matches = { parameterTypes.size == 1 })
        hook("com.cainiao.wireless.homepage.v9.PackageListSection", "bindPackageListData",
            before = { param ->
                val rows = param.args.firstOrNull() as? List<*> ?: return@hook
                for (row in rows) {
                    if (row == null) continue
                    val kind = XposedHelpers.getObjectField(row, "bizzType") as? String ?: continue
                    if (kind != "PKG_GROUP_TITLE") continue
                    val raw = XposedHelpers.getObjectField(row, "packageData") as? String ?: continue
                    val json = XposedHelpers.callStaticMethod(
                        XposedHelpers.findClass("com.alibaba.fastjson.JSON", hostLoader), "parseObject", raw
                    ) as? Map<*, *> ?: continue
                    if (CleanupRules.cleanPackageGroup(json)) {
                        XposedHelpers.setObjectField(row, "packageData", json.toString())
                    }
                }
                param.args[0] = rows.filterNot { row ->
                    row != null && XposedHelpers.getObjectField(row, "bizzType") == "PKG_ACTION_CARD"
                }
            }, matches = { parameterTypes.size == 1 })
    }

    private fun installLogistics() {
        hook("com.cainiao.wireless.shop.task.guide.TaskGuideManager", before = { param ->
            val current = XposedHelpers.getObjectField(param.thisObject, "fGV") as? java.lang.ref.WeakReference<*>
            val activity = current?.get() as? Activity ?: return@hook
            if (activity.javaClass.simpleName.endsWith("LogisticDetailActivity")) param.result = null
        }, matches = {
            !java.lang.reflect.Modifier.isStatic(modifiers) && parameterTypes.size == 1 &&
                parameterTypes[0].name == "com.cainiao.wireless.shop.task.guide.e" && name == "a" &&
                returnType == Void.TYPE
        })
        for (name in listOf("LogisticDetailBannerView", "LogisticDetailTANX_BannerView")) {
            hook("com.taobao.cainiao.logistic.ui.view.component.$name", before = { it.args[0] = null },
                matches = {
                    parameterTypes.size == 1 &&
                        parameterTypes[0].name == "com.taobao.cainiao.logistic.response.model.LdAdsCommonEntity" &&
                        returnType == Void.TYPE
                })
        }
        hook("com.taobao.cainiao.logistic.ui.newview.LogisticDetailTemplateFragment", "updateAdsInfo",
            after = { param ->
                for (name in listOf("mLogisticRedPacketViewStub", "mLogisticRedPacketView")) {
                    (XposedHelpers.getObjectField(param.thisObject, name) as? View)?.visibility = View.GONE
                }
            })
    }

    private fun installSearchCleanup() {
        val actionBar = "com.cainiao.wireless.homepage.view.widget.v891127.HomeActionBarV891127"
        hook(actionBar, "init", after = { clearHomeSearchHint(it.thisObject as View) },
            matches = { parameterTypes.isEmpty() })
        // This method only binds and schedules the rotating search suggestions.
        hook(actionBar, "b", before = {
            clearHomeSearchHint(it.thisObject as View)
            it.result = null
        }, matches = {
            parameterTypes.size == 1 &&
                parameterTypes[0].name == "com.cainiao.wireless.homepage.entity.HomeSearchBarModelV9" &&
                returnType == Void.TYPE
        })

        val fragment = "com.cainiao.wireless.mvp.activities.fragments.MainPackageSearchFragment"
        for ((method, field) in listOf(
            "initPackageAdVideoView" to "mPackageAdVideoView",
            "initSearchHotGoodsView" to "mHomeRecommendHotGoodsView"
        )) {
            // Skip only these promotion loaders; search and import initialization still run.
            hook(fragment, method, before = { param ->
                (XposedHelpers.getObjectField(param.thisObject, field) as? View)?.visibility = View.GONE
                param.result = null
            }, matches = { parameterTypes.isEmpty() && returnType == Void.TYPE })
        }
    }

    private fun clearHomeSearchHint(actionBar: View) {
        (XposedHelpers.getObjectField(actionBar, "eiM") as? Handler)?.removeCallbacksAndMessages(null)
        val hint = XposedHelpers.getObjectField(actionBar, "eiz") as? TextSwitcher ?: return
        hint.inAnimation = null
        hint.outAnimation = null
        hint.clearAnimation()
        for (index in 0 until hint.childCount) {
            (hint.getChildAt(index) as? TextView)?.apply {
                text = ""
                setCompoundDrawables(null, null, null, null)
            }
        }
        hint.visibility = View.INVISIBLE
    }

    private fun installPopupShield() {
        // 统一弹窗队列：命中广告类型/名称时不入队，后续 GZ() 自然无事发生。
        hook("com.cainiao.commonlibrary.popupmanager.a", "c", before = { param ->
            val entity = param.args.getOrNull(0) ?: return@hook
            val type = XposedHelpers.callMethod(entity, "GW")?.toString()
            val name = XposedHelpers.callMethod(entity, "getName") as? String
            if (CleanupRules.isBlockedPopup(type, name)) {
                param.result = param.thisObject
                reportOnce("blocked popup: $name [$type]")
            }
        }, matches = {
            parameterTypes.size == 1 && !java.lang.reflect.Modifier.isStatic(modifiers) &&
                parameterTypes[0].name == "com.cainiao.commonlibrary.popupmanager.PopViewEntity"
        })

        // DialogFragment：命中广告黑名单时不再显示（support 与 androidx 两个实现都试）。
        for (fragmentClass in listOf(
            "android.support.v4.app.DialogFragment",
            "androidx.fragment.app.DialogFragment"
        )) {
            for (method in listOf("show", "showNow")) {
                hook(fragmentClass, method, before = { blockDialogFragment(it) })
            }
        }

        // 原生 Dialog：命中黑名单时不再显示。
        hook("android.app.Dialog", "show", before = { param ->
            val className = param.thisObject.javaClass.name
            if (CleanupRules.isBlockedDialog(className)) {
                param.result = null
                reportOnce("blocked dialog: $className")
            }
        }, matches = { name == "show" && parameterTypes.isEmpty() })

        // 首页营销插屏：a(Activity, HomePageStartUpBean, IAdStartDialogListener) 直接返回 false。
        hook("com.cainiao.wireless.homepage.view.widget.dialog.HomeAdStartDialogMgr", "a",
            before = { it.result = false },
            matches = {
                parameterTypes.size == 3 && returnType == Boolean::class.javaPrimitiveType &&
                    parameterTypes[0] == Activity::class.java
            })

        // 包裹导入营销：Toast 与插屏两种形态都从这个私有方法收口。
        // 该类是 Kotlin 类，dex 真实名为 ad.c（jadx 按 @Metadata 还原成 PackageImportAdManager）。
        hook("com.cainiao.wireless.packagelist.ad.c", "a",
            before = { it.result = false },
            matches = {
                parameterTypes.size == 2 && returnType == Boolean::class.javaPrimitiveType &&
                    parameterTypes[0] == java.lang.ref.WeakReference::class.java
            })

        // 物流详情会员奖励弹窗：showDialog(Context, Model) 不再弹出。
        hook("com.taobao.cainiao.logistic.ui.jsnewview.view.LogisticDetailMemberRewardDialog",
            "showDialog", before = { it.result = null })
    }

    private fun blockDialogFragment(param: XC_MethodHook.MethodHookParam) {
        val className = param.thisObject.javaClass.name
        if (!CleanupRules.isBlockedDialog(className)) return
        // show(FragmentTransaction, String) 返回 int（tag id），其余重载返回 void。
        val returnsInt = (param.method as? Method)?.returnType == Int::class.javaPrimitiveType
        param.result = if (returnsInt) 0 else null
        reportOnce("blocked dialog fragment: $className")
    }

    private fun installPersonal() {
        // Both setData overloads converge here, including DIFF and RELOAD updates.
        hook("com.cainiao.wireless.cubex.mvvm.adapter.DXRecyclerViewAdapter", "commonSetData",
            before = { param ->
                @Suppress("UNCHECKED_CAST")
                val rows = param.args.firstOrNull() as? MutableList<Any?> ?: return@hook
                rows.removeAll(CleanupRules::isPersonalPromotion)
                addAboutRow(rows)
            }, matches = { parameterTypes.size == 1 })
    }

    @Suppress("UNCHECKED_CAST")
    private fun addAboutRow(rows: MutableList<Any?>) {
        if (rows.any(CleanupRules::isAbout)) return
        val quit = rows.indexOfFirst {
            CleanupRules.templateName(it) == "guoguo_new_my_settings_quit"
        }
        if (quit < 0) return
        val source = rows.take(quit).firstOrNull {
            CleanupRules.templateName(it) == "guoguo_new_my_settings_item"
        } ?: return
        val content = (source as? Map<*, *>)?.get("materialContentMapper") ?: return
        val about = XposedHelpers.callMethod(source, "clone") as MutableMap<String, Any?>
        val aboutContent = XposedHelpers.callMethod(content, "clone") as MutableMap<String, Any?>
        aboutContent["hasGroupHeader"] = ""
        aboutContent["groupTitle"] = ""
        aboutContent["title"] = "关于 FuckCainiao"
        aboutContent["title_right"] = "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        aboutContent["type"] = "normal"
        aboutContent["jumpUrl"] = URL_ABOUT_FUCK_CAINIAO
        about["materialContentMapper"] = aboutContent
        about["id"] = "fuckcainiao_about"
        about["materialId"] = "fuckcainiao_about"
        about["pitId"] = "fuckcainiao_about"
        rows.add(quit, about)
    }

    private fun hook(
        className: String,
        methodName: String? = null,
        before: ((XC_MethodHook.MethodHookParam) -> Unit)? = null,
        after: ((XC_MethodHook.MethodHookParam) -> Unit)? = null,
        matches: Method.() -> Boolean = { true }
    ) {
        try {
            val type = XposedHelpers.findClassIfExists(className, hostLoader)
            if (type == null) {
                log("unavailable: $className")
                return
            }
            hook(type, methodName, before, after, matches)
        } catch (error: Throwable) {
            log("installation failed: $className: $error")
        }
    }

    private fun hook(
        type: Class<*>,
        methodName: String? = null,
        before: ((XC_MethodHook.MethodHookParam) -> Unit)? = null,
        after: ((XC_MethodHook.MethodHookParam) -> Unit)? = null,
        matches: Method.() -> Boolean = { true }
    ) {
        val label = "${type.name}.${methodName ?: "<signature>"}"
        try {
            val methods = type.declaredMethods.filter {
                (methodName == null || it.name == methodName) && it.matches()
            }
            if (methods.isEmpty()) log("unavailable: $label")
            methods.forEach { method ->
                XposedBridge.hookMethod(method, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        guarded(label) { before?.invoke(param) }
                    }
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!param.hasThrowable()) guarded(label) { after?.invoke(param) }
                    }
                })
            }
            log("installed: $label (${methods.size})")
        } catch (error: Throwable) {
            log("installation failed: $label: $error")
        }
    }

    private fun guarded(label: String, action: () -> Unit) {
        try {
            action()
        } catch (error: Throwable) {
            reportOnce("callback failed: $label: $error")
        }
    }

    private fun reportOnce(message: String) {
        synchronized(reported) { if (!reported.add(message)) return }
        log(message)
    }

    private fun log(message: String) {
        android.util.Log.i(TAG, message)
        XposedBridge.log("$TAG: $message")
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
        EzXHelperInit.initZygote(startupParam)
    }
}
