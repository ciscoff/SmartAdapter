package s.yarlykov.lib.smartadapter.holder

fun interface SmartCallback<T : Any?> {
    fun call(arg: T)
}