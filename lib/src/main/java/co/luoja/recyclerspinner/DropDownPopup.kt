package co.luoja.recyclerspinner

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.*
import android.widget.FrameLayout.*
import android.widget.*
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.*
import co.luoja.recyclerspinner.adapter.*
import co.luoja.recyclerspinner.R
import kotlin.properties.Delegates.observable

/**
 * The [PopupWindow] used to display a [RecyclerSpinner]'s dropdown selection window.
 */
internal class DropDownPopup(context: Context, background: Drawable?, initialPadding: PaddingSpec?, initialSpacing: Int?, adapter: RecyclerSpinnerAdapter<*, *, *, *>?) : PopupWindow(context) {

    /**
     * The recycler view used to display the contents of the dropdown.
     */
    private lateinit var recyclerView: RecyclerView

    /**
     * The view that provides the background of the window. We use a custom view so that we can move easily animate it,
     * as animating the size of a popup window directly is difficult.
     */
    private var backgroundView: LinearLayout

    /**
     * The adapter used that defines the contents of the associated spinner.
     */
    var adapter: RecyclerSpinnerAdapter<*, *, *, *>? by observable(adapter) { _, _, adapter ->
        recyclerView.adapter = adapter
        adapter?.setOnDisplayedItemsCountChangedListener(this::onAdapterDisplayedItemCountChanged)
    }

    /**
     * The padding around the popup's contents. Should be applied to the recyclerview so that the background does not shrink.
     */
    var padding: PaddingSpec? by observable(initialPadding) { _, _, value ->
        recyclerView.setPaddingRelative(value)
    }

    /**
     * The spacing between each element in the popup's content recyclerview.
     */
    var spacing: Int? by observable(initialSpacing) { _, oldValue, newValue ->
        if (oldValue != null) recyclerView.removeItemDecorationAt(0)
        if (newValue != null) recyclerView.addItemDecoration(VerticalInterItemMarginDecorator(newValue), 0)
    }

    /**
     * Indicates whether an animation should be performed when the recycler view changes height.
     *
     * To avoid stutter and graphical glitches, changes in the height of the content should only occur if the number of displayed elements has changed.
     * This should be set to true when the number of items changes, and then set to true when an animation for that change is initiated.
     */
    private var shouldAnimateForContentHeightChange = false

    init {

        adapter?.setOnDisplayedItemsCountChangedListener(this::onAdapterDisplayedItemCountChanged)

        val view = View.inflate(context, R.layout.popup_contents, null)

        contentView = view

        recyclerView = view.findViewById(R.id.content)

        recyclerView.setPaddingRelative(initialPadding)
        initialSpacing?.let {
            recyclerView.addItemDecoration(VerticalInterItemMarginDecorator(initialSpacing), 0)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        width = WRAP_CONTENT
        height = WRAP_CONTENT
        isFocusable = true

        backgroundView = view.findViewById(R.id.popup_background)

        background?.let {
            backgroundView.background = background
        }

        recyclerView.addOnLayoutChangeListener(this::onContentLayoutChanged)

        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }

    /**
     * Called when the recycler view's layout changes. Handles displaying an animation when the number of displayed elements changes.
     */
    private fun onContentLayoutChanged(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {

        val newHeight = bottom - top
        val oldHeight = oldBottom - oldTop

        // We only animate if the size has changed in response to a change in content.
        if (newHeight == oldHeight || !shouldAnimateForContentHeightChange) return

        shouldAnimateForContentHeightChange = false

        backgroundView.gravity =
                if (isAboveAnchor)
                    Gravity.BOTTOM
                else
                    Gravity.TOP

        // It is difficult to properly animate the size of the spinner or root view,
        // so a custom background is animated instead,
        // and the contents of the recycler view are clipped to the height of the background
        // to prevent elements from appearing outside of the apparent boundary of the window.
        ValueAnimator.ofInt(oldHeight, newHeight).apply {
            duration = view.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            addUpdateListener {
                val visibleHeight = animatedValue as Int
                backgroundView.layoutParams = LayoutParams(MATCH_PARENT, visibleHeight)
                recyclerView.clipBounds =
                    if (isAboveAnchor)
                        Rect(0, bottom - visibleHeight, right, bottom)
                    else
                        Rect(0, 0, right, visibleHeight)
            }
            doOnEnd {
                backgroundView.layoutParams = LayoutParams(MATCH_PARENT, recyclerView.height)
                recyclerView.clipBounds = null
            }
        }.start()

    }

    /**
     * To be called when the number of displayed elements changes so that we can perform an animation.
     */
    private fun onAdapterDisplayedItemCountChanged() {
        shouldAnimateForContentHeightChange = true
    }

}