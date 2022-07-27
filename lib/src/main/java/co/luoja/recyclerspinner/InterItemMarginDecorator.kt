package co.luoja.recyclerspinner

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class VerticalInterItemMarginDecorator(val marginSize: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildAdapterPosition(view) == 0)
            return

        outRect.top = marginSize
    }

}