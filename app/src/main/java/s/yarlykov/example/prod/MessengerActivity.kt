package s.yarlykov.example.prod

import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import s.yarlykov.decoration.Decorator
import s.yarlykov.decoration.offset.OffsetDecorator
import s.yarlykov.decoration.sticky.StickyItemDecorator
import s.yarlykov.example.R
import s.yarlykov.example.databinding.ActivityMessengerBinding
import s.yarlykov.example.extentions.expandTouchArea
import s.yarlykov.example.extentions.px
import s.yarlykov.example.prod.domain.*
import s.yarlykov.example.prod.rv.*
import s.yarlykov.lib.smartadapter.events.EventWrapper
import s.yarlykov.lib.smartadapter.model.SmartList
import s.yarlykov.lib.smartadapter.pagination.PagedSmartAdapter
import s.yarlykov.lib.smartadapter.pagination.TakeMore

class MessengerActivity : AppCompatActivity() {

    private lateinit var viewModel: MessengerViewModel

    private lateinit var binding: ActivityMessengerBinding

    /**
     * Один и тот же адаптер используется на протяжении всего жизненного цикла NotificationsView.
     * При этом он подключается к заново пересоздаваемым RecyclerView.
     */
    private val smartAdapter = PagedSmartAdapter(PAGING_PREFETCH_DISTANCE)

    /**
     * Контроллеры для различных элементов списка
     */
    private val shimmerViewController =
        StubController(R.layout.layout_shimmer_item)
    private val failureViewController =
        FailureController(R.layout.layout_item_load_failure)
    private val timeStampController =
        TimeStampController(R.layout.layout_item_time_stamp)
    private val notificationController: NotificationController =
        NotificationController(R.layout.layout_unc_message_container)

    /**
     * Sticky декоратор
     */
    private val stickyDecorator = StickyItemDecorator()

    /**
     * Offset декоратор
     */
    private val offsetsDecor =
        OffsetDecorator(
            left = OFFSET_HOR.px,
            top = OFFSET_VER.px,
            right = OFFSET_HOR.px,
            bottom = OFFSET_VER.px
        )

    /**
     * Результирующий декоратор для списка показывающего загрузку
     */
    private val noDataDecorator =
        Decorator.Builder()
            .offset(shimmerViewController.viewType() to offsetsDecor)
            .build()

    /**
     * Результирующий декоратор для списка показывающего данные
     */
    private val dataDecorator =
        Decorator.Builder()
            .overlay(stickyDecorator)
            .offset(shimmerViewController.viewType() to offsetsDecor)
            .build()

    private var isReloading = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessengerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewBinding {
            recyclerViewUnc.setup()
        }

        viewModel = ViewModelProvider(this)[MessengerViewModel::class.java]

        viewModel.apply {
            onStart()

            // Не подписываемся повторно на LiveData
            if (modelState.hasActiveObservers()) {
                return@apply
            }

            modelState.observe(this@MessengerActivity) { modelState ->

                viewBinding {
                    progressBar.visibility = View.INVISIBLE
                }

                when (modelState) {
                    ModelState.Init -> {
                        viewBinding {
                            controlBar.hide()
                        }
                    }
                    ModelState.Loading -> {
                        viewBinding {
                            controlBar.hide()
                            recyclerViewUnc.showLoading()
                        }
                    }
                    ModelState.Updating -> {
                        viewBinding {
                            progressBar.visibility = View.VISIBLE
                        }
                    }
                    is ModelState.Failure -> {
                        viewBinding {
                            controlBar.hide()
                            recyclerViewUnc.showFailure(modelState.data)
                        }
                    }
                    is ModelState.Success -> {
                        viewBinding {
                            controlBar.show(modelState.data)
                            recyclerViewUnc.showData(modelState.data)
                        }
                    }
                    is ModelState.Updated -> {
                        viewBinding {
                            controlBar.show(modelState.data)
                            recyclerViewUnc.showData(modelState.data)
                        }
                    }
                }
            }
        }

        observeOf(smartAdapter.eventFlow)
    }

    /**
     * Начальная инициализация списка
     */
    private fun RecyclerView.setup() {
        itemAnimator = null

        // todo обратить внимание, что адаптер назначаем после LayoutManager'а иначе
        // todo будет крэш в PagedSmartAdapter.
        layoutManager = NotificationsLayoutManager(context)
        adapter = smartAdapter

        val touchHelper = NotificationTouchHelper(context)
        addOnItemTouchListener(touchHelper)
        addOnScrollListener(touchHelper)
    }

    /**
     * Пока нет реальных данных, то показываем анимированную Shimmer-заглушку.
     */
    private fun RecyclerView.showLoading() {
        val animationController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)

        isReloading = true

        // Очистить пул для правильной отрисовки анимации.
        recycledViewPool.clear()

        // Анимации
        itemAnimator = null
        layoutAnimation = animationController

        // При показе анимированных заглушек отключаем скроллинг.
        (layoutManager as? NotificationsLayoutManager)?.isScrollable = false

        // Настроить декораторы
        clearDecorations()
        addItemDecoration(noDataDecorator)

        // Формируем модель
        SmartList.create().apply {
            repeat(STUB_VIEWS_QTY) {
                addItem(shimmerViewController)
            }
        }.also(smartAdapter::updateModel)

        scheduleLayoutAnimation()
    }

    /**
     * Показ единственного элемента с UI отображения ошибки
     */
    private fun RecyclerView.showFailure(description: String?) {
        (layoutManager as? NotificationsLayoutManager)?.isScrollable = false
        clearDecorations()

        // Плавный переход. В качестве scene используем RecyclerView, чтобы без скачков.
        itemAnimator = null
        viewBinding {
            TransitionManager.beginDelayedTransition(recyclerViewUnc)
        }

        SmartList.create().apply {
            addItem(failureViewController)
        }.also(smartAdapter::updateModel)
    }

    /**
     * Показ списка с данными
     */
    private fun RecyclerView.showData(model: List<MockMessage>) {

        (layoutManager as? NotificationsLayoutManager)?.isScrollable = true
        clearDecorations()
        addItemDecoration(dataDecorator)

        layoutAnimation = null

        if (isReloading) {
            viewBinding {
                controlBarAction.expandTouchArea(_top = EXPAND_AREA, _bottom = EXPAND_AREA)
                TransitionManager.beginDelayedTransition(recyclerViewUnc.parent as ViewGroup)
            }
            itemAnimator = null
            isReloading = false
        } else {
            itemAnimator = DefaultItemAnimator()
        }

        SmartList.create().apply {
            model.forEach { item ->

                when (item) {
                    is MockMessage.Header -> {
                        addItem(item, timeStampController)
                    }
                    is MockMessage.Data -> {
                        addItem(item, notificationController)
                    }
                }
            }
        }.also(smartAdapter::updateModel)
    }

    /**
     * Удалить все текущие декораторы
     */
    private fun RecyclerView.clearDecorations() {
        while (itemDecorationCount > 0) {
            removeItemDecorationAt(0)
        }
    }

    /**
     * Скрыть панель действий
     */
    private fun ViewGroup.hide() {
        this.visibility = View.GONE

        viewBinding {
            controlBarAction.setOnClickListener(null)
        }
    }

    /**
     * Паказать панель действий
     */
    private fun ViewGroup.show(model: List<MockMessage>) {
        this.visibility = View.VISIBLE

        // Количество непрочитанных
        val unread = model.filterIsInstance<MockMessage.Data>().count { it.isUnread }

        val controlBarState =
            if (unread > 0) {
                ControlBarState(
                    context.getString(R.string.notification_new_count, unread),
                    context.getString(R.string.notification_center_read_all),
                    UserAction.MarkAllAsRead
                )
            } else {
                ControlBarState(
                    context.getString(R.string.notification_no_new),
                    context.getString(R.string.notification_delete_all),
                    UserAction.DeleteAll
                )
            }

        viewBinding {
            controlBarTitle.text = controlBarState.title
            controlBarAction.text = controlBarState.proposal

            // TODO ??
            controlBarAction.tag = controlBarState.action

            controlBarAction.setOnClickListener {
                (it.tag as? UserAction)?.let(viewModel::onUserAction)
            }
        }
    }

    /**
     * Вспомогательная функция, заменяющая повсеместное использование 'binding?'
     */
    private fun viewBinding(op: ActivityMessengerBinding.() -> Unit) {
        binding.op()
    }

    /**
     * Обработка событий из элементов RecyclerView
     */
    private fun dispatchAdapterEvent(event: AdapterEvent) {
        when (event) {
            AdapterEvent.Update -> {
                viewModel.fetch(withFailure = false)
            }
            is AdapterEvent.Done -> {
                viewModel.onUserAction(UserAction.MarkAsRead(event.position))
            }
            is AdapterEvent.Delete -> {
                viewModel.onUserAction(UserAction.Delete(event.position))
            }
        }
    }

    /**
     * Получение событий от элементов списка
     */
    private fun observeOf(events: SharedFlow<EventWrapper<Any>>) {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                events.collect { wrapper ->
                    wrapper.getContentIfNotHandled()?.let { event ->
                        when (event) {
                            is AdapterEvent -> {
                                dispatchAdapterEvent(event)
                            }
                            is TakeMore -> {
                                // todo запросить у модели новую страницу
                                viewModel.fetchMore()
                            }
                            else -> {
                                throw IllegalArgumentException("Unknown event type")
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val STUB_VIEWS_QTY = 15
        private const val OFFSET_HOR = 12
        private const val OFFSET_VER = 2
        private const val EXPAND_AREA = 18
        private const val PAGING_PREFETCH_DISTANCE = 10
    }
}