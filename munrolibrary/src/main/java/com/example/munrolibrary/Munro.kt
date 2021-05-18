package com.example.munrolibrary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Munro(
    val name: String?,
    val heightInMeters: Long?,
    val category: String?,
    val gridRef: String?
) : Parcelable
