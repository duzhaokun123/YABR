package io.github.duzhaokun123.yabr.module.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import io.github.duzhaokun123.module.base.ModuleEntry
import io.github.duzhaokun123.yabr.R
import io.github.duzhaokun123.yabr.module.UICategory
import io.github.duzhaokun123.yabr.module.base.BaseModule
import io.github.duzhaokun123.yabr.module.base.UIActivity
import io.github.duzhaokun123.yabr.module.base.loadSkip
import io.github.duzhaokun123.yabr.module.core.ModuleActivity
import io.github.duzhaokun123.yabr.module.core.ModuleActivityMeta
import io.github.duzhaokun123.yabr.utils.ModuleEntryTarget

@ModuleEntry(
    id = "about_libraries",
    targets = [ModuleEntryTarget.MAIN]
)
object AboutLibraries : BaseModule(), UIActivity {
    override val name = "About Libraries"
    override val description = "open source libraries used in this app"
    override val category = UICategory.ABOUT

    override fun onLoad() = loadSkip()

    override val moduleActivity = AboutLibrariesActivity::class.java
}

@ModuleActivity
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
class AboutLibrariesActivity : ComponentActivity(), ModuleActivityMeta {
    override val theme = androidx.appcompat.R.style.Theme_AppCompat_DayNight

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val colorScheme =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val context = LocalContext.current
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                } else {
                    if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
                }
            MaterialTheme(colorScheme) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("About Libraries")
                            }
                        )
                    }
                ) {  innerPadding ->
                    val libraries by produceLibraries(R.raw.aboutlibraries)
                    LibrariesContainer(libraries, Modifier.padding(innerPadding),
                        showAuthor = true,
                        showDescription = true,
                        showVersion = true,
                        showLicenseBadges = true,
                        showFundingBadges = true)
                }
            }
        }
    }
}