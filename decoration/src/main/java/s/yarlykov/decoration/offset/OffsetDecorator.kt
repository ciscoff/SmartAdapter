package s.yarlykov.decoration.offset

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.decoration.Decorator

/**
 * Декоратор устанавливает offsets по сторонам View
 */
class OffsetDecorator(
    private val left: Int = 0,
    private val top: Int = 0,
    private val right: Int = 0,
    private val bottom: Int = 0
) : Decorator.Offset {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(left, top, right, bottom)
    }
}