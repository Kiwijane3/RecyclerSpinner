package co.luoja.recyclerspinner.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.*
import kotlin.properties.Delegates.observable

/**
 * A [RecyclerSpinnerAdapter] that presents non-hierarchical list of items.
 *
 * @param I: The Type of the items that this [RecyclerSpinnerAdapter] is used to select.
 * @param DVH: The Type of [ViewHolder] used to manage the views used to display the items in the dropdown.
 * @param SVH: The Type of [ViewHolder] used to display the selected item in the spinner itself.
 *
 * @param initiallySelected: The initially selected [Item][I], or null if no item is initially selected.
 * @param differ: The [DiffUtil.ItemCallback] used to calculate diffs when the displayed items change.
 */
abstract class FlatSpinnerAdapter<I: Any, DVH: ViewHolder, SVH: ViewHolder>(initiallySelected: I? = null, differ: DiffUtil.ItemCallback<I>): RecyclerSpinnerAdapter<I, I, DVH, SVH>(initiallySelected, differ) {

    /**
     * The [Item][I]s that can currently be selected from.
     */
    var items: List<I>? by observable(null) { _, _, _ ->
        dispatch()
    }

    /**
     * Creates a new [FlatSpinnerAdapter] using the given [differ], which initially has no item selected.
     */
    constructor(differ: DiffUtil.ItemCallback<I>): this(null, differ)

    /**@suppress*/
    final override fun generateElements(): List<I>? {
        return items
    }

    /**@suppress*/
    final override fun onBindViewHolder(holder: DVH, position: Int) {
        val item = getItem(position)
        onBindViewHolder(holder, item)
        holder.itemView.setOnClickListener {
            selectedItem = item
            dismissDropDown()
        }
    }

    /**
     * Called to display the specified [item]. Should update the contents of [holder.itemView] to reflect the [item].
     */
    abstract fun onBindViewHolder(holder: DVH, item: I)

    override fun submitList(list: MutableList<I>?) {
        items = list
    }

}