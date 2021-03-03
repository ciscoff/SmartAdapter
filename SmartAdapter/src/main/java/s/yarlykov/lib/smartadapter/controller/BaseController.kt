package s.yarlykov.lib.smartadapter.controller

import android.view.ViewGroup
import s.yarlykov.lib.smartadapter.holder.BaseViewHolder
import s.yarlykov.lib.smartadapter.model.item.BaseItem

/**
 * Базовый класс контроллера. Его задача сформировать viwType, сгенерить ViewHolder и
 * сбиндить holder и Item. viewType формируется на базе двух значений:
 * - тип данных T (сейчас не используется)
 * - layoutId элемента списка
 *
 * Это позволит не только назначать различные холдеры для различных данных, но и
 * показывать однотипные данные в разных представляениях. Например часть элемента однотипного
 * списка вывести в одной layout, часть в другой и каждая со своим типом холдера.
 */
abstract class BaseController<H : BaseViewHolder, I : BaseItem<H>> {

    abstract fun createViewHolder(parent: ViewGroup): H

    /**
     * Адаптер вызывает этот метод и "знакомит" контроллер с его контейнером (Item).
     * Если у контейнера есть данные, то он передаст их в холдер через holder.bind(item.data)
     */
    abstract fun bind(holder: H, item: I)
    abstract fun viewType(): Int
}