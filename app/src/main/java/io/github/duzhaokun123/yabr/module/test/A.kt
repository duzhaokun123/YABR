package io.github.duzhaokun123.yabr.module.test

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.core.BiliToast
import io.github.duzhaokun123.yabr.module.core.TopActivity
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.loaderContext
import kotlin.random.Random

@ModuleEntry(
    id = "test_a",
    targets = [ModuleEntryTarget.MAIN],
)
object A : BaseModule() {
    override fun onLoad(): Boolean {
        loaderContext.application.registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?
                ) {
                    logger.d("here")
                    AlertDialog.Builder(TopActivity.topActivity)
                        .setTitle("dev.o0kam1.Test A")
                        .setMessage("This is a test module A.")
                        .setPositiveButton("OK") { dialog, _ ->
                            BiliToast.showToast("test A received broadcast")
                        }.show()
                }
            }, IntentFilter("test_action"),
            Context.RECEIVER_EXPORTED
        )
        return true
    }

    override fun onUnload(): Boolean {
        TODO("Not yet implemented")
    }
}