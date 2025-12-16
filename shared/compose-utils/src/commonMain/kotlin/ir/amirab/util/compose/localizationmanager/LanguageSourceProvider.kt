package ir.amirab.util.compose.localizationmanager

import ir.amirab.resources.contracts.MyLanguageResource

/**
 * at the moment we only use bundled strings
 */
class LanguageSourceProvider(
    val defaultLanguageResource: MyLanguageResource,
    val allLanguageResources: List<MyLanguageResource>,
)
