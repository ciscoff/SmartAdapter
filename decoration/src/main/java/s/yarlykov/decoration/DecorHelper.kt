package s.yarlykov.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.decoration.Decorator.EACH_VIEW

/**
 * Класс аккумулирует все декораторы. Декораторы для viewHolder'ов он раскладывает в отдельные
 * HashMap'ы для быстрого вызова нужного декоратора по viewType.
 *
 * @param underlaysViewType - связки 'viewType - underlayDecorator' для отрисовки под itemView
 * @param overlaysViewType - связки 'viewType - overlayDecorator' для отрисовки над itemView
 *
 * @param underlaysRecycler - список декораторов для отрисовки на поверхности RecyclerView
 * @param overlaysRecycler - список декораторов для отрисовки по поверхности RecyclerView
 *
 * @param offsetsViewType - связки 'viewType - offsets'
 */
class DecorHelper(
    private val underlaysViewType: List<DecorBinder<Decorator.ViewHolderDecorator>>,
    private val underlaysRecycler: List<Decorator.RecyclerViewDecorator>,
    private val overlaysViewType: List<DecorBinder<Decorator.ViewHolderDecorator>>,
    private val overlaysRecycler: List<Decorator.RecyclerViewDecorator>,
    private val offsetsViewType: List<DecorBinder<Decorator.Offset>>
) {
    /**
     * Сконвертировать список в hashMap.
     * На один К только один V, где К = viewType, V = элемент исходного списка.
     *
     * Все элементы с одинаковым viewType имеют одинаковые offsets.
     */
    private val associatedOffsetsViewType = offsetsViewType.associateBy { it.viewType }

    /**
     * Сконвертировать список в hashMap.
     * На один K может получиться несколько V (где К = viewType, V = элемент списка).
     */
    private val groupedUnderlaysViewType = underlaysViewType.groupBy { it.viewType }

    /**
     * Сконвертировать список в hashMap.
     * На один K может получиться несколько V (где К = viewType, V = элемент списка).
     */
    private val groupedOverlaysViewType = overlaysViewType.groupBy { it.viewType }

    /**
     * Порядок рисования:
     * 1. сначала по поверхности RecyclerView
     * 2 и 3. затем под местами размещения элементов (то есть поверх предыдущего рисования п.1)
     */
    fun drawUnderlay(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
        underlaysRecycler.drawRecyclerViewDecors(canvas, recyclerView, state)
        groupedUnderlaysViewType.drawNotAttachedDecors(canvas, recyclerView, state)
        groupedUnderlaysViewType.drawAttachedDecors(canvas, recyclerView, state)
    }

    /**
     * Порядок рисования:
     * 1 и 2. сначала поверх элементов
     * 3. затем по всей поверхности RecyclerView.
     */
    fun drawOverlay(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {
        groupedOverlaysViewType.drawAttachedDecors(canvas, recyclerView, state)
        groupedOverlaysViewType.drawNotAttachedDecors(canvas, recyclerView, state)
        overlaysRecycler.drawRecyclerViewDecors(canvas, recyclerView, state)
    }

    fun getItemOffsets(
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        drawOffset(EACH_VIEW, outRect, view, recyclerView, state)
        recyclerView.findContainingViewHolder(view)?.itemViewType?.let { itemViewType ->
            drawOffset(itemViewType, outRect, view, recyclerView, state)
        }
    }

    private fun Map<Int, List<DecorBinder<Decorator.ViewHolderDecorator>>>.drawAttachedDecors(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        recyclerView.children.forEach { view ->
            val viewType = recyclerView.getChildViewHolder(view).itemViewType
            this[viewType]?.forEach {
                it.decorator.draw(canvas, view, recyclerView, state)
            }
        }
    }

    private fun Map<Int, List<DecorBinder<Decorator.ViewHolderDecorator>>>.drawNotAttachedDecors(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        recyclerView.children.forEach { view ->
            this[EACH_VIEW]
                ?.forEach { it.decorator.draw(canvas, view, recyclerView, state) }
        }
    }

    private fun List<Decorator.RecyclerViewDecorator>.drawRecyclerViewDecors(
        canvas: Canvas,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        forEach { it.draw(canvas, recyclerView, state) }
    }

    private fun drawOffset(
        viewType: Int,
        outRect: Rect,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        associatedOffsetsViewType[viewType]
            ?.decorator
            ?.getItemOffsets(outRect, view, recyclerView, state)
    }
}