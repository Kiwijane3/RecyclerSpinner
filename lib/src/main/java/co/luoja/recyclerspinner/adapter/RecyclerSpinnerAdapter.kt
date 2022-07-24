package co.luoja.recyclerspinner.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.*
import kotlin.properties.Delegates

/**
 * A Base class for classes that provides items to be displayed in [co.luoja.recyclerspinner.RecyclerSpinner]s. For most use cases, it is not necessary to inherit from this class,
 * as [FlatSpinnerAdapter] or [NestedSpinnerAdapter] are usually sufficient.
 *
 * @param I: The Type of the items that this [RecyclerSpinnerAdapter] is used to select.
 * @param E: The Type of the items that are used to define the elements displayed in the spinner dropdown's [RecyclerView].
 * @param DVH: The Type of [ViewHolder] used to manage the views used to display the items in the dropdown.
 * @param SVH: The Type of [ViewHolder] used to display the selected item in the spinner itself.
 *
 * @constructor Create a new [RecyclerSpinnerAdapter] which starts with the given [Item][I] selected and uses the given [DiffUtil.ItemCallback] to calculate differences when its contents change.
 *
 * @see ListAdapter
 */
abstract class RecyclerSpinnerAdapter<I: Any, E, DVH: ViewHolder, SVH: ViewHolder>(
    initiallySelected: I?,
    differ: DiffUtil.ItemCallback<E>
) : ListAdapter<E, DVH>(differ) {

    /**
     * An interface for objects representing types of views to be displayed in the selection dropdown.
     *
     * Implementors should have properly defined overrides for [Object.equals] and [Object.hashCode],
     * as these are used by [RecyclerSpinnerAdapter]. If you are using kotlin, a good way to do this is to use a data class.
     */
    interface ViewType {
        object Default: ViewType
    }

    /**
     * Creates a new [RecyclerSpinnerAdapter] which initially has no item selected.
     *
     * @param differ: The [DiffUtil.ItemCallback] used to calculate differences when the content changes.
     */
    constructor(differ: DiffUtil.ItemCallback<E>): this(null, differ)

    /**
     * The currently selected item, or none if no item is selected.
     */
    var selectedItem: I? by Delegates.observable(initiallySelected) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (getSelectedItemViewType(oldValue) != getSelectedItemViewType(newValue)) {
                selectedItemViewHolder?.let(selectedItemViewPool::putRecycledView)
                selectedItemViewHolder = null
                onSelectedItemViewChanged?.invoke()
            } else {
                selectedItemViewHolder?.let {
                    onBindSelectedItem(it, newValue)
                }
            }
            onItemSelected?.invoke(newValue)
        }
    }

    private val selectedItemViewPool = RecycledViewPool()

    private val viewTypeMap = HashMap<ViewType, Int>()

    private val viewTypeList = mutableListOf<ViewType>()

    private var onDismissDropDown: (() -> Unit)? = null

    // Should be called when the view that should be used to display the selected item changes
    private var onSelectedItemViewChanged: (() -> Unit)? = null

    private var onDisplayedItemsCountChanged: (() -> Unit)? = null

    private var onItemSelected: ((I?) -> Unit)? = null

    private var selectedItemViewHolder: SVH? = null

    /**
     * Updates the contents of the spinner dropdown.
     */
    protected fun dispatch() {
        val elements = generateElements()
        if ((elements?.count() ?: 0) != itemCount) onDisplayedItemsCountChanged?.invoke()
        submitList(elements)
    }

    /**
     * Should return the list of [Elements][E] to be displayed in the dropdown.
     */
    protected abstract fun generateElements(): List<E>?

    /**
     * Sets a callback that will be invoked when this [Adapter][RecyclerSpinnerAdapter] decides that the selection dropdown should be dismissed.
     */
    internal fun setOnDismissDropDownListener(handler: () -> Unit) {
        onDismissDropDown = handler
    }

    /**
     * Removes the callback that was added using [setOnDismissDropDownListener]
     */
    internal fun clearOnDismissDropDownListener() {
        onDismissDropDown = null
    }

    /**
     * To be called when the selection dropdown should be dismissed.
     */
    protected fun dismissDropDown() {
        onDismissDropDown?.invoke()
    }

    /**
     * Called when the number of displayed items changes.
     */
    private fun displayItemsCountChanged() {
        onDisplayedItemsCountChanged?.invoke()
    }

    /**@suppress*/
    final override fun getItemViewType(position: Int): Int {
        return getItemViewType(getItem(position)).id
    }

    /**
     * Return the view type of the view used to display the item in the dropdown, for the purposes of view recycling.
     *
     * The default implementation returns [ViewType.Default], fitting a case where a single type of view is used.
     * If multiple kinds of view should be used, override this function to return the appropriate [ViewType] for each [Item][E]
     *
     * @see Adapter.getItemViewType
     */
    open fun getItemViewType(element: E): ViewType {
        return ViewType.Default
    }

    /**
     * Return the view type of the view used to display the selected item, for the purposes of view recycling.
     *
     * The default implementation returns [ViewType.Default], fitting a case where a single type of view is used.
     * If multiple kinds of view should be used, override this function to return the appropriate [ViewType] for each [Item][E]
     *
     * @see Adapter.getItemViewType
     */
    open fun getSelectedItemViewType(element: I?): ViewType {
        return ViewType.Default
    }

    /**
     * Converts a [ViewType] to an [Int] which is constant for the associated instance of [RecyclerSpinnerAdapter].
     * Used for compatibility with [RecyclerView.Adapter].
     */
    private val ViewType.id: Int
        get() {
            return if (viewTypeMap.containsKey(this)) {
                viewTypeMap[this]!!
            } else {
                viewTypeList.add(this)
                viewTypeMap[this] = viewTypeList.lastIndex
                viewTypeList.lastIndex
            }
        }

    /**
     * Converts an id obtained from [ViewType.id] into the associated [ViewType].
     * Used for compatibility with [RecyclerView.Adapter].
     */
    private fun viewTypeWithId(id: Int): ViewType {
        return viewTypeList[id]
    }

    /**@suppress*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DVH {
        return onCreateViewHolder(parent, viewTypeWithId(viewType))
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: ViewType): DVH

    /**
     * Gets the [ViewHolder][SVH] that contains the view that should be embedded in the spinner to display the selected item
     */
    @Suppress("UNCHECKED_CAST")
    internal fun getSelectedItemViewHolder(parent: ViewGroup): SVH {
        val viewType = getSelectedItemViewType(selectedItem)
        return selectedItemViewHolder ?: (selectedItemViewPool.getRecycledView(viewType.id) as? SVH ?: onCreateSelectedItemViewHolder(parent, viewType)).apply {
            onBindSelectedItem(this, selectedItem)
            selectedItemViewHolder = this
        }
    }

    /**
     * Generates the [ViewHolder] that manages the view displaying the currently selected item in the spinner itself.
     */
    abstract fun onCreateSelectedItemViewHolder(parent: ViewGroup, viewType: ViewType): SVH

    /**
     * Called by the [RecyclerSpinner] to display the newly selected item. This method should update the [viewHolder]'s
     * contents to reflect the [selectedItem].
     *
     * @param selectedItem: The [Item][I] that has just been selected.
     * @param viewHolder: the [ViewHolder][SVH] managing the view that displays the currently selected [item][I], which should be updated.
     */
    abstract fun onBindSelectedItem(viewHolder: SVH, selectedItem: I?)


    /**
     * Adds a callback to be called when the number of displayed elements is changed.
     * This can be used to prevent the dropdown's size from being animated unnecessarily.
     */
    fun setOnDisplayedItemsCountChangedListener(handler: () -> Unit) {
        onDisplayedItemsCountChanged = handler
    }

    /**
     * Removes the callback that added via [setOnDisplayedItemsCountChangedListener]
     */
    fun clearOnDisplayedItemsCountChangedListener() {
        onDisplayedItemsCountChanged = null
    }

    /**
     * Adds a callback to be called when the view that should be used to display the currently selected item has changed.
     * This can be used to install the new view into the spinner.
     */
    fun setOnSelectedItemViewChangedListener(handler: () -> Unit) {
        onSelectedItemViewChanged = handler
    }

    /**
     * Removes the callback that was added via [setOnSelectedItemViewChangedListener]
     */
    fun clearOnSelectedItemViewChangedListener() {
        onSelectedItemViewChanged = null
    }

    /**
     * Adds a callback to be called when an [Item][I] is selected.
     */
    fun setOnItemSelectedListener(handler: (I?) -> Unit) {
        onItemSelected = handler
    }

    /**
     * Removes the callback that was added via [setOnItemSelectedListener].
     */
    fun clearOnItemSelectedListener() {
        onItemSelected = null
    }

}