package s.yarlykov.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MainDecorator(private val helper: DecorHelper) : RecyclerView.ItemDecoration() {

    /**
     * Рисование уже после вызова onDraw() на view элемента списка
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        helper.drawOverlay(canvas, parent, state)
    }

    /**
     * Рисование ещё до вызова onDraw() на view элемента списка
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)
        helper.drawUnderlay(canvas, parent, state)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        helper.getItemOffsets(outRect, view, parent, state)
    }
}