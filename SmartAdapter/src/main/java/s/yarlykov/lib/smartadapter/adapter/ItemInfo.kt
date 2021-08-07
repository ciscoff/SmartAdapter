package s.yarlykov.lib.smartadapter.adapter

/**
 * Мета данные отдельного элемента списка, которые представляют этот элемент для DiffUtil.
 *
 * @property id - уникальный ID элемента модели
 * @property hash - hash содержимого элемента модели
 */
data class ItemInfo(val id: String, val hash: String)