package com.example.munrolibrary

sealed class CsvReadResult <out T> {

    data class Success(val isSuccess: Boolean): CsvReadResult<Boolean>()
    data class Error(val errorMessage: String) : CsvReadResult<Nothing>()

    companion object {
        fun success(isSuccess: Boolean): CsvReadResult<Boolean> = Success(isSuccess)
        fun error(errorMessage: String): CsvReadResult<Nothing> = Error(errorMessage)
    }
}