package co.luoja.recyclerspinner.demo

import android.view.*
import androidx.recyclerview.widget.RecyclerView.*
import co.luoja.recyclerspinner.adapter.*
import com.luoja.recyclerspinner.demo.databinding.*

class DemoNestedAdapter: NestedSpinnerAdapter<String, String, DemoNestedAdapter.SectionHeaderHolder, DemoNestedAdapter.ItemHolder, DemoNestedAdapter.SelectedItemViewHolder>(null, Differ()) {

    data class SectionHeaderHolder(val binding: SectionHeaderBinding): ViewHolder(binding.root)

    abstract class SelectedItemViewHolder(root: View): ViewHolder(root)

    data class ItemHolder(val binding: ItemBinding): SelectedItemViewHolder(binding.root)

    data class ItemPlaceholderHolder(val binding: ItemPlaceholderBinding): SelectedItemViewHolder(binding.root)

    object ItemPlaceholderViewType: ViewType

    class Differ: DifferCallback<String, String>() {
        override fun doSectionIdsShareIdentity(a: String, b: String): Boolean {
            return a == b
        }

        override fun doSectionIdsShareContents(a: String, b: String): Boolean {
            return a == b
        }

        override fun doItemsShareIdentity(a: String, b: String): Boolean {
            return a == b
        }

        override fun doItemsShareContents(a: String, b: String): Boolean {
            return a == b
        }

    }

    override fun onCreateSectionHeaderViewHolder(
        parent: ViewGroup,
        viewType: SectionViewType
    ): SectionHeaderHolder {
        val binding = SectionHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SectionHeaderHolder(binding)
    }

    override fun onCreateItemViewHolder(
        parent: ViewGroup,
        viewType: ItemViewType
    ): ItemHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindSectionHeaderViewHolder(
        viewHolder: SectionHeaderHolder,
        sectionIdentifier: String,
        expanded: Boolean
    ) {
        (viewHolder as? SectionHeaderHolder)?.binding?.sectionLabel?.text = sectionIdentifier
    }

    override fun onBindItemViewHolder(
        viewHolder: ItemHolder,
        item: String,
        sectionIdentifier: String?
    ) {
        (viewHolder as? ItemHolder)?.binding?.itemLabel?.text = item
    }

    override fun getSelectedItemViewType(element: String?): ViewType {
        return if (element == null) ItemPlaceholderViewType else ViewType.Default
    }

    override fun onCreateSelectedItemViewHolder(
        parent: ViewGroup,
        viewType: ViewType
    ): SelectedItemViewHolder {
        return when (viewType) {
            is ItemPlaceholderViewType -> {
                val binding = ItemPlaceholderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemPlaceholderHolder(binding)
            }
            else -> {
                val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ItemHolder(binding)
            }
        }
    }

    override fun onBindSelectedItem(viewHolder: SelectedItemViewHolder, selectedItem: String?) {
        (viewHolder as? ItemHolder)?.binding?.itemLabel?.text = selectedItem
    }

}