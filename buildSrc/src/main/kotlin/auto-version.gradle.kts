import net.legacylauncher.gradle.*

project.version = providers.of(StdOutExecValueSource::class) {
    parameters {
        args("git", "describe", "--tags", "--always", "--match", "[0-9].*")
        failureResult = "detached"
    }
}.get()
