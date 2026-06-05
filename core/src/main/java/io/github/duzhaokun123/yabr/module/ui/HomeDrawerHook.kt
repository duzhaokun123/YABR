package io.github.duzhaokun123.yabr.module.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import io.github.duzhaokun123.hooker.base.HookCallbackContext
import io.github.duzhaokun123.hooker.base.Unhooker
import io.github.duzhaokun123.hooker.base.thisObject
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.Compatible
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
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

@ModuleEntry(
    id = "home_drawer_hook",
    targets = [ModuleEntryTarget.MAIN]
)
@SuppressLint("StaticFieldLeak")
object HomeDrawerHook : BaseModule(), UISwitch, SwitchModule, Compatible, DexKitMemberOwner {
    override val canUnload = false

    override val name = "移动我的到侧边栏"
    override val description =
        "移动后，可以点击首页的头像或者在空白地方左划打开侧边栏"
    override val category = UICategory.UI

    override fun checkCompatibility(): String? {
        return null // TODO: Implement compatibility check
    }

    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: View


    override fun onLoad(): Boolean {
        val class_MainActivity = loadClass("tv.danmaku.bili.MainActivityV2")
        class_MainActivity
            .findMethod { it.name == "onCreate" && it.parameterTypes contentEquals arrayOf(Bundle::class.java) }
            .hookAfter { param ->
                val self = param.thisObject as Activity
                val view = self.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
                (view.parent as ViewGroup).removeViewInLayout(view)
                drawerLayout = DrawerLayout(self)
                drawerLayout.addView(view, 0, view.layoutParams)

                val homeFragment =
                    loadClass("tv.danmaku.bili.ui.main2.mine.HomeUserCenterFragment").new()
                val fragmentManager = self.invokeMethod("getSupportFragmentManager")!!
                fragmentManager
                    .invokeMethod("beginTransaction")!!
                    .invokeMethod("add", homeFragment, "home")!!
                    .invokeMethod("commit")
                fragmentManager.invokeMethod("executePendingTransactions")

                self.setContentView(drawerLayout)
            }
        var createUnhooker: Unhooker? = null
        val createHooker: (HookCallbackContext) -> Unit = { param ->
            val self = param.thisObject as Activity
            val fragmentManager = self.invokeMethod("getSupportFragmentManager")!!
            navView = fragmentManager.invokeMethod("findFragmentByTag", "home")!!
                .invokeMethodAs("getView")!!
            val layoutParams = DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.START)
            (navView.parent as? ViewGroup ?: drawerLayout).addView(navView, 1, layoutParams)
            createUnhooker?.invoke()
        }

        createUnhooker = (class_MainActivity.getDeclaredMethodOrNull("onPostCreate", Bundle::class.java)
            ?: class_MainActivity.getDeclaredMethod("onStart"))
            .hookAfter(createHooker)


        class_MainActivity.findMethod { it.name == "onBackPressed" }
            .hookBefore {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    it.result = drawerLayout.closeDrawer(GravityCompat.START)
                }
            }

        loadClass("tv.danmaku.bili.ui.main2.basic.BaseMainFrameFragment")
            .findMethodBestMatch("onViewCreated", View::class.java, Bundle::class.java)
            .hookAfter { param ->
                val id = getResId("avatar_layout")
                (param.args[0] as View).findViewById<View>(id)?.setOnClickListener {
                    runCatching {
                        drawerLayout.openDrawer(GravityCompat.START)
                    }.logError()
                }
            }

        return true
    }
}