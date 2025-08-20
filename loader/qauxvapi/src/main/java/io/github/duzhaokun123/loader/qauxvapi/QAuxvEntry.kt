package io.github.duzhaokun123.loader.qauxvapi

import android.app.Application
import android.util.Log
import io.github.duzhaokun123.hooker.qauxvapi.QAuxvHookerContext
import io.github.duzhaokun123.loader.base.ImplementationInfo
import io.github.duzhaokun123.loader.base.LoaderContext
import io.github.duzhaokun123.yabr.Main
import io.github.qauxv.chainloader.api.ChainLoaderAgent
import java.lang.reflect.Method

class QAuxvEntry(
    val modulePath: String,
    val hostDataDir: String,
    val xblService: Map<String, Method>?
) : Runnable {

    override fun run() {
        runCatching {
            Log.d("QAuxvEntry", "Loading QAuxvAPI with modulePath=$modulePath, hostDataDir=$hostDataDir, xblService=$xblService")

            val hookerContext = QAuxvHookerContext()
            val loaderContext = object : LoaderContext {
                override val implementationInfo: ImplementationInfo
                    get() = ImplementationInfo(
                        name = "QAuxvAPI",
                        version = "idk",
                        description = "QAuxvAPI Loader"
                    )
                override val hostClassloader: ClassLoader
                    get() = ChainLoaderAgent.getHostClassLoader()
                override val processName: String
                    get() = ChainLoaderAgent.getProcessName()
                override val application: Application
                    get() = ChainLoaderAgent.getHostApplication()
                override val modulePath: String
                    get() = ChainLoaderAgent.getHostApplication().packageManager.getApplicationInfo(Main.packageName, 0).sourceDir
            }
            Main.main(loaderContext, hookerContext)
        }.onFailure { t ->
            Log.e("QAuxvEntry", "Failed to load QAuxvAPI: ", t)
        }
    }
}