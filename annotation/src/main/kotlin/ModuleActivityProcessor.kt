import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter
import kotlin.io.FileAlreadyExistsException

class ModuleActivityProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val className: String
) : SymbolProcessor {
    val packageName = "io.github.duzhaokun123.codegen"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("io.github.duzhaokun123.yabr.module.core.ModuleActivity")
            .filter { it is KSClassDeclaration }

        // Skip generation if no symbols found to avoid duplicate empty classes across modules
        if (symbols.toList().isEmpty()) return emptyList()

        val out = try {
            codeGenerator.createNewFile(
                Dependencies(
                    aggregating = true,
                    sources = symbols.map { it.containingFile!! }.toList().toTypedArray()
                ), packageName, className
            )
        } catch (e: FileAlreadyExistsException) {
            return emptyList()
        }
        val writer = OutputStreamWriter(out)
        writer.write("package $packageName\n\n")
        writer.write("object $className {\n")
        writer.write("val activities = listOf<String>(\n")
        symbols.forEach { module ->
                val symClassName = (module as KSClassDeclaration).qualifiedName?.asString()
                logger.info("Found module entry: $symClassName.")
                if (module.classKind != ClassKind.CLASS) {
                    logger.error("ModuleActivity must be a class, $symClassName", module)
                    return@forEach
                }
                writer.write("\"\"\"$symClassName\"\"\",\n")
            }
        writer.write(")\n")
        writer.write("}\n")
        writer.flush()
        writer.close()
        out.close()
        logger.info("Module activities generated in $packageName.$className")
        return symbols.toList()
    }
}

class ModuleActivityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val className = environment.options["activitiesClassname"] ?: "ModuleActivities"
        return ModuleActivityProcessor(environment.codeGenerator, environment.logger, className)
    }
}