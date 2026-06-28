package com.xeton.util.compose.localizationmanager

import com.xeton.resources.contracts.MyLanguageResource

/**
 * at the moment we only use bundled strings
 */
class LanguageSourceProvider(
    val defaultLanguageResource: MyLanguageResource,
    val allLanguageResources: List<MyLanguageResource>,
)
