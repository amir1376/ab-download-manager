package com.abdownloadmanager.shared.util.category

interface ICategoryItemProvider {
    suspend fun getAll(): List<CategoryItemWithId>
}
