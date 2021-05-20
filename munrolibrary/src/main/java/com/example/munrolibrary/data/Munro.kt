package com.example.munrolibrary.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Munro(
    val name: String,
    val heightInMeters: Double,
    val category: String,
    val gridRef: String
) : Parcelable
