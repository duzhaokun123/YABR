package aislop

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.DexKitMemberOwner
import io.github.duzhaokun123.yabr.module.base.SwitchModule
import io.github.duzhaokun123.yabr.module.base.UIComplex
import io.github.duzhaokun123.yabr.module.base.isEnabled
import io.github.duzhaokun123.yabr.module.core.ActivityUtils
import io.github.duzhaokun123.yabr.module.core.ConfigStore
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget
import io.github.duzhaokun123.yabr.utils.loaderContext
import org.json.JSONArray
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.FindClass
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.ClassMatcher
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToLong

@ModuleEntry(
    id = "aislop.SponsorBlock",
    targets = [ModuleEntryTarget.MAIN],
)
object SponsorBlock : BaseModule(), SwitchModule, UIComplex, DexKitMemberOwner {
    override val name = "空降助手"
    override val description = "根据 SponsorBlock 社区标记跳过视频中的广告、片头和推广片段"
    override val category = UICategory.AI_SLOP
    override val needDexKitBridge = true

    private val config get() = ConfigStore.ofModule(this)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val states = ConcurrentHashMap<String, VideoState>()
    private val seekMethods = ConcurrentHashMap<Class<*>, List<Method>>()
    private val seenPositionClasses = ConcurrentHashMap.newKeySet<Class<*>>()
    private val missingStateSources = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var activeKey = ""
    private var playViewMethods = emptyList<Method>()
    private var positionMethods = emptyList<Method>()
    private var drawMethods = emptyList<Method>()
    private var progressViewClasses = emptyList<Class<*>>()

    override fun onDexKitReady(bridge: DexKitBridge) {
        playViewMethods = (PLAYER_MOSS_CLASSES.asSequence()
            .mapNotNull { loaderContext.hostClassloader.loadClassOrNull(it) } +
            findClasses(bridge, PLAYER_MOSS_NAMES))
            .distinctBy { it.getName() }
            .flatMap { it.allMethods() }
            .filter { method ->
                !Modifier.isStatic(method.modifiers) && !Modifier.isAbstract(method.modifiers) &&
                    method.name in PLAY_VIEW_METHOD_NAMES &&
                    method.parameterTypes.firstOrNull()?.hasMethod("getBvid") == true
            }
            .distinctBy(Method::toGenericString)
            .toList()

        val controllerClasses = findClasses(bridge, CONTROLLER_NAMES)
        positionMethods = controllerClasses.asSequence()
            .flatMap { it.allMethods() }
            .filter { method ->
                !Modifier.isStatic(method.modifiers) && !Modifier.isAbstract(method.modifiers) &&
                    !Modifier.isNative(method.modifiers) && method.parameterCount == 0 &&
                    method.name in POSITION_METHOD_NAMES && method.returnType.isNumberType()
            }
            .distinctBy(Method::toGenericString)
            .toList()

        progressViewClasses = PROGRESS_VIEW_NAMES.mapNotNull { name ->
            loaderContext.hostClassloader.loadClassOrNull(name).also {
                if (it == null) logger.w("SponsorBlock progress class missing: $name")
            }
        }
        drawMethods = progressViewClasses.asSequence()
            .mapNotNull(::findHostDrawMethod)
            .distinctBy(Method::toGenericString)
            .toList()

        logger.i(
            "SponsorBlock discovery: playView=${playViewMethods.size}, " +
                "position=${positionMethods.size}, progress=${drawMethods.size}",
        )
        logger.d("SponsorBlock playView methods:\n${playViewMethods.joinToString("\n", transform = Method::toGenericString)}")
        logger.d("SponsorBlock position methods:\n${positionMethods.joinToString("\n", transform = Method::toGenericString)}")
        logger.d("SponsorBlock progress methods:\n${drawMethods.joinToString("\n", transform = Method::toGenericString)}")
    }

    override fun onLoad(): Boolean {
        if (playViewMethods.isEmpty() || positionMethods.isEmpty() || drawMethods.isEmpty()) {
            logger.w("SponsorBlock hooks incomplete: playView=${playViewMethods.size}, position=${positionMethods.size}, progress=${drawMethods.size}")
            return false
        }
        playViewMethods.forEach { method ->
            method.hookBefore { hook ->
                val request = hook.args.firstOrNull() ?: return@hookBefore
                logger.d("SponsorBlock playView called: ${method.toGenericString()}, request=${request.javaClass.name}")
                updateIdentity(
                    bvid = request.call("getBvid") as? String,
                    vod = request.call("getVod"),
                )
                hook.args.indices
                    .firstOrNull { index -> hook.args[index]?.let(::isResponseHandler) == true }
                    ?.let { index ->
                        hook.args[index]?.let(::wrapResponseHandler)?.let { hook.args[index] = it }
                    }
            }
        }
        positionMethods.forEach { method ->
            method.hookAfter { hook ->
                val controller = hook.thiz ?: return@hookAfter
                val position = (hook.result as? Number)?.toLong() ?: return@hookAfter
                if (seenPositionClasses.add(controller.javaClass)) {
                    logger.i("SponsorBlock first position callback: ${controller.javaClass.name}.${method.name}=$position")
                }
                checkPosition(controller, position)
            }
        }
        drawMethods.forEach { method ->
            method.hookAfter { hook ->
                val bar = hook.thiz as? ProgressBar ?: return@hookAfter
                if (progressViewClasses.none { it.isInstance(bar) }) return@hookAfter
                drawSegments(bar, hook.args.firstOrNull() as? Canvas ?: return@hookAfter)
            }
        }
        logger.i("SponsorBlock hooked playView=${playViewMethods.size}, position=${positionMethods.size}, progress=${drawMethods.size}")
        return true
    }

    override fun onUnload(): Boolean {
        states.clear()
        seekMethods.clear()
        seenPositionClasses.clear()
        missingStateSources.clear()
        activeKey = ""
        return super.onUnload()
    }

    private fun findClasses(bridge: DexKitBridge, names: Set<String>): Sequence<Class<*>> = names.asSequence()
        .flatMap { name ->
            bridge.findClass(
                FindClass.create().searchPackages("com.bilibili", "com.bapis", "tv.danmaku", "p371tv")
                    .matcher(ClassMatcher.create().className(name, StringMatchType.Contains)),
            ).asSequence().map { it.name }
        }
        .distinct()
            .mapNotNull { loaderContext.hostClassloader.loadClassOrNull(it) }

    private fun wrapResponseHandler(handler: Any): Any? {
        val listener = handler.javaClass.interfaces.firstOrNull { type ->
            type.methods.any { it.name == "onNext" && it.parameterCount == 1 }
        } ?: return null
        return Proxy.newProxyInstance(
            handler.javaClass.classLoader ?: loaderContext.hostClassloader,
            handler.javaClass.interfaces.ifEmpty { arrayOf(listener) },
        ) { _, method, args ->
            if (method.name == "onNext") updateIdentityFromResponse(args?.firstOrNull())
            method.invoke(handler, *(args ?: emptyArray()))
        }
    }

    private fun isResponseHandler(value: Any?): Boolean =
        value?.javaClass?.interfaces?.any { type ->
            type.methods.any { it.name == "onNext" && it.parameterCount == 1 }
        } == true

    private fun updateIdentity(bvid: String?, vod: Any?) {
        val aid = (vod?.call("getAid") as? Number)?.toLong()
        val cid = (vod?.call("getCid") as? Number)?.toLong()?.takeIf { it > 0 }
        val resolvedBvid = bvid?.trim()?.takeIf(String::isNotBlank) ?: aid?.takeIf { it > 0 }?.let(::bvidFromAid)
        if (resolvedBvid == null || cid == null) {
            logger.w("SponsorBlock request identity incomplete: bvid=$bvid, aid=$aid, cid=$cid, vod=${vod?.javaClass?.name}")
            return
        }
        activate(resolvedBvid, cid.toString(), "request")
    }

    private fun updateIdentityFromResponse(response: Any?) {
        val arc = response.call("getPlayArc") ?: return
        val aid = (arc.call("getAid") as? Number)?.toLong()?.takeIf { it > 0 } ?: return
        val cid = (arc.call("getCid") as? Number)?.toLong()?.takeIf { it > 0 } ?: return
        activate(bvidFromAid(aid), cid.toString(), "response")
    }

    private fun activate(bvid: String, cid: String, source: String) {
        val key = "$bvid:$cid"
        val created = states.putIfAbsent(key, VideoState(bvid, cid)) == null
        val previousKey = activeKey
        activeKey = key
        if (created || previousKey != key) {
            logger.i("SponsorBlock active video: key=$key, source=$source, newState=$created, previous=$previousKey")
        }
    }

    private fun drawSegments(bar: ProgressBar, canvas: Canvas) {
        if (!isEnabled) return
        val state = currentState()
        if (state == null) {
            logMissingState("progress:${bar.javaClass.name}")
            return
        }
        val duration = bar.max.toLong().takeIf { it >= 1_000L }
        if (duration == null) {
            if (!state.invalidDurationLogged) {
                state.invalidDurationLogged = true
                logger.w("SponsorBlock invalid progress duration: key=${state.key}, view=${bar.javaClass.name}, max=${bar.max}")
            }
            return
        }
        state.durationMs = duration
        state.progressBar = WeakReference(bar)
        if (!state.progressLogged) {
            state.progressLogged = true
            logger.i("SponsorBlock progress bound: key=${state.key}, view=${bar.javaClass.name}, durationMs=$duration")
        }
        fetchSegments(state)
        val segments = state.segments.filter { modeFor(it.category) != Mode.IGNORE }
        if (segments.isEmpty()) return
        val track = bar.markerTrackBounds() ?: return
        val left = track.left
        val right = track.right
        val top = track.top
        val bottom = track.bottom
        val width = right - left
        val rect = RectF()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val saveCount = canvas.save()
        canvas.clipRect(track)
        bar.markerThumbBounds()?.let(canvas::clipOutRect)
        segments.forEach { segment ->
            val start = (left + segment.start * 1000f / duration * width).coerceIn(left, right)
            val end = (left + segment.end * 1000f / duration * width).coerceIn(left, right)
            if (end <= start) return@forEach
            rect.set(start, top, maxOf(end, start + 3 * bar.resources.displayMetrics.density), bottom)
            paint.color = categoryFor(segment.category)?.color ?: 0xfffb7299.toInt()
            canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint)
        }
        canvas.restoreToCount(saveCount)
        state.markerDrawn = true
        if (!state.markerLogged) {
            state.markerLogged = true
            logger.i("SponsorBlock markers drawn: key=${state.key}, count=${segments.size}, view=${bar.javaClass.name}")
        }
    }

    private fun checkPosition(player: Any, position: Long) {
        if (!isEnabled) return
        val state = currentState()
        if (state == null) {
            logMissingState("position:${player.javaClass.name}")
            return
        }
        if (!state.markerDrawn || position <= 0) {
            if (!state.positionWaitingLogged) {
                state.positionWaitingLogged = true
                logger.d("SponsorBlock position waiting: key=${state.key}, markerDrawn=${state.markerDrawn}, position=$position")
            }
            return
        }
        val previousPosition = state.lastCheck
        if (previousPosition != Long.MIN_VALUE && position >= previousPosition && position - previousPosition < 800) return
        if (position < previousPosition) state.promptedSegment = -1f
        state.lastCheck = position
        val segment = state.segments.firstOrNull { position >= it.start * 1000 && position < it.end * 1000 }
        if (segment == null) {
            state.promptedSegment = -1f
            return
        }
        when (modeFor(segment.category)) {
            Mode.AUTO -> seek(player, state, segment)
            Mode.MANUAL -> prompt(player, state, segment)
            else -> Unit
        }
    }

    private fun seek(player: Any, state: VideoState, segment: Segment) {
        val target = ((segment.end * 1000).roundToLong() + 500).coerceAtMost(state.durationMs.takeIf { it > 0 } ?: Long.MAX_VALUE)
        val methods = seekMethods.computeIfAbsent(player.javaClass, ::findSeekMethods)
        val method = methods.firstOrNull()
        if (method == null) {
            logger.w("SponsorBlock seek method missing: player=${player.javaClass.name}, key=${state.key}")
            return
        }
        val argument = target.coerceTo(method.parameterTypes[0])
        logger.i("SponsorBlock seek: key=${state.key}, category=${segment.category}, position=${state.lastCheck}, target=$target, method=${method.toGenericString()}")
        if (method.parameterCount == 1) method.invoke(player, argument) else method.invoke(player, argument, true)
        toast("已跳过${categoryFor(segment.category)?.label ?: segment.category}")
    }

    private fun prompt(player: Any, state: VideoState, segment: Segment) {
        if (state.promptedSegment == segment.start) return
        state.promptedSegment = segment.start
        val label = categoryFor(segment.category)?.label ?: segment.category
        logger.i("SponsorBlock manual segment: key=${state.key}, category=${segment.category}, start=${segment.start}, end=${segment.end}")
        mainHandler.post {
            val activity = ActivityUtils.topActivity
            if (activity == null || activity.isFinishing || activity.isDestroyed) {
                Toast.makeText(loaderContext.application, "检测到${label}片段", Toast.LENGTH_SHORT).show()
                return@post
            }
            val dialog = AlertDialog.Builder(activity)
                .setTitle("检测到${label}片段")
                .setMessage("${"%.1f".format(segment.start)}s - ${"%.1f".format(segment.end)}s")
                .setPositiveButton("跳过") { _, _ -> seek(player, state, segment) }
                .setNegativeButton("取消", null)
                .show()
            mainHandler.postDelayed({ if (dialog.isShowing) dialog.dismiss() }, MANUAL_PROMPT_TIMEOUT_MS)
        }
    }

    private fun fetchSegments(state: VideoState) {
        val categories = CATEGORIES.filter { modeFor(it.key) != Mode.IGNORE }.map { it.key }.toSet()
        if (state.loaded && state.loadedCategories == categories) return
        if (!state.fetching.compareAndSet(false, true)) return
        logger.i("SponsorBlock fetch start: key=${state.key}, categories=${categories.sorted().joinToString()}")
        Thread {
            try {
                state.segments = requestSegments(state.bvid, state.cid, categories)
                state.loadedCategories = categories
                state.loaded = true
                logger.i("SponsorBlock fetch complete: key=${state.key}, segments=${state.segments.size}")
            } catch (throwable: Throwable) {
                logger.w("SponsorBlock request failed for ${state.bvid}", throwable)
            } finally {
                state.fetching.set(false)
                mainHandler.post { state.progressBar?.get()?.invalidate() }
            }
        }.apply { name = "YABR-SponsorBlock"; isDaemon = true; start() }
    }

    private fun requestSegments(bvid: String, cid: String, categories: Set<String>): List<Segment> {
        if (categories.isEmpty()) return emptyList()
        val prefix = MessageDigest.getInstance("SHA-256").digest(bvid.toByteArray()).joinToString("") { "%02x".format(it) }.take(4)
        for (base in API_BASES) {
            try {
                val connection = URL(base + prefix).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000
                    connection.setRequestProperty("accept", "application/json")
                    connection.setRequestProperty("origin", "YABR")
                    connection.setRequestProperty("user-agent", "Mozilla/5.0 (Linux; Android; Xposed) YABR")
                    val responseCode = connection.responseCode
                    logger.d("SponsorBlock mirror response: url=$base$prefix, code=$responseCode")
                    if (responseCode !in 200..299) continue
                    val entries = JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
                    for (index in 0 until entries.length()) {
                        val entry = entries.optJSONObject(index) ?: continue
                        if (entry.optString("videoID") != bvid) continue
                        val segments = entry.optJSONArray("segments")?.let { items ->
                            buildList {
                                for (segmentIndex in 0 until items.length()) {
                                    val item = items.optJSONObject(segmentIndex) ?: continue
                                    val range = item.optJSONArray("segment") ?: continue
                                    val start = range.optDouble(0, Double.NaN).toFloat()
                                    val end = range.optDouble(1, Double.NaN).toFloat()
                                    if (start.isFinite() && end > start && item.optString("actionType").equals("skip", true) &&
                                        item.optString("category") in categories &&
                                        (item.optString("cid").isBlank() || item.optString("cid") == cid)
                                    ) add(Segment(start, end, item.optString("category")))
                                }
                            }
                        }.orEmpty().sortedBy(Segment::start)
                        logger.i("SponsorBlock mirror matched: url=$base$prefix, bvid=$bvid, cid=$cid, segments=${segments.size}")
                        return segments
                    }
                    logger.d("SponsorBlock mirror has no matching video: url=$base$prefix, entries=${entries.length()}, bvid=$bvid")
                } finally {
                    connection.disconnect()
                }
            } catch (exception: Exception) {
                logger.w("SponsorBlock mirror failed: $base", exception)
            }
        }
        logger.w("SponsorBlock no usable mirror result: bvid=$bvid, cid=$cid, prefix=$prefix")
        return emptyList()
    }

    private fun currentState(): VideoState? = states[activeKey]

    private fun logMissingState(source: String) {
        if (missingStateSources.add(source)) {
            logger.w("SponsorBlock callback before video identity: source=$source, activeKey=$activeKey")
        }
    }

    private fun ProgressBar.markerTrackBounds(): RectF? {
        if (width <= 0 || height <= 0) return null
        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = (width - paddingRight).toFloat()
        val contentBottom = (height - paddingBottom).toFloat()
        if (contentRight <= contentLeft || contentBottom <= contentTop) return null

        val bounds = progressDrawable?.bounds
        if (bounds != null && bounds.width() > 0 && bounds.height() > 0) {
            val relative = bounds.left >= 0 && bounds.top >= 0 &&
                bounds.right <= contentRight - contentLeft + BOUNDS_EPSILON &&
                bounds.bottom <= contentBottom - contentTop + BOUNDS_EPSILON
            val left = if (relative) contentLeft + bounds.left else bounds.left.toFloat()
            val right = if (relative) contentLeft + bounds.right else bounds.right.toFloat()
            val top = if (relative) contentTop + bounds.top else bounds.top.toFloat()
            val bottom = if (relative) contentTop + bounds.bottom else bounds.bottom.toFloat()
            if (left >= 0f && top >= 0f && right <= width && bottom <= height && right > left && bottom > top) {
                return RectF(left, top, right, bottom)
            }
        }
        return RectF(contentLeft, contentTop, contentRight, contentBottom)
    }

    private fun ProgressBar.markerThumbBounds(): RectF? {
        val seekBar = this as? SeekBar ?: return null
        val thumb = seekBar.thumb ?: return null
        val bounds = thumb.bounds
        if (bounds.isEmpty) return null
        val offsetX = paddingLeft - seekBar.thumbOffset
        return RectF(
            (bounds.left + offsetX).toFloat(),
            (bounds.top + paddingTop).toFloat(),
            (bounds.right + offsetX).toFloat(),
            (bounds.bottom + paddingTop).toFloat(),
        )
    }

    private fun modeFor(category: String): Mode {
        val default = categoryFor(category)?.defaultMode ?: Mode.IGNORE
        return Mode.entries.getOrElse(config.getInt("mode_$category", default.ordinal) ?: default.ordinal) { default }
    }

    private fun categoryFor(key: String) = CATEGORIES.firstOrNull { it.key == key }

    private fun toast(message: String) = mainHandler.post { Toast.makeText(loaderContext.application, message, Toast.LENGTH_SHORT).show() }

    override fun onCreateUI(context: Context): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(32, 32, 32, 32)
        addView(Switch(context).apply {
            text = "启用空降助手"
            isChecked = this@SponsorBlock.isEnabled
            setOnCheckedChangeListener { _, checked -> this@SponsorBlock.isEnabled = checked }
        })
        CATEGORIES.forEach { category ->
            addView(TextView(context).apply { text = category.label })
            addView(android.widget.Spinner(context).apply {
                adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, Mode.entries.map(Mode::label))
                setSelection(modeFor(category.key).ordinal)
                onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                        config.putInt("mode_${category.key}", position)
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
                }
            })
        }
    }

    private fun Any?.call(name: String): Any? {
        this ?: return null
        return javaClass.allMethods().firstOrNull { it.name == name && it.parameterCount == 0 }?.invoke(this)
    }

    private fun Class<*>.allMethods(): Sequence<Method> = sequence {
        var type: Class<*>? = this@allMethods
        while (type != null) { yieldAll(type.declaredMethods.asSequence()); type = type.superclass }
    }

    private fun Class<*>.hasMethod(name: String) = allMethods().any { it.name == name && it.parameterCount == 0 }
    private fun findHostDrawMethod(type: Class<*>): Method? = generateSequence(type) { it.superclass }
        .takeWhile { current ->
            val name = current.name
            !name.startsWith("android.") && !name.startsWith("androidx.")
        }
        .flatMap { it.declaredMethods.asSequence() }
        .firstOrNull { method ->
            !Modifier.isAbstract(method.modifiers) && !Modifier.isNative(method.modifiers) &&
                method.name == "onDraw" && method.parameterTypes.contentEquals(arrayOf(Canvas::class.java))
        }
    private fun Class<*>.isNumberType() =
        this == Int::class.javaPrimitiveType || this == Int::class.javaObjectType ||
            this == Long::class.javaPrimitiveType || this == Long::class.javaObjectType ||
            this == Short::class.javaPrimitiveType || this == Short::class.javaObjectType
    private fun ClassLoader.loadClassOrNull(name: String): Class<*>? = runCatching { loadClass(name) }.getOrNull()

    private fun findSeekMethods(type: Class<*>): List<Method> = type.allMethods().filter { method ->
        val validParameters = method.parameterTypes.firstOrNull()?.isNumberType() == true &&
            (method.parameterCount == 1 ||
            (method.parameterCount == 2 &&
                (method.parameterTypes[1] == Boolean::class.javaPrimitiveType ||
                    method.parameterTypes[1] == Boolean::class.javaObjectType)))
        when (method.name) {
            "seekTo", "seek" -> validParameters
            "q" -> validParameters && method.parameterCount == 2
            else -> false
        }
    }.sortedWith(compareBy<Method>({ SEEK_METHOD_NAMES.indexOf(it.name) }, Method::getParameterCount))
        .onEach { it.isAccessible = true }
        .toList()
        .also { methods ->
            logger.d("SponsorBlock seek candidates for ${type.name}:\n${methods.joinToString("\n", transform = Method::toGenericString)}")
        }

    private fun Long.coerceTo(type: Class<*>): Any = when (type) {
        Int::class.javaPrimitiveType, Int::class.javaObjectType -> toInt()
        Short::class.javaPrimitiveType, Short::class.javaObjectType -> toShort()
        else -> this
    }

    private fun bvidFromAid(aid: Long): String {
        val result = CharArray(12) { if (it < 3) "BV1"[it] else '0' }
        var value = ((1L shl 51) or aid) xor 23442827791579L
        var index = 11
        while (value > 0) { result[index--] = BV_TABLE[(value % 58).toInt()]; value /= 58 }
        result[3] = result[9].also { result[9] = result[3] }; result[4] = result[7].also { result[7] = result[4] }
        return String(result)
    }

    private data class VideoState(val bvid: String, val cid: String) {
        val key = "$bvid:$cid"
        @Volatile var durationMs = 0L
        @Volatile var segments = emptyList<Segment>()
        @Volatile var loaded = false
        @Volatile var loadedCategories = emptySet<String>()
        @Volatile var markerDrawn = false
        @Volatile var lastCheck = Long.MIN_VALUE
        @Volatile var promptedSegment = -1f
        @Volatile var progressBar: WeakReference<ProgressBar>? = null
        @Volatile var progressLogged = false
        @Volatile var invalidDurationLogged = false
        @Volatile var markerLogged = false
        @Volatile var positionWaitingLogged = false
        val fetching = java.util.concurrent.atomic.AtomicBoolean()
    }
    private data class Segment(val start: Float, val end: Float, val category: String)
    private data class Category(val key: String, val label: String, val color: Int, val defaultMode: Mode)
    private enum class Mode(val label: String) { AUTO("自动跳过"), MANUAL("手动提示"), BAR("仅显示进度条"), IGNORE("忽略") }

    private val CATEGORIES = listOf(
        Category("sponsor", "赞助推广", 0xff00d400.toInt(), Mode.AUTO),
        Category("selfpromo", "自我推广", 0xffffff00.toInt(), Mode.MANUAL),
        Category("interaction", "互动提醒", 0xffcc00ff.toInt(), Mode.MANUAL),
        Category("intro", "片头", 0xff00ffff.toInt(), Mode.MANUAL),
        Category("outro", "片尾", 0xff0202ed.toInt(), Mode.MANUAL),
        Category("preview", "预告", 0xff008fd6.toInt(), Mode.MANUAL),
        Category("music_offtopic", "非正片音乐", 0xffff9900.toInt(), Mode.BAR),
        Category("poi_highlight", "精彩片段", 0xffff1684.toInt(), Mode.MANUAL),
        Category("filler", "无关填充", 0xff7300ff.toInt(), Mode.BAR),
        Category("exclusive_access", "付费内容", 0xff008a5c.toInt(), Mode.BAR),
    )
    private val PLAYER_MOSS_NAMES = setOf("PlayerMoss", "KPlayerMoss", "PlayURLMoss")
    private val PLAYER_MOSS_CLASSES = setOf(
        "com.bapis.bilibili.app.playerunite.v1.PlayerMoss",
        "com.bapis.bilibili.app.playerunite.v1.KPlayerMoss",
    )
    private val CONTROLLER_NAMES = setOf("PlayerCoreService", "PlayerCoreServiceV2", "PlayerContext", "StoryPlayer")
    private val PROGRESS_VIEW_NAMES = setOf("com.bilibili.playerbizcommonv2.widget.seek.v3.PlayerSeekWidget3", "com.bilibili.video.story.view.StorySeekBar", "com.bilibili.app.comm.list.common.inline.widgetV3.InlineProgressWidgetV3")
    private val PLAY_VIEW_METHOD_NAMES = setOf("executePlayView", "executePlayViewUnite", "playViewUnite", "playView")
    private val POSITION_METHOD_NAMES = setOf("getCurrentPosition", "getCurrentPositionMs")
    private val SEEK_METHOD_NAMES = listOf("seekTo", "seek", "q")
    private val API_BASES = listOf("https://bsbsb.top/api/skipSegments/", "https://www.bsbsb.xyz/api/skipSegments/")
    private const val BV_TABLE = "FcwAPNKTMug3GV5Lj7EJnHpWsx4tb8haYeviqBz6rkCy12mUSDQX9RdoZf"
    private const val BOUNDS_EPSILON = 1f
    private const val MANUAL_PROMPT_TIMEOUT_MS = 3_000L
}
