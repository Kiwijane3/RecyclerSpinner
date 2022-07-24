package co.luoja.recyclerspinner

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent.ACTION_UP
import android.view.*
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import co.luoja.recyclerspinner.adapter.RecyclerSpinnerAdapter
import initiallyNull
import kotlin.properties.Delegates

/**
 * An [AppCompatSpinner] that displays a dynamic list of contents using a [RecyclerView].
 */
@SuppressLint("ClickableViewAccessibility")
class RecyclerSpinner: AppCompatSpinner {

    /**
     * A [StaticSpinnerAdapter] used to display the selected item view provided by the adapter.
     */
    private class SelectedItemViewProvider(val adapter: RecyclerSpinnerAdapter<*, *, *, *>, val view: ViewGroup, context: Context): StaticSpinnerAdapter(context) {

        override fun getView(parent: ViewGroup?): View {
            return adapter.getSelectedItemViewHolder(parent ?: view).itemView
        }

    }

    constructor(context: Context): super(context) {
        dropDownWidth = MATCH_PARENT
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes)

    /**
     * The [RecyclerSpinnerAdapter] that defines and displays the contents of this [RecyclerSpinner]
     */
    public var adapter: RecyclerSpinnerAdapter<*, *, *, *>? by Delegates.observable(null) { _, _, adapter ->
        adapter?.let {
            installSelectedItemView()
            adapter.setOnSelectedItemViewChangedListener(this::installSelectedItemView)

            popup?.adapter = adapter
            adapter.setOnDismissDropDownListener { popup?.dismiss() }
        }
    }

    /**
     * The popup used to display the dropdown.
     */
    private var popup: DropDownPopup? by initiallyNull()

    init {
        this.setOnTouchListener() { _, event ->
            if (event.action == ACTION_UP)
                showPopup()
            true
        }
    }

    override fun performClick(): Boolean {
        showPopup()
        return true
    }

    /**
     * Displays the dropdown.
     */
    private fun showPopup() {

        if (popup == null)
            popup = DropDownPopup(popupContext, popupBackground, adapter)

        popup!!.width = width
        popup!!.showAsDropDown(this, dropDownHorizontalOffset, dropDownVerticalOffset, Gravity.TOP)

    }

    /**
     * Displays the current selected item view in this spinner.
     */
    private fun installSelectedItemView() {

        val adapter = adapter ?: return

        super.setAdapter(SelectedItemViewProvider(adapter, this, context))

    }

}