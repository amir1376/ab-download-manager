package ir.amirab.git_version.core

import org.eclipse.jgit.lib.Constants.*
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.Repository.shortenRefName
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk


private fun Repository.fullBranchOrNull(): String? {
    return fullBranch.takeIf { !ObjectId.isId(it) }
}

private fun Repository.tagsPointAt(pointAtThisObject: ObjectId): List<Ref> {
    return refDatabase.getRefsByPrefix(R_TAGS).filter {
        it.toString()
        refDatabase.peel(it).run {
            peeledObjectId ?: objectId
        } == pointAtThisObject
    }
}

class GitStatus(
    val repository: Repository,
) {
    val head = repository.resolve(HEAD)

    val branch: GitReference.BranchInfo? by lazy {
        repository.fullBranchOrNull()?.let {
            GitReference.BranchInfo(it, head.name)
        }
    }

    val tags by lazy {
        RevWalk(repository).use { revWalk ->
            repository.tagsPointAt(head).map {
                val f = revWalk.parseAny(it.objectId)
                GitReference.TagInfo(
                    fullName = it.name,
                    commitHash = head.name,
                    createdAt = (f as? RevTag)?.taggerIdent?.`when`?.time,
                )
            }
        }
    }

    fun isDetached() = branch == null
}

sealed interface GitReference {
    val commitHash: String?

    sealed class SymbolicReference : GitReference {
        abstract val fullName: String
        val shortenName by lazy { shortenRefName(fullName) }
    }

    data class TagInfo(
        override val fullName: String,
        override val commitHash: String? = null,
        val createdAt: Long? = null,
    ) : SymbolicReference()

    data class BranchInfo(
        override val fullName: String,
        override val commitHash: String? = null,
    ) : SymbolicReference()

    data class ShaReference(
        override val commitHash: String,
    ) : GitReference

    companion object {
        fun of(
            ref: String,
            sha: String?,
        ): GitReference {
            return when {
                ref.startsWith(R_TAGS) -> TagInfo(
                    fullName = ref,
                    commitHash = sha,
                )

                ref.startsWith(R_REMOTES)
                        || ref.startsWith(R_HEADS)
                -> BranchInfo(ref, sha)

                else -> error("'$ref' is not a valid ref name")
            }
        }
    }
}
