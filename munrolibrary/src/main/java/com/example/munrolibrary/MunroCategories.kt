package com.example.munrolibrary

import androidx.annotation.StringDef
import com.example.munrolibrary.MunroCategories.Companion.MUNRO
import com.example.munrolibrary.MunroCategories.Companion.MUNRO_TOP
import com.example.munrolibrary.MunroCategories.Companion.NONE

@Retention(AnnotationRetention.SOURCE)
@StringDef(NONE, MUNRO, MUNRO_TOP)
annotation class MunroCategories {

    companion object {
        const val NONE: String = "none"
        const val MUNRO: String = "MUN"
        const val MUNRO_TOP: String = "TOP"
    }

}
