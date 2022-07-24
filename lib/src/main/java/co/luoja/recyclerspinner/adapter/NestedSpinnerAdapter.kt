package co.luoja.recyclerspinner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.*
import co.luoja.recyclerspinner.*
import kotlin.properties.Delegates

/**
 * A [RecyclerSpinnerAdapter] that presents a two-level hierarchical list of items, where the second level elements can be hidden or displayed.
 *
 * @param SI: The Type of the objects used to identify sections. (Section Identifier)
 * @param I: The Type of the items that this [RecyclerSpinnerAdapter] is used to select.
 * @param SDVH: The Type of the [ViewHolder] used to display headers for each section
 * @param IDVH: The Type of [ViewHolder] used to manage the views used to display the items in the dropdown.
 * @param SVH: The Type of [ViewHolder] used to display the selected item in the spinner itself.
 *
 * @constructor Create a new [NestedSpinnerAdapter] which starts with the given [Item][I] selected and uses the given [DifferCallback] to calculate differences when its contents change.
 *
 * @see ListAdapter
 */
abstract class NestedSpinnerAdapter<SI: Any, I: Any, SDVH: ViewHolder, IDVH: ViewHolder, SVH: ViewHolder>(initiallySelected: I? = null, differ: DifferCallback<SI, I>): RecyclerSpinnerAdapter<I, NestedSpinnerAdapter.Element<SI, I>, ViewHolder, SVH>(initiallySelected, differ) {

    /**
     * Represents one of the underlying elements to be displayed in the dropdown's recycler view.
     *
     * This interface should not be used outside of this class. It is only public because it is used as a generic parameter of the super class.
     *
     * @suppress
     */
    sealed interface Element<SI, I> {
        /**
         * Represents a selectable item.
         *
         * @property item: The [Item][I] to be displayed.
         * @property sectionIdentifier: The [Identifier][SI] of the section that the [item] is currently being displayed in, or null if it is at the root level.
         *
         * @suppress
         */
        data class Item<SI, I>(internal val item: I, internal val sectionIdentifier: SI?): Element<SI, I>

        /**
         * Represents a header for a section.
         *
         * @property sectionIdentifier: The [Identifier][SI] of the section that this [Element] represents the header of.
         * @property expanded: Whether the section that this [Element] represents the header of is currently expanded.
         *
         * @suppress
         */
        data class SectionHeader<SI, I>(internal val sectionIdentifier: SI, internal val expanded: Boolean): Element<SI, I>
    }

    /**
     * Used by [NestedSpinnerAdapter] to determine whether [Items][I] and [Section Identifiers][SI] have the same identity and contents.
     *
     * @see DiffUtil.ItemCallback
     */
    abstract class DifferCallback<SI, I>: DiffUtil.ItemCallback<Element<SI, I>>() {

        /**
         * Called to check whether two [Section Identifiers][SI] represent the same section.
         *
         * @see DiffUtil.ItemCallback.areItemsTheSame
         */
        abstract fun doSectionIdsShareIdentity(a: SI, b: SI): Boolean

        /**
         * Called to check whether two [Section Identifiers][SI] have the same data.
         *
         * @see DiffUtil.ItemCallback.areContentsTheSame
         */
        abstract fun doSectionIdsShareContents(a: SI, b: SI): Boolean

        /**
         * Called to check whether two objects represent the same item.
         *
         * @see DiffUtil.ItemCallback.areItemsTheSame
         */
        abstract fun doItemsShareIdentity(a: I, b: I): Boolean

        /**
         * Called to check whether two objects have the same data.
         *
         * @see DiffUtil.ItemCallback.areContentsTheSame
         */
        abstract fun doItemsShareContents(a: I, b: I): Boolean

        /**@suppress*/
        final override fun areContentsTheSame(oldItem: Element<SI, I>, newItem: Element<SI, I>): Boolean {
            return if (oldItem is Element.Item<SI, I> && newItem is Element.Item<SI, I>) {
                doItemsShareContents(oldItem.item, newItem.item)
            } else if (oldItem is Element.SectionHeader<SI, I> && newItem is Element.SectionHeader<SI, I>) {
                doSectionIdsShareContents(oldItem.sectionIdentifier, newItem.sectionIdentifier) && oldItem.expanded == newItem.expanded
            } else {
                false
            }
        }

        /**@suppress*/
        final override fun areItemsTheSame(oldItem: Element<SI, I>, newItem: Element<SI, I>): Boolean {
            return if (oldItem is Element.Item<SI, I> && newItem is Element.Item<SI, I>) {
                doItemsShareIdentity(oldItem.item, newItem.item)
            } else if (oldItem is Element.SectionHeader<SI, I> && newItem is Element.SectionHeader<SI, I>) {
                doSectionIdsShareIdentity(oldItem.sectionIdentifier, newItem.sectionIdentifier)
            } else {
                false
            }
        }

    }

    /**
     * An interface for objects representing types of views to be displayed as section headers in the selection dropdown.
     *
     * Implementors should have properly defined overrides for [Object.equals] and [Object.hashCode],
     * as these are used by [RecyclerSpinnerAdapter]. If you are using kotlin, a good way to do this is to use a data class.
     */
    interface SectionViewType: ViewType {
            object Default : SectionViewType
        }

    /**
     * An interface for objects representing types of views to be displayed to represent items in the selection dropdown.
     *
     * Implementors should have properly defined overrides for [Object.equals] and [Object.hashCode],
     * as these are used by [RecyclerSpinnerAdapter]. If you are using kotlin, a good way to do this is to use a data class.
     */
    interface ItemViewType : ViewType {
        object Default : ItemViewType
    }

    /**
     * Creates a new [NestedSpinnerAdapter] using the given [differ], which initially has no item selected.
     */
    constructor(differ: DifferCallback<SI, I>): this(null, differ)

    /**
     * The list defining the current contents of the spinner.
     */
    private var contents: NestedList<SI, I>? by Delegates.observable(null) { _, _, _ ->
        dispatch()
    }

    private val expansionMap = HashMap<SI, Boolean>()

    /**@suppress*/
    final override fun generateElements(): List<Element<SI, I>>? {
        return contents?.flatMap(this::elementsForEntry)
    }

    private fun elementsForEntry(entry: NestedList.Entry<SI, I>): List<Element<SI, I>> {
        return when(entry) {
            is NestedList.Entry.Item<SI, I> -> listOf(elementForEntryItem(entry))
            is NestedList.Entry.Section<SI, I> -> elementsForEntrySection(entry)
        }
    }

    private fun elementForEntryItem(entry: NestedList.Entry.Item<SI, I>): Element.Item<SI, I> {
        return Element.Item(entry.item, null)
    }

    private fun elementsForEntrySection(entry: NestedList.Entry.Section<SI, I>): List<Element<SI, I>> {
        return if (isExpanded(entry.identifier))
            listOf(Element.SectionHeader<SI, I>(entry.identifier, true)) + entry.items.map { item ->
                Element.Item(item, entry.identifier)
            }
        else
            listOf(Element.SectionHeader<SI, I>(entry.identifier, false))
    }

    /**
     * Submits a nested list to be displayed in the spinner.
     *
     * @see ListAdapter.submitList
     */
    fun submitNestedList(list: NestedList<SI, I>?) {
        contents = list
    }

    /**
     * Toggles whether the section identified by the given [sectionIdentifier] is expanded.
     */
    fun toggleExpanded(sectionIdentifier: SI) {
        setIsExpanded(sectionIdentifier, !isExpanded(sectionIdentifier))
    }

    /**
     * Expands the section identified by the given [sectionIdentifier] if it is not currently expanded.
     */
    fun expand(sectionIdentifier: SI) {
        setIsExpanded(sectionIdentifier, true)
    }

    /**
     * Collapses the section identified by the given [sectionIdentifier] if it is currently expanded.
     */
    fun collapse(sectionIdentifier: SI) {
        setIsExpanded(sectionIdentifier, false)
    }

    fun setIsExpanded(sectionIdentifier: SI, expanded: Boolean) {

        if (expanded == expansionMap[sectionIdentifier]) return

        expansionMap[sectionIdentifier] = expanded
        dispatch()
    }

    /**
     * Returns whether the section identified by the given [sectionIdentifier] is currently expanded.
     */
    fun isExpanded(sectionIdentifier: SI): Boolean {
        return expansionMap[sectionIdentifier] ?: false
    }

    /**
     * Determines the [type][SectionViewType] of the view that should be used to display the header for the section with the given [sectionIdentifier].
     *
     * @param expanded: Whether the section is currently expanded.
     * @return The [SectionViewType] of the view to be used for the section header.
     *
     * @see RecyclerView.Adapter.getItemViewType
     */
    open fun viewTypeForSectionHeader(sectionIdentifier: SI, expanded: Boolean): SectionViewType {
        return SectionViewType.Default
    }

    /**
     * Determines the [type][ItemViewType] of the view that should be used to the display the given [item].
     *
     * @param section: The [Section Identifier][SI] of the section that [item] is to be displayed in.
     * @return The [ItemViewType] of the view to be used for the item.
     *
     * @see RecyclerView.Adapter.getItemViewType
     */
    open fun viewTypeForItem(item: I, sectionIdentifier: SI?): ItemViewType {
        return ItemViewType.Default
    }

    /**@suppress*/
    final override fun getItemViewType(element: Element<SI, I>): ViewType {
        return when(element) {
            is Element.SectionHeader<SI, I> -> viewTypeForSectionHeader(element.sectionIdentifier, element.expanded)
            is Element.Item<SI, I> -> viewTypeForItem(element.item, element.sectionIdentifier)
        }
    }

    /**
     * Called when a new [ViewHolder][SDVH] of the given [viewType] is needed to act as a section header.
     *
     * @param parent: The ViewGroup into which the new View will be added after it is bound to an adapter position.
     *
     * @see RecyclerView.Adapter.onCreateViewHolder
     */
    abstract fun onCreateSectionHeaderViewHolder(parent: ViewGroup, viewType: SectionViewType): SDVH

    /**
     * Called when a new [ViewHolder][IDVH] of the given [viewType] is needed to represent an item in the dropdown.
     *
     * @param parent: The ViewGroup into which the new View will be added after it is bound to an adapter position.
     *
     * @see RecyclerView.Adapter.onCreateViewHolder
     */
    abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: ItemViewType): IDVH

    /**@suppress*/
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: ViewType): ViewHolder {
        return when (viewType) {
            is SectionViewType -> onCreateSectionHeaderViewHolder(parent, viewType)
            is ItemViewType -> onCreateItemViewHolder(parent, viewType)
            // This should not occur, as the impelmentation of getItemViewType is final and ensures that the output is either a SectionViewType or an ItemViewType
            else -> throw IllegalStateException()
        }
    }

    /**
     * Called to display the section header for the section identified by the given [sectionIdentifier].
     * This method should update the contents of [viewHolder.itemView] to reflect the section.
     *
     * @param expanded: Whether the section is currently expanded.
     *
     * @see RecyclerView.Adapter.onBindViewHolder
     */
    abstract fun onBindSectionHeaderViewHolder(viewHolder: SDVH, sectionIdentifier: SI, expanded: Boolean)

    /**
     * Called to display the specified [item]. Should update the contents of [holder.itemView] to reflect the [item].
     *
     * @param sectionIdentifier: The [Identifier][SI] of the section that the [item] is to be displayed in, or [null] if the item is to be displayed at the root.
     *
     * @see RecyclerView.Adapter.onBindViewHolder
     */
    abstract fun onBindItemViewHolder(viewHolder: IDVH, item: I, sectionIdentifier: SI?)

    @Suppress("UNCHECKED_CAST")
    /**@suppress*/
    final override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = getItem(position)
        // We can assume these casts are safe because getItemViewType and onCreateViewHolder ensure that the generated view holders are of the appropriate type.
        when (element) {
            is Element.SectionHeader -> {
                onBindSectionHeaderViewHolder(holder as SDVH, element.sectionIdentifier, element.expanded)
                holder.itemView.setOnClickListener {
                    toggleExpanded(element.sectionIdentifier)
                }
            }
            is Element.Item -> {
                onBindItemViewHolder(holder as IDVH, element.item, element.sectionIdentifier)
                holder.itemView.setOnClickListener {
                    selectedItem = element.item
                    dismissDropDown()
                }
            }
        }
    }

}