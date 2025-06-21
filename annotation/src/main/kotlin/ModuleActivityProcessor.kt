import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStreamWriter
import java.io.Writer

class ModuleActivityProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
    val packageName = "io.github.duzhaokun123.codegen"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("io.github.duzhaokun123.yabr.module.core.ModuleActivity")
            .filter { it is KSClassDeclaration }

        val out = try {
            codeGenerator.createNewFile(
                Dependencies(
                    aggregating = true,
                    sources = symbols.map { it.containingFile!! }.toList().toTypedArray()
                ), packageName, "ModuleActivities"
            )
        } catch (e: FileAlreadyExistsException) {
            return emptyList()
        }
        val writer = OutputStreamWriter(out)
        writer.write("package $packageName\n\n")
        writer.write("object ModuleActivities {\n")
        writer.write("val activities = listOf(\n")
        symbols.forEach { module ->
                val className = (module as KSClassDeclaration).qualifiedName!!.asString()
                logger.info("Found module entry: $className.")
                if (module.classKind != ClassKind.CLASS) {
                    logger.error("ModuleActivity must be a class", module)
                    return@forEach
                }
                writer.write("${className}::class,\n")
            }
        writer.write(")\n")
        writer.write("}\n")
        writer.flush()
        writer.close()
        out.close()
        logger.info("Module activities generated in $packageName.ModuleActivities")
        return symbols.toList()
    }
}

class ModuleActivityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleActivityProcessor(environment.codeGenerator, environment.logger)
    }
}