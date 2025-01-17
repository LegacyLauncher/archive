package net.legacylauncher.gradle

import org.gradle.api.file.*
import org.gradle.api.provider.*
import org.gradle.process.*
import java.io.*
import javax.inject.*

abstract class StdOutExecValueSource : ValueSource<String, StdOutExecValueSource.Parameters> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String {
        val stdout = java.io.ByteArrayOutputStream()
        val failureResult = parameters.failureResult
        failureResult.finalizeValue()
        val execResult = execOperations.exec {
            commandLine(parameters.args.get())
            if (parameters.workingDir.isPresent) {
                workingDir(parameters.workingDir)
            }
            standardOutput = stdout
            errorOutput = OutputStream.nullOutputStream()
            isIgnoreExitValue = failureResult.isPresent
        }
        if (execResult.exitValue != 0) {
            return failureResult.get()
        }
        return stdout.toString(Charsets.UTF_8).trim()
    }

    interface Parameters : ValueSourceParameters {
        val args: ListProperty<String>
        val workingDir: DirectoryProperty
        val failureResult: Property<String>

        fun args(vararg args: String) {
            this.args.addAll(*args)
        }
    }
}
