package s.yarlykov.lib.smartadapter.events

fun interface SmartCallback<T : Any?> {
    fun call(arg: T)
}