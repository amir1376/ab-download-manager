package ir.amirab

object SelectionUtil {
    inline fun <T, ID> invertSelection(
        selectionList: List<ID>,
        all: List<T>,
        getId: (T) -> ID
    ): List<ID> {
        return all
            .filterNot { getId(it) in selectionList }
            .map { getId(it) }
    }

    inline fun <T, ID> toggleSelectInside(
        selectionList: List<ID>,
        fullSortedList: List<T>,
        getId: (T) -> ID,
    ): List<ID>? {
        val selectionSet = selectionList.toSet()
        val startIndex = fullSortedList.indexOfFirst {
            getId(it) in selectionSet
        }
        val endIndex = fullSortedList.indexOfLast {
            getId(it) in selectionSet
        }
        if (startIndex == -1 || endIndex == -1) {
            return null
        }
        val startItem = getId(fullSortedList[startIndex])
        val endItem = getId(fullSortedList[endIndex])
        return if ((endIndex - startIndex + 1) == selectionSet.size) {
            listOf(startItem, endItem)
        } else {
            selectInside(
                sortedList = fullSortedList,
                startItem = startItem,
                endItem = endItem,
                getID = getId,
            )
        }
    }

    // ONLY PASS SORTED LIST!
    inline fun <Item, ID> getARangeOfItems(
        sortedList: List<Item>,
        id: (Item) -> ID,
        fromItem: ID,
        toItem: ID,
    ): List<ID> {
        return sortedList.map(id).dropWhile {
            it != fromItem && it != toItem
        }.dropLastWhile {
            it != fromItem && it != toItem
        }
    }

    inline fun <T, ID> selectInside(
        sortedList: List<T>,
        startItem: ID,
        endItem: ID,
        getID: (T) -> ID
    ): List<ID> {
        val ids: List<ID> = getARangeOfItems(
            sortedList = sortedList,
            id = getID,
            fromItem = startItem,
            toItem = endItem,
        )
        return ids
    }
}
