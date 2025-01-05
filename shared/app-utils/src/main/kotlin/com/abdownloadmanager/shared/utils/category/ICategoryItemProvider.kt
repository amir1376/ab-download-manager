package com.abdownloadmanager.shared.utils.category

interface ICategoryItemProvider {
    suspend fun getAll(): List<CategoryItemWithId>
}