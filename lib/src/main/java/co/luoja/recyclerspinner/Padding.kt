package co.luoja.recyclerspinner

import android.view.View

internal data class PaddingSpec private constructor(val start: Int, val top: Int, val end: Int, val bottom: Int) {

    companion object {

        fun create(start: Int, top: Int, end: Int, bottom: Int): PaddingSpec? {
            return if (start == 0 && top == 0 && end == 0 && bottom == 0)
                null
            else
                PaddingSpec(start, top, end, bottom)
        }

    }

}

internal fun View.setPaddingRelative(spec: PaddingSpec?) {
    this.setPaddingRelative(spec?.start ?: 0, spec?.top ?: 0, spec?.end ?: 0, spec?.bottom ?: 0)
}