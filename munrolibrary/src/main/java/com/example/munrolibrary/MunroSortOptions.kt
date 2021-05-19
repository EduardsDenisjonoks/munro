package com.example.munrolibrary

import androidx.annotation.StringDef
import com.example.munrolibrary.MunroSortOptions.Companion.ASC
import com.example.munrolibrary.MunroSortOptions.Companion.DESC

@Retention(AnnotationRetention.SOURCE)
@StringDef(ASC, DESC)
annotation class MunroSortOptions {

    companion object {
        const val ASC: String = "asc"
        const val DESC: String = "desc"
    }

}
