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
import io.github.duzhaokun123.yabr.utils.findMethod
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.util.WeakHashMap

@ModuleEntry(
    id = "card_json",
    targets = [ModuleEntryTarget.MAIN]
)
object CardJson : BaseModule() {
    private val cache = WeakHashMap<String, Any>()
    private val jackson by lazy {
        jsonMapper {
            addModules(kotlinModule {
                enable(KotlinFeature.KotlinPropertyNameAsImplicitName)
                enable(KotlinFeature.UseJavaDurationConversion)
            })
        }
    }

    override fun onLoad(): Boolean {
        ThreePointHook.addThreePointCallback(91357L, object : ThreePointCallback {
            override fun parseData(data: Any): ThreePointItemItemData {
                val key = data.hashCode().toString()
                cache[key] = data
                return ThreePointItemItemData("json", key)
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
                    fun getData(): String? {
                        val data = cache[data.data] ?: return """{"error":"not found"}"""
                        return when (data.javaClass.name) {
                            "com.bilibili.pegasus.data.card.SmallCoverV2Data" -> {
                                JsonHelper.gsonToString(data)
                            }
                            "com.bilibili.ship.theseus.united.page.intro.module.relate.RelateCard" -> {
                                val start = System.currentTimeMillis()
                                val json = jackson.writeValueAsString(data)
                                val end = System.currentTimeMillis()
                                logger.d("json serialization time: ${end - start}ms")
                                json
                            }
                            else -> """{"error": "unknown type: ${data.javaClass.name}"}"""
                        }
                    }
                }, "card_json")
                webView.loadData(
                    """
                    <pre id="data">Loading...</pre>
                    <script>
                        let data = card_json.getData()
                        document.getElementById("data").innerText = JSON.stringify(JSON.parse(data), null, 2)
                    </script>
                """.trimIndent(), "text/html", "utf-8"
                )
                AlertDialog.Builder(context)
                    .setTitle("json")
                    .setView(webView)
                    .show()
            }
        })

        // FIXME： 脏方法解决序列化问题 也许应该向上游提交修改
        Class.forName("tools.jackson.module.kotlin.KotlinModuleKt")
            .findMethod { it.name == "isKotlinClass" }
            .hookBefore {
                val clazz = it.args[0] as Class<*>
                it.result = clazz.declaredAnnotations.any { (it as java.lang.annotation.Annotation).annotationType().name == kotlin.Metadata::class.java.name }
            }
        Class.forName("tools.jackson.module.kotlin.KotlinNamesAnnotationIntrospector")
            .findMethod { it.name == "findImplicitPropertyName" }
            .hookAfter {
                if (it.throwable != null) {
                    logger.d("avoid tools.jackson.module.kotlin.KotlinNamesAnnotationIntrospector.findImplicitPropertyName crash!")
                    it.throwable = null
                    it.result = null
                }
            }
        return true
    }
}