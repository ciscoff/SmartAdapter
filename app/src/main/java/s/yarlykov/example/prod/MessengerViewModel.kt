package s.yarlykov.example.prod

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import s.yarlykov.example.extentions.logIt
import s.yarlykov.example.prod.data.ModelGenerator
import s.yarlykov.example.prod.domain.MockMessage
import s.yarlykov.example.prod.domain.ModelState
import s.yarlykov.example.prod.domain.UserAction
import java.time.LocalDate

class MessengerViewModel : ViewModel() {

    private val modelStateMutable = MutableLiveData<ModelState>()
    val modelState = modelStateMutable as LiveData<ModelState>

    // Модель
    private val model = mutableListOf<MockMessage>()

    override fun onCleared() {
        logIt(this.toString().substringAfterLast("@"))
        super.onCleared()
    }

    fun onStart() {
        logIt(this.toString().substringAfterLast("@"))

        when (model.isEmpty()) {
            true -> {
                modelStateMutable.value = ModelState.Init
                fetch()
            }
            false -> {
                modelStateMutable.value = ModelState.Success(model)
            }
        }
    }

    fun onStop() {
        logIt(this.toString().substringAfterLast("@"))
        // TODO
    }

    fun onDestroy() {
        logIt(this.toString().substringAfterLast("@"))
        // TODO
    }

    /**
     * TODO Добавить проверку, что предыдущая загрузка закончена
     *
     */
    fun fetch(withFailure: Boolean = true) {
        viewModelScope.launch(Dispatchers.Main) {

            if (withFailure) {
                loadWithFailure()
            } else {
                loadMockData()
            }
        }
    }

    fun fetchMore() {
        // Не реагировать на пагинацию, если нет сообщений в списке
        if(modelState.value !is ModelState.Success &&
            modelState.value !is ModelState.Updated) {
            return
        }

        viewModelScope.launch {
            val pageModel = ModelGenerator.nextPage(model.lastIndex, PAGE_SIZE)

            if (pageModel.isNotEmpty()) {
                val filteredModel = model.filterIsInstance<MockMessage.Data>()

                model.apply {
                    clear()
                    addAll(groupByDate(filteredModel + pageModel))
                }

                // Показываем начало загрузки. Можно в ответ покрутить ProgressBar.
                modelStateMutable.value = ModelState.Updating

                // Имитация длительной загрузки и обновление модели.
                delay(1000)

                modelStateMutable.value = ModelState.Updated(model)
            }
        }
    }

    fun onUserAction(action: UserAction) {
        when (action) {
            UserAction.MarkAllAsRead -> {
                model.forEach {
                    if (it is MockMessage.Data) {
                        it.isUnread = false
                    }
                }
                modelStateMutable.value = ModelState.Success(model)
            }
            UserAction.DeleteAll -> {
                model.clear()
                fetch(true)
            }
            is UserAction.MarkAsRead -> {
                val item = model[action.position]
                if (item is MockMessage.Data) {
                    item.isUnread = false
                    modelStateMutable.value = ModelState.Success(model)
                }
            }
            is UserAction.Delete -> {
                val groupId = (model[action.position] as? MockMessage.Data)?.groupId ?: return
                model.removeAt(action.position)

                // Если удалены все сообщения из группы, то удалить и заголовок этой группы.
                deleteHeaderIfNoMoreMessages(groupId)

                if (model.isNotEmpty()) {
                    modelStateMutable.value = ModelState.Success(model)
                } else {
                    modelStateMutable.value = ModelState.Failure()
                }


            }
        }
    }

    private fun deleteHeaderIfNoMoreMessages(groupId: String) {
        val remain = model.filterIsInstance<MockMessage.Data>().count { it.groupId == groupId }
        if (remain == 0) {

            val index = model.indexOfFirst {
                (it as? MockMessage.Header)?.groupId == groupId
            }

            if (index != -1) {
                model.removeAt(index)
            }
        }
    }

    private suspend fun loadWithFailure() {
        logIt(this.toString().substringAfterLast("@"))
        modelStateMutable.value = ModelState.Loading
        delay(LOAD_DELAY)
        modelStateMutable.value = ModelState.Failure()
    }

    private fun loadMockData() {
        val newModel = ModelGenerator.createModel(MODEL_SIZE, LocalDate.now())

        model.apply {
            clear()
            addAll(groupByDate(newModel))
        }
        modelStateMutable.value = ModelState.Success(model)
    }

    /**
     * Метод группирует входные сообщения по датам, вставляет дополнительные элементы для заголовков
     * и сортрует релультирующий список по убыванию даты.
     */
    private fun groupByDate(messages: List<MockMessage>): List<MockMessage> {
        val result = mutableListOf<MockMessage>()

        messages
            .asSequence()
            .filterIsInstance<MockMessage.Data>()
            // Получить Map<Дата, Список_сообщений_на_эту_дату>
            .fold(mutableMapOf<String, MutableList<MockMessage.Data>>()) { acc, message ->
                acc.apply {
                    val list = this[message.groupId] ?: mutableListOf()
                    list.add(message)
                    this[message.groupId] = list
                }
            }
            // Сортировка по убыванию даты
            .toSortedMap { sz1, sz2 ->
                when {
                    sz2 > sz1 -> 1
                    sz2 == sz1 -> 0
                    else -> -1
                }
            }
            // Формирование окончательного списка с добавлением заголовков
            .entries.forEach {
                val (k, v) = it
                result.add(MockMessage.Header(k))
                result.addAll(v)
            }

        return result
    }


    companion object {
        private const val LOAD_DELAY = 2000L
        private const val MODEL_SIZE = 30
        private const val PAGE_SIZE = 10
    }
}