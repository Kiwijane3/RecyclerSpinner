package co.luoja.recyclerspinner

import co.luoja.recyclerspinner.NestedList.*

/**
 * A list that stores items in a two-level hierarchy, where elements can either be at the root of the hierarchy or embedded in a section.
 *
 * @param I: The Type of items stored in the list.
 * @param SI: The Type used to identify sections in this list.
 */
interface NestedList<SI, I>: List<Entry<SI, I>> {
    /**
     * An enumerated interface representing the possible contents at each index of the nested list.
     */
    sealed interface Entry<SI, I> {
        /**
         * A root-level [Item][I].
         *
         * @property item: The [Item][I] that this [Entry] represents.
         */
        data class Item<SI, I>(val item: I): Entry<SI, I>

        /**
         * A section containing several [Items][I].
         *
         * @property identifier: The [Identifier][SI] used to identify and describe this section.
         * @property items: The [Item][SI]s contained in this section.
         */
        data class Section<SI, I>(val identifier: SI, val items: List<I>): Entry<SI, I>
    }

}

/**
 * A [NestedList] that can be mutated.
 *
 * @param I: The Type of the items stored in the list.
 * @param SI: The Type of the objects used to identify sections in the list.
 */
interface MutableNestedList<SI, I>: NestedList<SI, I>, MutableList<Entry<SI, I>>

internal data class ConcreteNestedList<SI, I>(private val entries: List<Entry<SI, I>>): NestedList<SI, I>, List<Entry<SI, I>> by entries
internal data class ConcreteMutableNestedList<SI, I>(private val entries: MutableList<Entry<SI, I>>): MutableNestedList<SI, I>, MutableList<Entry<SI, I>> by entries

/**
 * Create a new [NestedList] containing the provided [entries]. The best way to generate entries is with the functions [item] and [section].
 */
fun <SI, I> nestedListOf(vararg entries: Entry<SI, I>): NestedList<SI, I> {
    return ConcreteNestedList(entries.toList())
}

/**
 * Create a new [MutableNestedList] containing the provided [entries]. The best way to generate entries is with the functions [item] and [section].
 */
fun <SI, I> mutableNestedListOf(vararg entries: Entry<SI, I>): MutableNestedList<SI, I> {
    return ConcreteMutableNestedList(entries.toMutableList())
}

/**
 * Generates an [Entry] representing the given [item] at the root level.
 */
fun <SI, I> item(value: I): Entry<SI, I> {
    return Entry.Item(value)
}

/**
 * Generates an [Entry] representing a section containing the given [items], which is identified and described by the given [identifier].
 */
fun <SI, I> section(identifier: SI, vararg items: I): Entry<SI, I> {
    return Entry.Section(identifier, items.toList())
}

/**
 * Generates an [Entry] representing a section containing the given [items], which is identified and described by the given [identifier].
 */
fun <SI, I> section(identifier: SI, items: List<I>): Entry<SI, I> {
    return  Entry.Section(identifier, items)
}