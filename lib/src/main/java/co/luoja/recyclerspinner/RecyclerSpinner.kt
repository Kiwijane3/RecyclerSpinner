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
import java.util.jar.Attributes
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

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

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        initAttributes(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr) {
        initAttributes(attributeSet)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes) {
        initAttributes(attributeSet)
    }

    fun initAttributes(set: AttributeSet?) {

        val attributes = set?.let {
            context.obtainStyledAttributes(set, R.styleable.RecyclerSpinner)
        } ?: return

        val defaultPadding = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_padding, 0)

        popupPadding = PaddingSpec.create(
            start = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_padding_start, defaultPadding),
            top = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_padding_top, defaultPadding),
            end = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_padding_end, defaultPadding),
            bottom = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_padding_bottom, defaultPadding)
        )

        if (attributes.hasValue(R.styleable.RecyclerSpinner_popup_spacing))
            popupSpacing = attributes.getDimensionPixelOffset(R.styleable.RecyclerSpinner_popup_spacing, 0)

    }

    /**
     * The [RecyclerSpinnerAdapter] that defines and displays the contents of this [RecyclerSpinner]
     */
    var adapter: RecyclerSpinnerAdapter<*, *, *, *>? by observable(null) { _, _, adapter ->
        adapter?.let {
            installSelectedItemView()
            adapter.setOnSelectedItemViewChangedListener(this::installSelectedItemView)

            popup?.adapter = adapter
            adapter.setOnDismissDropDownListener { popup?.dismiss() }
        }
    }

    /**
     * The padding around the contents in the popup.
     */
    internal var popupPadding: PaddingSpec? by observable(null) { _, _, value ->
        popup?.padding = value
    }

    /**
     * The spacing between the selectable items in the popup, in raw pixels.
     *
     * Can be set using a dimension value, such as `24dp` by using the xml attribute []app:popup_spacing].
     */
    var popupSpacing: Int? by observable(null) { _, _, value ->
        popup?.spacing = value
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
            popup = DropDownPopup(
                popupContext,
                popupBackground,
                popupPadding,
                popupSpacing,
                adapter
            )

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

    /**
     * Sets the relative padding around the selectable content in the popup. The values are raw pixels.
     *
     * These values can be set in using dimension values by using the xml attribute `app:popup_padding`, which sets all the values,
     * or the attributes `app:popup_padding_start`, `app:popup_padding_top`, `app:popup_padding_end`, and `app:popup_padding_bottom`,
     * which set each value individually. These can be used together; In this case, the value provided for the specific axis will be preferred,
     * with the value provided by `popup_padding` being used as the default if a specific value is not set.
     *
     * @see [View.setPaddingRelative]
     */
    fun setPopupPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        popupPadding = PaddingSpec.create(
            start,
            top,
            end,
            bottom
        )
    }

}