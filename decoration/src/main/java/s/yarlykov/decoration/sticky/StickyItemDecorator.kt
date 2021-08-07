package s.yarlykov.decoration.sticky

import android.graphics.*
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
 * Используются два "кэша":
 * 1. Neighbors: Map<Int, Int>
 * 2. Stickies: Map<Int, Bitmap>
 *
 * Кэш Stickies позволяет по id сохранять битмапы элементов HeaderView. Кэш Neighbors позволяет
 * по id элемента HeaderView найти его "предшественника", который располагается выше в layout.
 * Имея эти две структуры можно в любой момент найти битмапу верхнего соседа любого элемента
 * HeaderView. Зачем это нужно:
 */
class StickyItemDecorator : Decorator.RecyclerViewDecorator {
    private val neighbors = mutableMapOf<Int, Int?>()
    private val stickies = mutableMapOf<Int, Bitmap>()

    private val paintSimple = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintDebug = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = LightingColorFilter(Color.GREEN, 0)
    }

    private var paintCurrent = paintSimple
    fun highlightMode(mode: Boolean) {
        paintCurrent = if (mode) paintDebug else paintSimple
    }

    private var currentStickyId: Int = Int.MIN_VALUE
    private var prevTopHeaderViewY: Int = 0

    @ExperimentalStdlibApi
    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        // Найти все ViewHolder 'ы всех HeaderView на экране...
        val stickyViewHolders = recyclerView.children
            .map { recyclerView.findContainingViewHolder(it) }
            .filter { it is StickyHolder }

        // ... запомнить верхний,...
        val topHeaderHolder = stickyViewHolders.firstOrNull() ?: return
        val topHeaderViewId = (topHeaderHolder as StickyHolder).id
        val topHeaderViewY = topHeaderHolder.itemView.y

        // ... сделать их view видимыми, и ...
        stickyViewHolders.forEach { it?.itemView?.alpha = 1.0f }

        // ... сохранить битмапы, и ...
        saveStickies(stickyViewHolders)

        // ... сохранить соседство
        saveNeighbors(stickyViewHolders)

        when {
            /**
             * TODO Проверяем, что находимся в начальном состоянии.
             * - Это старт активити/фрагмента/родительского_элемента.
             * - RecyclerView только что создан и отрисован первый раз.
             */
            (currentStickyId == Int.MIN_VALUE) -> {
                currentStickyId = topHeaderViewId
                neighbors[topHeaderViewId] = Int.MIN_VALUE
            }
            /**
             * TODO Проверяем, что HeaderView вытолкнул Sticky выше верхней границы RecyclerView.
             * TODO То есть HeaderView.Y стал меньше 0, но был больше в предыдущем draw()
             * В этом случае два варианта:
             * 1. Первый HeaderView адаптера поднимается вверх. В этом случае других sticky ещё
             *    не было, currentStickyId == topStickyId и поэтому ничего делать не надо.
             * 2. Очередной StickyView поднялся вверх. Его id отличается от id текущей битмапы и в
             *    этом случае он должен заменить currentStickyId.
             */
            (topHeaderViewY < 0) -> {
                if (currentStickyId < topHeaderViewId) {
                    currentStickyId = topHeaderViewId
                }
            }
        }

        /**
         * На текущий момент ужё есть ясность относительно currentStickyId поэтому можно посчитать
         * метрики.
         */
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

        /**
         * TODO Далее проверяем, что HeaderView спустилась сверху и стала полностью видна.
         * В этой ситуации нужно найти в кэще битмапу верхнего соседа и назначить её на роль sticky.
         */
        if (topHeaderViewY >= 0 &&
            topHeaderViewY < bitmapHeight / 2
        ) {
            currentStickyId = neighbors[topHeaderViewId] ?: Int.MIN_VALUE
        }

        topHeaderHolder.itemView.alpha = if (topHeaderViewY < 0f) 0f else 1f

        stickies[currentStickyId]?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paintCurrent)
        }

        // Очистить стек если верхний Header (adapterPosition 0) полностью на экране
        if (topHeaderHolder.adapterPosition == 0
            && topHeaderViewY >= 0
            && prevTopHeaderViewY < 0
        ) {
            clearStickies(stickyViewHolders)
        }

        prevTopHeaderViewY = topHeaderViewY.toInt()
    }

    /**
     * Добавляем в кэш stickies битмапы видимых в данный момент HeaderView
     */
    private fun saveStickies(holders: Sequence<RecyclerView.ViewHolder?>) {
        holders.forEach { holder ->
            if (holder is StickyHolder) {
                if (!stickies.containsKey(holder.id)) {
                    stickies[holder.id] = holder.itemView.drawToBitmap()
                }
            }
        }
    }

    /**
     * Добавляем в кэш neighbor данные о соседстве видимых в данный момент HeaderView
     */
    private fun saveNeighbors(holders: Sequence<RecyclerView.ViewHolder?>) {
        holders.filterIsInstance<StickyHolder>().map { it.id }.zipWithNext().forEach {
            val (topViewId, bottomViewId) = it
            neighbors[bottomViewId] = topViewId
        }
    }

    /**
     * Делаем очистку кэша stickies, чтобы не хранить не нужные в данный момент битмапы.
     */
    private fun clearStickies(holders: Sequence<RecyclerView.ViewHolder?>) {
        val remain = holders.filterIsInstance<StickyHolder>().map { it.id }
        val iterator = stickies.entries.iterator()

        while (iterator.hasNext()) {
            if (iterator.next().key !in remain) {
                iterator.remove()
            }
        }
    }
}