package s.yarlykov.lib.smartadapter.model.item

import s.yarlykov.lib.smartadapter.controller.BaseController
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder

/**
 * Базовый контейнер для элемента модели. Это не элемент модели, а именно контейнер.
 * Он ничего не знает о том как будет отображаться его элемент на экране.
 * Всю работу по отображению он делегирует контроллеру.
 */
abstract class BaseItem<H : BaseViewHolder>(val controller: BaseController<H, BaseItem<H>>)