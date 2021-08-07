package s.yarlykov.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Основной класс, который собирает все в кучу и настраивает.
 */
object Decorator {

    /**
     * Если какой-то декор нужно вывести на все views, то устанавливаем viewType == EACH_VIEW
     */
    const val EACH_VIEW = -1
    const val UNDEFINE_VIEW_HOLDER: Int = -1

    class Builder {

        /**
         * Хранилище декораторов. Scope задает область их применения:
         * - Underlay - рисуем до вызова itemView::onDraw.
         * - Overlay - рисуем после вызова itemView::onDraw.
         * - ViewHolder - рисуем в области, занимаемой view элемента списка.
         * - RecyclerView - рисуем в любом месте RecyclerView.
         * - Offsets - просто определить отступы.
         *
         * В скоупе ViewHolder хранятся элементы DecorBinder, которые связывают viewType
         * и его декоратор.
         */
        private var scopeViewHolderUnderlay: MutableList<DecorBinder<ViewHolderDecorator>> =
            mutableListOf()

        private var scopeViewHolderOverlay: MutableList<DecorBinder<ViewHolderDecorator>> =
            mutableListOf()

        private var scopeRecyclerViewUnderlay: MutableList<RecyclerViewDecorator> =
            mutableListOf()

        private var scopeRecyclerViewOverlay: MutableList<RecyclerViewDecorator> =
            mutableListOf()

        private var scopeOffsets: MutableList<DecorBinder<Offset>> =
            mutableListOf()

        /**
         * underlay рисование для определенных viewType
         */
        fun underlay(pair: Pair<Int, ViewHolderDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply { scopeViewHolderUnderlay.add(DecorBinder(viewType, decorator)) }
        }

        /**
         * underlay рисование на ВСЕХ (EACH_VIEW) элементах
         */
        fun underlay(decor: ViewHolderDecorator): Builder {
            return apply { scopeViewHolderUnderlay.add(DecorBinder(EACH_VIEW, decor)) }
        }

        /**
         * underlay рисование на RecyclerView
         */
        fun underlay(decor: RecyclerViewDecorator): Builder {
            return apply { scopeRecyclerViewUnderlay.add(decor) }
        }

        /**
         * overlay рисование на RecyclerView
         */
        fun overlay(decor: RecyclerViewDecorator): Builder {
            return apply { scopeRecyclerViewOverlay.add(decor) }
        }

        /**
         * overlay рисование для определенных viewType
         */
        fun overlay(pair: Pair<Int, ViewHolderDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply {
                scopeViewHolderOverlay.add(DecorBinder(viewType, decorator))
            }
        }

        /**
         * overlay рисование на ВСЕХ (EACH_VIEW) элементах
         */
        fun overlay(decor: ViewHolderDecorator): Builder {
            return apply {
                scopeViewHolderOverlay.add(DecorBinder(EACH_VIEW, decor))
            }
        }

        /**
         * Добавить offset-декоратор для определенного viewType.
         */
        fun offset(pair: Pair<Int, Offset>): Builder {
            val (viewType, decorator) = pair
            return apply { scopeOffsets.add(DecorBinder(viewType, decorator)) }
        }

        /**
         * Добавить offset-декоратор для всех viewType.
         */
        fun offset(decor: Offset): Builder {
            return apply { scopeOffsets.add(DecorBinder(EACH_VIEW, decor)) }
        }

        /**
         * Builds the main decorator
         */
        fun build(): MainDecorator {
            require(
                scopeOffsets.groupingBy { it.viewType }.eachCount().all { it.value == 1 }
            ) { "Any ViewHolder can have only a single OffsetDecorator" }

            return MainDecorator(
                DecorHelper(
                    scopeViewHolderUnderlay,
                    scopeRecyclerViewUnderlay,
                    scopeViewHolderOverlay,
                    scopeRecyclerViewOverlay,
                    scopeOffsets
                )
            )
        }
    }

    /**
     * Интерфейс для получения offsets
     */
    interface Offset {
        fun getItemOffsets(
            outRect: Rect,
            view: View,
            recyclerView: RecyclerView,
            state: RecyclerView.State
        )
    }

    /**
     * Интерфейс для рисования на поверхности, занимаемой элементом списка
     */
    interface ViewHolderDecorator {
        fun draw(canvas: Canvas, view: View, recyclerView: RecyclerView, state: RecyclerView.State)
    }

    /**
     * Интерфейс для рисования в произвольном месте поверхности RecyclerView
     */
    interface RecyclerViewDecorator {
        fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State)
    }
}