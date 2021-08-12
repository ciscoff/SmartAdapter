package s.yarlykov.decoration.sticky

import android.graphics.*
import android.util.Log
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
 * HeaderView.
 *
 * TODO Важно !!!
 *
 * Основной проблемой данного декоратора оказалось поддержание актуальных связей между
 * sticky-элементами, а именно каждый последующий должен всегда знать правильный ID своего
 * предшественника. Эта проблема частично была решена с помощью структур neighbors/stickies.
 * Изначально в качестве ID StickyHolder 'а я использовал adapterPosition. Но с таким подходом
 * быстро обнаружился глюк неправильной отрисовки липучек при удалении элементов списка свайпом.
 * Структуры neighbors/stickies дополнялись новыми данными, но не удаляли данные устаревшие.
 * В итоге в качестве ID я решил использовать то, что уникально идентифицирует данные,
 * отображаемые ViewHolder 'ом, а не ID самого холдера. Для этого подходит значение
 * System.identityHashCode(data) от данных в BindableViewHolder. Поэтому ViewHolder для липучки
 * должен во-первых реализовать интерфейс StickyHolder, а во-вторых при биндинге данных
 * вычислить их System.identityHashCode и присвоить результат в StickyHolder::id. Вычислять ID
 * при биндинге данных безопасно потому что в этот момент декоратор ешё не приступил к работе.
 *
 * См. реализацию в [s.yarlykov.example.prod.rv.TimeStampController.Holder]
 *
 * NOTE: Сначала я думал при каждом обновлении модели генерить новую структуру neighbors внешним
 * кодом и передавать в декоратор. Но это плохо, потому что декоратор теряет автономность.
 * Он зависит от кого-то. В итоге стал искать решение, которое всю работу оставит декторатору с
 * минимальной помощью извне. Все что от нас требуется - это уникальный ID для данных липучки.
 *
 * На TODO: Подумать как чистить кэши.
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

    private var currentStickyId: Int = StickyHolder.NO_ID
    private var prevTopHeaderViewY: Int = 0

    @ExperimentalStdlibApi
    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        val topVisibleHolder = findTopHolder(recyclerView)

        // Найти все ViewHolder 'ы всех HeaderView на экране...
        val stickyViewHolders = recyclerView.children
            .map { recyclerView.findContainingViewHolder(it) }
            .filter { it is StickyHolder.Header }

        // ... запомнить верхний Header,...
        val topHeaderHolder = stickyViewHolders.firstOrNull() ?: return
        val topHeaderId = (topHeaderHolder as StickyHolder).groupId
        val topHeaderViewY = topHeaderHolder.itemView.y

        // ... сделать их view видимыми, и ...
        stickyViewHolders.forEach { it?.itemView?.alpha = 1.0f }

        // ... сохранить битмапы, и ...
        saveStickies(stickyViewHolders)

        // ... сохранить соседство
        saveNeighbors(stickyViewHolders)

        /**
         * Важны ЧЕТЫРЕ положения ВЕРХНЕГО StickyHolder 'a
         * 1. Начальное - данные только загрузились, topHeaderHolder - первый элемент списка и
         *    он на самом верху.
         * 2. topHeaderHolder начал выходить выше верхней границы RecyclerView. Это значит, что
         *    он "вытолкал" какую-то битмапу (свою или чужую)
         * 3. topHeaderViewY в диапазоне от 0 до bitmap.height. Это значит, что topHeaderView
         *    толкает/тянет битмапу
         * 4. topHeaderViewY больше bitmap.height. Это значит что с липучкой ничего не происходит.
         *    Она просто висит наверху.
         */

        when {
            /**
             * TODO Проверяем положение 1: то что находимся в начальном состоянии.
             * - Это старт активити/фрагмента/родительского_элемента.
             * - RecyclerView только что создан и отрисован первый раз.
             */
            (currentStickyId == StickyHolder.NO_ID) -> {
                currentStickyId = topHeaderId
                // У первой липучки нет верхнего соседа
                neighbors[currentStickyId] = StickyHolder.NO_ID
            }
            /**
             * TODO Проверяем положение 2: то что HeaderView вытолкнул Sticky выше верхней границы
             * TODO RecyclerView. HeaderView.Y стал меньше 0, но был больше в предыдущем draw()
             * В этом случае два варианта:
             * 1. Первый HeaderView адаптера поднимается вверх. В этом случае других sticky ещё
             *    не было, currentStickyId == topStickyId и поэтому ничего делать не надо.
             * 2. Очередной StickyView поднялся вверх. Его id отличается от id текущей битмапы и в
             *    этом случае он должен заменить currentStickyId.
             */
            (topHeaderViewY < 0) -> {
                if (currentStickyId != topHeaderId) {
                    currentStickyId = topHeaderId
                }
            }
        }

//        Log.d("STICKY", "${stickyViewHolders.toList().map { it?.adapterPosition }}")
//        Log.d("STICKY", "neighbors: $neighbors, bitmaps: ${stickies.keys}")

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
         * TODO Проверяем положение 3: topHeaderViewY внутри [0 .. bitmap.height].
         * В этой ситуации нужно найти в кэше битмапу верхнего соседа и назначить её на роль sticky.
         */
        if (topHeaderViewY >= 0 && topHeaderViewY < bitmapHeight / 2) {
            currentStickyId = neighbors[topHeaderId] ?: StickyHolder.NO_ID
        }

        // TODO Проверяем положение 4: Мля! Вот эта строка спасает от глюков при резком свайпе !!!
        if (topHeaderViewY > bitmapHeight) {

            /**
             * TODO Очень важный момент ! Проверяем, что верхний сосед сменился !
             * TODO Это может произойти в следующей ситуации:
             * Допустип, что в качестве липучки висит битмапа '10 августа'. Чуть ниже Header
             * '07 августа' с парой элементов данных, и затем ещё один Header '04 августа'. Хидер
             * от 4 августа считает своим верхним соседом хидера от 7 августа. Теперь мы удаляем
             * оба элемента данных у 7 августа. И вуаля ! Липучка '10 августа' неожиданно меняется
             * на '07 августа' хотя должна остаться прежняя, а такого элемента как '7 августа'
             * вообще больше не существует !!! Дело в том, что хидер '04 августа' оказался верхним.
             * В структуре neighbors его соседом записан id '7 августа', однако такого соседа
             * больше нет, но есть новый - '10 августа', чья липучка сейчас записана в переменной
             * currentStickyId, сохранившей значение от прошлого draw(). И мы можем заменить
             * соседа хидера '04 августа' с 07 на 10 августа. Что мы и делаем !!!
             *
             * TODO WTF снова трабла при резком свайпе !
             */
//            if (currentStickyId != neighbors[topHeaderId]) {
//                neighbors[topHeaderId] = currentStickyId
//            }

            currentStickyId = neighbors[topHeaderId] ?: StickyHolder.NO_ID
        }

        topHeaderHolder.itemView.alpha = if (topHeaderViewY < 0f) 0f else 1f

        stickies[currentStickyId]?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paintCurrent)
        }

        Log.d("STICKY", "currentStickyId=$currentStickyId")

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
                if (!stickies.containsKey(holder.groupId)) {
                    stickies[holder.groupId] = holder.itemView.drawToBitmap()
                }
            }
        }
    }

    /**
     * Добавляем в кэш neighbor данные о соседстве видимых в данный момент HeaderView
     */
    private fun saveNeighbors(holders: Sequence<RecyclerView.ViewHolder?>) {
        holders.filterIsInstance<StickyHolder>().map { it.groupId }.zipWithNext().forEach {
            val (topViewId, bottomViewId) = it
            neighbors[bottomViewId] = topViewId
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

    /**
     * TODO Нужно продумать вариант если используется элемент, который вообще не StickyHolder
     */
    private fun findTopHolder(recyclerView: RecyclerView): RecyclerView.ViewHolder? {
        return recyclerView.children.firstOrNull { view ->
            view.top <= recyclerView.paddingTop && view.bottom > recyclerView.paddingTop
        }?.let(recyclerView::findContainingViewHolder)
    }
}