package s.yarlykov.example.prod.domain

sealed class AdapterEvent {
    object Update : AdapterEvent()
    data class Delete(val position: Int) : AdapterEvent()
    data class Done(val position: Int) : AdapterEvent()
}