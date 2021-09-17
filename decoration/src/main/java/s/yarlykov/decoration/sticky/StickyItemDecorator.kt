package s.yarlykov.decoration.sticky

import android.graphics.*
import android.view.View
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.decoration.Decorator

/**
 * Список отображает три типа элементов;
 * - HeaderView: View из которых формируются Sticky.
 * - DataView: View которые отображают данные модели.
 * - Sticky: Bitmap - битмапа элемента Header.
 *
 * Используется "кэш" Stickies: Map<Int, Bitmap>, который позволяет по id сохранять битмапы
 * элементов HeaderView.
 */
class StickyItemDecorator : Decorator.RecyclerViewDecorator {
    private val stickies = mutableMapOf<Int, Bitmap>()

    private val paintSimple = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintDebug = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = LightingColorFilter(Color.GREEN, 0)
    }

    private var paintCurrent = paintSimple
    fun highlightMode(mode: Boolean) {
        paintCurrent = if (mode) paintDebug else paintSimple
    }

    private var currentStickyId: Int = StickyHolder.NO_ID
    private var prevTopHeaderViewY: Int = 0

    private val visibleHoldersIds = mutableListOf<Int>()

    // Определение видимости элемента списка внутри RecyclerView
    private val viewRect = Rect()
    private val parentRect = Rect()
    private val View.isVisible: Boolean
        get() {
            getHitRect(viewRect)
            (parent as View).getLocalVisibleRect(parentRect)
            return viewRect.intersect(parentRect)
        }

    @ExperimentalStdlibApi
    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        // Составить список ID видимых холдеров (список из их groupId)
        populateVisibleHoldersList(recyclerView)

        // groupId верхнего видимого элемента будет идентификатором текущего Sticky
        currentStickyId = visibleHoldersIds.firstOrNull() ?: return

        // Найти все ViewHolder 'ы всех HeaderView на экране...
        val headerViewHolders = recyclerView.children
            .map { recyclerView.findContainingViewHolder(it) }
            .filter { it is StickyHolder.Header }

        // ... сделать их view видимыми, ...
        headerViewHolders.forEach { it?.itemView?.alpha = 1.0f }

        // ... сохранить битмапы, и...
        saveStickies(headerViewHolders)

        // ... запомнить текущий верхний Header (если такой есть на экране).
        val topHeaderHolder = headerViewHolders.firstOrNull() ?: run {

            /**
             * Может быть ситуация когда есть большой список из элементов только одной
             * группы. То есть имеем единственный Header. И когда при прокрутке пальцем вверх
             * этот Header уходит за экран, то нам нужно просто отрисовать его битмапу. Поэтому
             * вызываем отрисовку и на выход из draw()
             */
            drawSticky(canvas, 0f)
            return
        }
        val topHeaderViewY = topHeaderHolder.itemView.y

        val bitmapHeight = stickies[currentStickyId]?.height ?: 0

        /**
         * Когда HeaderView.Y попадает в диапазон '0 < y < bitmap.height', то он начинает тянуть
         * вниз за собой или толкать вверх битмапу. Это достигается изменением top-позиции битмапы
         * в координатах канвы. Например, надвигающийся снизу sticky касается битмапы снизу и
         * начинает "выталкивать" её вверх за экран.
         *
         * Если HeaderView.Y в другом диапазоне, то ничего не происходит и битмапа имеет 'top == 0'
         * и висит вверху экрана.
         */
        val bitmapTopOffset =
            if (0 <= topHeaderViewY && topHeaderViewY <= bitmapHeight) {
                topHeaderViewY - bitmapHeight
            } else {
                0f
            }

        topHeaderHolder.itemView.alpha = if (topHeaderViewY < 0f) 0f else 1f

        drawSticky(canvas, bitmapTopOffset)

        // Очистить стек если верхний Header (adapterPosition 0) полностью на экране
        if (topHeaderHolder.adapterPosition == 0
            && topHeaderViewY >= 0
            && prevTopHeaderViewY < 0
        ) {
            clearStickies(headerViewHolders)
        }

        prevTopHeaderViewY = topHeaderViewY.toInt()
    }

    /**
     * Отрисовка битмапы.
     */
    private fun drawSticky(canvas: Canvas, bitmapTopOffset: Float) {
        stickies[currentStickyId]?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paintCurrent)
        }
    }

    /**
     * Заполнить список visibleHoldersIds значениями groupId видимых холдеров.
     */
    private fun populateVisibleHoldersList(recyclerView: RecyclerView) {
        visibleHoldersIds.clear()
        recyclerView.children.forEach { child ->
            if (child.isVisible) {
                (recyclerView.findContainingViewHolder(child) as? StickyHolder)
                    ?.let { stickyHolder ->
                        visibleHoldersIds.add(stickyHolder.groupId)
                    }
            }
        }
    }

    /**
     * Добавляем в кэш stickies битмапы видимых в данный момент HeaderView
     */
    private fun saveStickies(holders: Sequence<RecyclerView.ViewHolder?>) {
        holders.forEach { holder ->
            if (holder is StickyHolder) {
                if (!stickies.containsKey(holder.groupId)) {
                    stickies[holder.groupId] = holder.itemView.drawToBitmap()
                }
            }
        }
    }

    /**
     * Делаем очистку кэша stickies, чтобы не хранить не нужные в данный момент битмапы.
     */
    private fun clearStickies(holders: Sequence<RecyclerView.ViewHolder?>) {
        val remain = holders.filterIsInstance<StickyHolder>().map { it.groupId }
        val iterator = stickies.entries.iterator()

        while (iterator.hasNext()) {
            if (iterator.next().key !in remain) {
                iterator.remove()
            }
        }
    }
}