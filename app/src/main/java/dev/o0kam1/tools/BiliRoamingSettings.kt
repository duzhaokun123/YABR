package dev.o0kam1.tools

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIActivity
import io.github.duzhaokun123.yabr.module.core.ActivityHijack
import io.github.duzhaokun123.yabr.module.core.ModuleActivity
import io.github.duzhaokun123.yabr.module.core.ModuleActivityMeta
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.loadClass
import kotlin.reflect.jvm.javaMethod

@ModuleEntry(
    id = "dev.o0kam1.BiliRoamingSettings",
    targets = [ModuleEntryTarget.MAIN]
)
object BiliRoamingSettings : BaseModule(), UIActivity {
    const val START_SETTING_KEY = "biliroaming_start_setting"

    override val name = "BiliRoaming 设置"
    override val description = "打开 BiliRoaming 模块的设置界面"
    override val category = UICategory.TOOL

    override val moduleActivity = BiliRoamingSettingsStartProxyActivity::class.java

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onLoad(): Boolean {
        Activity::onNewIntent.javaMethod!!
            .hookBefore {
                val activity = it.thiz as Activity
                if (activity.javaClass.name != "tv.danmaku.bili.MainActivityV2") return@hookBefore
                val intent = it.args[0] as? Intent ?: return@hookBefore
                if (intent.hasExtra(START_SETTING_KEY)) {
                    activity.finish()
                    activity.startActivity(intent)
                }
            }
        return true
    }
}


@ModuleActivity
class BiliRoamingSettingsStartProxyActivity : Activity(), ModuleActivityMeta, ActivityHijack.WindowIsTranslucent {
    override val theme = android.R.style.Theme_Translucent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, loadClass("tv.danmaku.bili.MainActivityV2")).apply {
            putExtra(BiliRoamingSettings.START_SETTING_KEY, 1)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        })
        finish()
    }
}