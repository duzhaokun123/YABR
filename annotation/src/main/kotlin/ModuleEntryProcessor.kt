import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStreamWriter

class ModuleEntryProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
    val packageName = "io.github.duzhaokun123.codegen"

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("io.github.duzhaokun123.module.base.ModuleEntry")
            .filter { it is KSClassDeclaration }

        val out = try {
            codeGenerator.createNewFile(
                Dependencies(
                    aggregating = true,
                    sources = symbols.map { it.containingFile!! }.toList().toTypedArray()
                ), packageName, "ModuleEntries"
            )
        } catch (e: FileAlreadyExistsException) {
            return emptyList()
        }
        val writer = OutputStreamWriter(out)
        writer.write("package $packageName\n\n")
        writer.write("object ModuleEntries {\n")
        writer.write("val entries = listOf(\n")
        symbols.forEach { module ->
                val className = (module as KSClassDeclaration).safeQualifiedName
                logger.info("Found module entry: $className.")
                if (module.classKind != ClassKind.OBJECT) {
                    logger.error("ModuleEntry must be an object, ${module.safeQualifiedName}", module)
                    return@forEach
                }
                writer.write("${className},\n")
            }
        writer.write(")\n")
        writer.write("}\n")
        writer.flush()
        writer.close()
        out.close()
        logger.info("Module entries generated in $packageName.ModuleEntries")
        return symbols.toList()
    }
}

class ModuleEntryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ModuleEntryProcessor(environment.codeGenerator, environment.logger)
    }
}