import app.revanced.patcher.data.ResourceContext
import app.revanced.patcher.patch.ResourcePatch
import app.revanced.patcher.patch.annotation.Patch
import org.w3c.dom.Element

@Patch(
    name = "YABR ReVanced Patcher",
    description = "load YABR",
    use = true,
    requiresIntegrations = true
)
@Suppress("unused")
object YabrPatchPlugin : ResourcePatch() {
    override fun execute(context: ResourceContext) {
        context.document["AndroidManifest.xml"].use { document ->
            val application =
                (document.getNode("manifest") as Element).getNode("application") as Element
            val applicationName = application.getAttribute("android:appComponentFactory")
            application.setAttribute("android:appComponentFactory", "io.github.duzhaokun123.loader.afc.AppComponentFactory")
            val metadata = document.createElement("meta-data").apply {
                setAttribute("android:name", "originalAppComponentFactory")
                setAttribute("android:value", applicationName)
            }
            application.appendChild(metadata)
        }
    }
}
