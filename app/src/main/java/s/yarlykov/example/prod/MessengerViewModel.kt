package s.yarlykov.example.prod

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import s.yarlykov.example.extentions.logIt
import s.yarlykov.example.prod.domain.MockUncMessage
import s.yarlykov.example.prod.domain.ModelState
import s.yarlykov.example.prod.domain.UserAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MessengerViewModel : ViewModel() {

    private val modelStateMutable = MutableLiveData<ModelState>()
    val modelState = modelStateMutable as LiveData<ModelState>

    // Модель
    private val model = mutableListOf<MockUncMessage>()

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

    fun onUserAction(action: UserAction) {
        when (action) {
            UserAction.MarkAllAsRead -> {
                model.forEach {
                    if (it is MockUncMessage.Data) {
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
                if (item is MockUncMessage.Data) {
                    item.isUnread = false
                    modelStateMutable.value = ModelState.Success(model)
                }
            }
            is UserAction.Delete -> {

                val date = (model[action.position] as? MockUncMessage.Data)?.date ?: return
                model.removeAt(action.position)

                // Если удалены все сообщений на определенную дату, то удалить и заголовок
                // с этой датой.
                deleteHeaderIfNoMoreMessages(date)

                if (model.isNotEmpty()) {
                    modelStateMutable.value = ModelState.Success(model)
                } else {
                    modelStateMutable.value = ModelState.Failure()
                }


            }
        }
    }

    private fun deleteHeaderIfNoMoreMessages(date: String) {
        val remain = model.filterIsInstance<MockUncMessage.Data>().count { it.date == date }
        if (remain == 0) {

            val index = model.indexOfFirst {
                (it as? MockUncMessage.Header)?.date == date
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

        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val now = LocalDate.now()

        model.clear()

        var date = LocalDate.now().format(formatter)

        repeat(15) { i ->

            if (i.rem(3) == 0) {
                date = now.minusDays(i.toLong()).format(formatter)
                model.add(MockUncMessage.Header(date))
            }

            // Первые UNREAD_COUNT пометить как "непрочитанные"
            model.add(MockUncMessage.Data(i < UNREAD_COUNT, date))
        }

        modelStateMutable.value = ModelState.Success(model)
    }

    companion object {
        private const val LOAD_DELAY = 2000L
        private const val DATE_PATTERN = "dd MMMM"
        private const val UNREAD_COUNT = 10
    }
}