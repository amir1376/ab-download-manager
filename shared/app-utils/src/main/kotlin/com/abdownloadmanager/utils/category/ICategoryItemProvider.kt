package com.abdownloadmanager.utils.category

interface ICategoryItemProvider {
    suspend fun getAll(): List<CategoryItemWithId>
}