import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File

/** Uses Kotlin's real parser; does not resolve Android or application types. */
fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Pass Kotlin files to parse" }
    val disposable = Disposer.newDisposable()
    try {
        val environment = KotlinCoreEnvironment.createForProduction(
            disposable, CompilerConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES,
        )
        val factory = KtPsiFactory(environment.project)
        var failures = 0
        args.forEach { path ->
            val file = File(path)
            val parsed = factory.createFile(file.name, file.readText())
            PsiTreeUtil.collectElementsOfType(parsed, PsiErrorElement::class.java).forEach {
                System.err.println("$path:${it.textOffset}: ${it.errorDescription}")
                failures++
            }
        }
        check(failures == 0) { "$failures Kotlin syntax errors" }
        println("${args.size} Kotlin files parsed without syntax errors (not Android compilation).")
    } finally {
        Disposer.dispose(disposable)
    }
}
