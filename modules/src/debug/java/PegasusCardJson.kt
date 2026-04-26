package debug

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.webkit.JavascriptInterface
import android.webkit.WebView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.module.core.JsonHelper
import io.github.duzhaokun123.yabr.module.core.ThreePointCallback
import io.github.duzhaokun123.yabr.module.core.ThreePointHook
import io.github.duzhaokun123.yabr.module.core.ThreePointItemItemData
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget

@ModuleEntry(
    id = "pegasus_card_json",
    targets = [ModuleEntryTarget.MAIN]
)
object PegasusCardJson : BaseModule() {
    override fun onLoad(): Boolean {
        ThreePointHook.addThreePointCallback(91357L, object : ThreePointCallback {
            override fun parseData(data: Any): ThreePointItemItemData? {
                if (data.javaClass.name != "com.bilibili.pegasus.data.card.SmallCoverV2Data") return null
                return ThreePointItemItemData(
                    "json",
                    JsonHelper.gsonToString(data)
                )
            }

            @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
            override fun onClick(data: ThreePointItemItemData) {
                val context = ActivityUtils.topActivity ?: return
                val webView = WebView(context)
                webView.settings.apply {
                    javaScriptEnabled = true
                    blockNetworkLoads = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                }
                webView.addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun getData() = data.data
                }, "pegasus_card_json")
                webView.loadData("""
                    <pre id="data"></pre>
                    <script>
                        let data = pegasus_card_json.getData()
                        document.getElementById("data").innerText = JSON.stringify(JSON.parse(data), null, 2)
                    </script>
                """.trimIndent(), "text/html", "utf-8")
                AlertDialog.Builder(context)
                    .setTitle("json")
                    .setView(webView)
                    .show()
            }
        })
        return true
    }
}