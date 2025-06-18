package io.github.duzhaokun123.yabr.module.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.hooker.base.thisObject
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.findMethod
import io.github.duzhaokun123.yabr.utils.findMethodBestMatch
import io.github.duzhaokun123.yabr.utils.getDeclaredMethodOrNull
import io.github.duzhaokun123.yabr.utils.getResId
import io.github.duzhaokun123.yabr.utils.invokeMethod
import io.github.duzhaokun123.yabr.utils.invokeMethodAs
import io.github.duzhaokun123.yabr.utils.loadClass
import io.github.duzhaokun123.yabr.utils.new
import io.github.duzhaokun123.yabr.utils.newAs

@ModuleEntry(
    id = "home_drawer_hook",
    targets = [ModuleEntryTarget.MAIN]
)
@SuppressLint("StaticFieldLeak")
object HomeDrawerHook : BaseModule(), UISwitch, SwitchModule, Compatible {
    override val canUnload = false

    override val name = "移动我的到侧边栏"
    override val description =
        "移动后，可以点击首页的头像或者在空白地方左划打开侧边栏\n关闭时，需要重启两次客户端才能复原。如果无法还原，请重启手机。"
    override val category = UICategory.UI

    override fun checkCompatibility(): String? {
        return null // TODO: Implement compatibility check
    }

    lateinit var drawLayout: ViewGroup
    lateinit var navView: View


    override fun onLoad(): Boolean {
        val class_MainActivity = loadClass("tv.danmaku.bili.MainActivityV2")
        class_MainActivity
            .findMethod { it.name == "onCreate" && it.parameterTypes contentEquals arrayOf(Bundle::class.java) }
            .hookAfter { param ->
                val self = param.thisObject as Activity
                val view = self.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                (view.parent as ViewGroup).removeViewInLayout(view)
                drawLayout = loadClass("androidx.drawerlayout.widget.DrawerLayout").newAs(self)
                drawLayout.addView(view, 0, view.layoutParams)

                val homeFragment =
                    loadClass("tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment").new()
                val fragmentManager = self.invokeMethod("getSupportFragmentManager")!!
                fragmentManager
                    .invokeMethod("beginTransaction")!!
                    .invokeMethod("add", homeFragment, "home")!!
                    .invokeMethod("commit")
                fragmentManager.invokeMethod("executePendingTransactions")

                self.setContentView(drawLayout)
            }
        val createHooker: (HookCallbackContext) -> Unit = { param ->
            val self = param.thisObject as Activity
            val fragmentManager = self.invokeMethod("getSupportFragmentManager")!!
            navView = fragmentManager.invokeMethod("findFragmentByTag", "home")!!
                .invokeMethodAs("getView")!!
            val layoutParams =
                loadClass("androidx.drawerlayout.widget.DrawerLayout\$LayoutParams").newAs<ViewGroup.LayoutParams>(
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            layoutParams.javaClass.fields[0].set(layoutParams, Gravity.START)
            (navView.parent as? ViewGroup ?: drawLayout).addView(navView, 1, layoutParams)
        }

        (class_MainActivity.getDeclaredMethodOrNull("onPostCreate", Bundle::class.java)
            ?: class_MainActivity.getDeclaredMethodOrNull("onStart"))
            ?.hookAfter(createHooker)


        class_MainActivity.findMethod { it.name == "onBackPressed" }
            .hookBefore {
                if (drawLayout.invokeMethod("isDrawerOpen", Gravity.START) == true) {
                    it.result = drawLayout.invokeMethod("closeDrawer", Gravity.START)
                }
            }

        loadClass("tv.danmaku.bili.ui.main2.basic.BaseMainFrameFragment")
            .findMethodBestMatch("onViewCreated", View::class.java, Bundle::class.java)
            .hookAfter { param ->
                val id = getResId("avatar_layout")
                (param.args[0] as View).findViewById<View>(id)?.setOnClickListener {
                    runCatching {
                        drawLayout.invokeMethod("openDrawer", Gravity.START, true)
                    }.logError()
                }
            }

        return true
    }
}