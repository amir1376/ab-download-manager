package com.abdownloadmanager.shared.utils.category

class InMemoryCategoryStorage : CategoryStorage {
    private var categories = emptyList<Category>()

    override suspend fun setCategories(categories: List<Category>) {
        this.categories = categories
    }

    override suspend fun getCategories(): List<Category> {
        return categories
    }

    override suspend fun isCategoriesSet(): Boolean {
        return true
    }
}