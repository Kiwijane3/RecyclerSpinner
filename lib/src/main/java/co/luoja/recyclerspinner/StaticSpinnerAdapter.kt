package co.luoja.recyclerspinner

import android.content.Context
import android.database.DataSetObserver
import android.view.*
import android.widget.SpinnerAdapter

/**
 * A [SpinnerAdapter] used to display a single [View].
 */
internal abstract class StaticSpinnerAdapter(val context: Context): SpinnerAdapter {

    final override fun getCount(): Int {
        return 1
    }

    final override fun getItem(position: Int): Any {
        return object: Any() {}
    }

    final override fun getItemId(p0: Int): Long {
        return 0
    }

    final override fun getItemViewType(p0: Int): Int {
        return 0
    }

    final override fun getViewTypeCount(): Int {
        return 1
    }

    final override fun hasStableIds(): Boolean {
        return true
    }

    final override fun isEmpty(): Boolean {
        return false
    }

    final override fun registerDataSetObserver(p0: DataSetObserver?) {}

    final override fun unregisterDataSetObserver(p0: DataSetObserver?) {}

    final override fun getDropDownView(p0: Int, p1: View?, p2: ViewGroup?): View {
        return View(context)
    }

    final override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {
        return getView(parent)
    }

    /**
     * Generates the single [View] to be displayed in the spinner.
     */
    abstract fun getView(parent: ViewGroup?): View

}