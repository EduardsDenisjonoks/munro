package com.example.munrolibrary.constants

import androidx.annotation.StringDef
import com.example.munrolibrary.constants.MunroSortOptions.Companion.ASC
import com.example.munrolibrary.constants.MunroSortOptions.Companion.DESC

@Retention(AnnotationRetention.SOURCE)
@StringDef(ASC, DESC)
annotation class MunroSortOptions {

    companion object {
        const val ASC: String = "asc"
        const val DESC: String = "desc"
    }

}
