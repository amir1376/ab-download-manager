package ir.amirab.util

import java.util.concurrent.TimeUnit

/**
 * this helper function is here to execute a command and waits for the process to finish and return the result based on exit code
 * @param command the command
 * @param waitFor maximum time allowed process finish ( in milliseconds )
 * @return `true` when process exits with `0` exit code, `false` if the process fails with non-zero exit code or execution time exceeds the [waitFor]
 */
fun execAndWait(
    command: Array<String>,
    waitFor: Long = 2_000,
): Boolean {
    return runCatching {
        val p = Runtime.getRuntime().exec(command)
        val exited = p.waitFor(waitFor, TimeUnit.MILLISECONDS)
        if (exited) {
            p.exitValue() == 0
        } else {
            false
        }
    }.getOrElse { false }
}