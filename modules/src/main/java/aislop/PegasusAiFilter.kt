package aislop

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.core.ConfigStore
import io.github.duzhaokun123.yabr.module.core.JsonHelper
import io.github.duzhaokun123.yabr.module.core.PegasusHook
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.getJsonFieldValueAs
import io.github.duzhaokun123.yabr.utils.setJsonFieldValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@ModuleEntry(
    id = "aislop.PegasusAiFilter",
    targets = [ModuleEntryTarget.MAIN]
)
object PegasusAiFilter : BaseModule(), SwitchModule, UIComplex {
    override val name = "首页卡片 AI 过滤"
    override val description = "通过 AI 根据自定义提示词移除首页卡片"
    override val category = UICategory.AI_SLOP

    private const val KEY_BASE_URL = "base_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MODEL = "model"
    private const val KEY_PROMPT = "prompt"
    private const val KEY_SHOW_COUNT = "show_count"

    private const val DEFAULT_BASE_URL = "https://api.openai.com/v1/chat/completions"
    private const val DEFAULT_MODEL = "gpt-4o-mini"
    private const val DEFAULT_PROMPT = "你是首页卡片过滤器 根据卡片信息判断其是否为低质量 标题党 或无意义内容 是则回答 yes 否则回答 no 只回答 yes 或 no"

    private val config get() = ConfigStore.ofModule(this)

    // 这些字段对内容判断无意义 直接整体丢弃
    private val DROP_KEYS = setOf(
        "uri", "cover", "goto_icon", "three_point_v2", "player_args",
        "a0", "track_id", "player_preload", "icon", "icon_night",
    )

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // 缓存 ai 判断结果 避免重复请求 key 为卡片标识文本
    private val decisionCache = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    private val executor = Executors.newCachedThreadPool()

    override fun onLoad(): Boolean {
        PegasusHook.addInterceptFirst(id) { data ->
            val items = data.getJsonFieldValueAs<MutableList<Any>>("items")
            var count = 0

            // 准备所有卡片文本并发送并发请求
            data class ItemInfo(val index: Int, val cardText: String)
            val itemsToCheck = mutableListOf<ItemInfo>()
            val cardTexts = mutableListOf<String>()

            items.forEachIndexed { index, item ->
                val rawJson = JsonHelper.gsonToString(item) ?: return@forEachIndexed
                if (rawJson.isBlank()) return@forEachIndexed
                val cardText = runCatching {
                    json.parseToJsonElement(rawJson).clean().toString()
                }.getOrDefault(rawJson)
                if (!decisionCache.containsKey(cardText)) {
                    itemsToCheck.add(ItemInfo(index, cardText))
                    cardTexts.add(cardText)
                }
            }

            // 并发发送所有AI请求
            val futures = cardTexts.map { cardText ->
                CompletableFuture.supplyAsync({ askAi(cardText) }, executor)
            }

            // 等待所有请求完成并更新缓存
            futures.forEachIndexed { i, future ->
                runCatching {
                    decisionCache[cardTexts[i]] = future.get()
                }.onFailure {
                    logger.w("ai request error", it)
                    decisionCache[cardTexts[i]] = false
                }
            }

            // 根据缓存过滤items
            val iterator = items.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                val rawJson = JsonHelper.gsonToString(item) ?: continue
                if (rawJson.isBlank()) continue
                val cardText = runCatching {
                    json.parseToJsonElement(rawJson).clean().toString()
                }.getOrDefault(rawJson)
                if (decisionCache[cardText] == true) {
                    iterator.remove()
                    count++
                }
            }

            if (count > 0 && config.getBoolean(KEY_SHOW_COUNT, false) == true) {
                val configData = data.getJsonFieldValueAs<Any>("config")
                val toastConfig = configData.getJsonFieldValueAs<Any>("toast")
                var toastMessage = toastConfig.getJsonFieldValueAs<String?>("toast_message") ?: ""
                toastMessage = toastMessage.trim().removeSuffix("\n")
                if (toastMessage.isNotEmpty()) toastMessage += "\n"
                toastMessage += "PegasusAiFilter removed $count"
                toastConfig.setJsonFieldValue("toast_message", toastMessage)
                toastConfig.setJsonFieldValue("has_toast", true)
            }
        }
        return true
    }

    override fun onUnload(): Boolean {
        PegasusHook.removeIntercept(id)
        executor.shutdown()
        return super.onUnload()
    }

    /** 递归剔除链接 / 过长 / 无意义的字段 减少 token 噪音 */
    private fun JsonElement.clean(): JsonElement = when (this) {
        is JsonObject -> buildJsonObject {
            for ((k, v) in this@clean) {
                if (k in DROP_KEYS) continue
                val cleaned = v.clean()
                if (cleaned is JsonNull) continue
                put(k, cleaned)
            }
        }
        is JsonArray -> buildJsonArray {
            for (v in this@clean) {
                val cleaned = v.clean()
                if (cleaned !is JsonNull) add(cleaned)
            }
        }
        is JsonPrimitive -> {
            if (isString && content.isNoise()) JsonNull else this
        }
    }

    private fun String.isNoise(): Boolean {
        if (length > 80) return true
        val lower = lowercase()
        return lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.startsWith("bilibili://") ||
            lower.contains("hdslb.com") ||
            lower.contains("bilivideo.com")
    }

    private fun askAi(cardText: String): Boolean {
        val baseUrl = config.getString(KEY_BASE_URL, DEFAULT_BASE_URL)!!
        val apiKey = config.getString(KEY_API_KEY, "")!!
        val model = config.getString(KEY_MODEL, DEFAULT_MODEL)!!
        val prompt = config.getString(KEY_PROMPT, DEFAULT_PROMPT)!!
        if (apiKey.isBlank()) return false

        val body = json.encodeToString(
            ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage(role = "system", content = prompt),
                    ChatMessage(role = "user", content = cardText)
                )
            )
        )
        val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
        return runCatching {
            connection.outputStream.use { it.write(body.toByteArray()) }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val err = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText)
                logger.w("ai request failed ${connection.responseCode}: $err")
                return@runCatching false
            }
            val respText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            val resp = json.decodeFromString<ChatResponse>(respText)
            val content = resp.choices.firstOrNull()?.message?.content?.trim()?.lowercase()
                ?: return@runCatching false
            content.startsWith("yes")
        }.onFailure {
            logger.w("ai request error", it)
        }.also {
            connection.disconnect()
        }.getOrDefault(false)
    }

    @Serializable
    data class ChatRequest(
        val model: String,
        val messages: List<ChatMessage>,
    )

    @Serializable
    data class ChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    data class ChatResponse(
        val choices: List<ChatChoice> = emptyList(),
    )

    @Serializable
    data class ChatChoice(
        val message: ChatMessage? = null,
    )

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    override fun onCreateUI(context: Context): View {
        val ll = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        ll.addView(Switch(context).apply {
            text = "启用首页卡片 AI 过滤"
            isChecked = this@PegasusAiFilter.isEnabled
            setOnCheckedChangeListener { _, isChecked ->
                this@PegasusAiFilter.isEnabled = isChecked
            }
        })

        fun addField(label: String, key: String, default: String, password: Boolean = false) {
            ll.addView(TextView(context).apply { text = label })
            ll.addView(EditText(context).apply {
                setText(config.getString(key, default))
                if (password) {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        config.putString(key, text.toString())
                        decisionCache.clear()
                    }
                }
            })
        }

        addField("API 地址 (OpenAI 兼容)", KEY_BASE_URL, DEFAULT_BASE_URL)
        addField("API Key", KEY_API_KEY, "", password = true)
        addField("模型", KEY_MODEL, DEFAULT_MODEL)
        addField("提示词", KEY_PROMPT, DEFAULT_PROMPT)

        ll.addView(Switch(context).apply {
            text = "显示过滤计数"
            isChecked = config.getBoolean(KEY_SHOW_COUNT, false) == true
            setOnCheckedChangeListener { _, isChecked ->
                config.putBoolean(KEY_SHOW_COUNT, isChecked)
            }
        })

        ll.addView(TextView(context).apply {
            text = "AI 回答 yes 时移除卡片 请求已改为并发模式"
        })

        return ll
    }
}
