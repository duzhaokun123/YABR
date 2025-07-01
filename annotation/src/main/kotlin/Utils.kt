import com.google.devtools.ksp.symbol.KSClassDeclaration

val KSClassDeclaration.safeQualifiedName: String
    get() {
        val packageName = qualifiedName!!.getQualifier()
        val className = qualifiedName!!.getShortName()
        return "`$packageName`.`$className`"
    }