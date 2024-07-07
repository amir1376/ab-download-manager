package ir.amirab.git_version.core

import ir.amirab.git_version.core.CiReferenceProvider.Companion.getEnv
import ir.amirab.git_version.core.CiReferenceProvider.Companion.safeEnv


interface CiReferenceProvider {
    fun isAvailable(): Boolean
    fun getSha(): String?
    fun getRef(): String?
    fun getReference(): GitReference? {
        return refOrNull(getRef(), getSha())
    }

    companion object {
        var envProvider: (String) -> String? = { System.getenv(it) }
        fun getEnv(string: String): String? {
            return envProvider(string)
        }
        fun safeEnv(string: String): String? {
            return getEnv(string)?.takeIf {
                it.isNotBlank()
            }
        }


        private val registeredItems = linkedSetOf<CiReferenceProvider>()
        private fun builtIns(): Set<CiReferenceProvider> {
            return setOf(
                GithubCiReferenceProvider,
                GitlabCiReferenceProvider,
                CircleCiReferenceProvider,
                JenkinsCiReferenceProvider,
            )
        }

        init {
            builtIns().forEach { add(it) }
        }

        fun add(ciReferenceProvider: CiReferenceProvider) {
            registeredItems.add(ciReferenceProvider)
        }

        fun isInCi() = getAll().any { it.isAvailable() }
        fun getAll(): Set<CiReferenceProvider> {
            return registeredItems
        }
    }
}

private fun refOrNull(ref: String?, sha: String? = null): GitReference? {
    return ref?.let {
        GitReference.of(ref, sha)
    }
}

object GithubCiReferenceProvider : CiReferenceProvider {
    override fun isAvailable(): Boolean {
        return getEnv("GITHUB_CI")?.lowercase() == "true"
    }

    override fun getRef(): String? {
        return safeEnv("GITHUB_REF")
    }

    override fun getSha(): String? {
        return safeEnv("GITHUB_SHA")
    }
}

object GitlabCiReferenceProvider : CiReferenceProvider {
    override fun isAvailable(): Boolean {
        return getEnv("GITLAB_CI")?.lowercase() == "true"
    }

    override fun getRef(): String? {
        return safeEnv("CI_COMMIT_BRANCH")
                ?: safeEnv("CI_COMMIT_TAG")
                ?: safeEnv("CI_MERGE_REQUEST_SOURCE_BRANCH_NAME")
    }

    override fun getSha(): String? {
        return null
    }
}

object CircleCiReferenceProvider : CiReferenceProvider {
    override fun isAvailable(): Boolean {
        return getEnv("CIRCLECI")?.lowercase() == "true"
    }

    override fun getRef(): String? {
        return safeEnv("CIRCLE_BRANCH")
                ?: safeEnv("CIRCLE_TAG")
    }

    override fun getSha(): String? {
        return null
    }
}

object JenkinsCiReferenceProvider : CiReferenceProvider {
    override fun isAvailable(): Boolean {
        return safeEnv("JENKINS_HOME") != null
    }

    override fun getRef(): String? {
        return safeEnv("BRANCH_NAME")
                ?: safeEnv("TAG_NAME")
    }

    override fun getSha(): String? {
        return null
    }
}

