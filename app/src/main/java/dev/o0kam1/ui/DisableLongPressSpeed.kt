package dev.o0kam1.ui

import android.view.MotionEvent
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UISwitch
import io.github.duzhaokun123.yabr.module.base.dexKitMember
import io.github.duzhaokun123.yabr.utils.toClass

@ModuleEntry(
    id = "dev.o0kam1.ui.DisableLongPressSpeed",
)
object DisableLongPressSpeed : BaseModule(), UISwitch, SwitchModule, DexKitMemberOwner {
    override val name = "禁用播放器长按加速"
    override val description = "防止误触"
    override val category = UICategory.UI

    val class_PlayerOnLongPressListener by dexKitMember("com.bilibili.playerbizcommon.gesture.GestureService.PlayerOnLongPressListener") { bridge ->
        bridge.findClass {
            searchPackages("com.bilibili.playerbizcommon.gesture")
            matcher {
                interfaces {
                    add("com.bilibili.playerbizcommon.gesture.OnLongPressListener")
                }
                usingStrings("mPlayerContainer")
            }
        }.single().toClass()
    }

    override fun onLoad(): Boolean {
        class_PlayerOnLongPressListener!!
            .getDeclaredMethod("onLongPress", MotionEvent::class.java)
            .hookReplace {
                true
            }
        return true
    }
}