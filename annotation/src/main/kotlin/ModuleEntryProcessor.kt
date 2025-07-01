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
                ), packageName, "ModuleEntries", extensionName = "java"
            )
        } catch (e: FileAlreadyExistsException) {
            return emptyList()
        }
        val writer = OutputStreamWriter(out)
        writer.write("package $packageName;\n\n")
        writer.write("import io.github.duzhaokun123.yabr.module.base.BaseModule;\n\n")
        writer.write("public class ModuleEntries {\n")
        writer.write("\tpublic static BaseModule[] entries = new BaseModule[] {\n")
        symbols.forEach { module ->
                val className = (module as KSClassDeclaration).qualifiedName!!.asString()
                logger.info("Found module entry: $className.")
                writer.write("\t\t${className}.INSTANCE,\n")
            }
        writer.write("\t};\n")
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